package com.formos.huub.framework.service.translate;

import com.formos.huub.framework.enums.LanguageEnum;
import com.formos.huub.framework.utils.ObjectUtils;
import com.itextpdf.styledxmlparser.jsoup.Jsoup;
import com.itextpdf.styledxmlparser.jsoup.nodes.Document;
import com.itextpdf.styledxmlparser.jsoup.nodes.Element;
import com.itextpdf.styledxmlparser.jsoup.nodes.TextNode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.translate.TranslateAsyncClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

@Primary
@Service
@Slf4j
@RequiredArgsConstructor
public class AwsTranslateService implements ITranslateService {

    private final TranslateAsyncClient translateClient;
    private static final int MAX_BYTES = 10000;

    @Override
    public String translate(String text, LanguageEnum targetLang, LanguageEnum sourceLang) {
        try {
            String result = translateText(text, targetLang, sourceLang);
            log.info("Text: {}, Source Lang: {}, Target Lang: {}, Result: {}", text, sourceLang, targetLang, result);
            return result;
        } catch (Exception e) {
            log.error("Amazon Translate Exception:", e);
        }
        return null;
    }

    public String translateText(String text, LanguageEnum targetLang, LanguageEnum sourceLang) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        if (isHtml(text) && text.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return translateHtml(text, targetLang, sourceLang);
        }
        if (text.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return translateLongText(text, targetLang, sourceLang);
        } else {
            return translateSingleText(text, targetLang, sourceLang);
        }
    }

    private String translateSingleText(String text, LanguageEnum targetLang, LanguageEnum sourceLang) {
        final AtomicReference<String> reference = new AtomicReference<>();
        TranslateTextRequest textRequest = TranslateTextRequest.builder()
            .sourceLanguageCode(sourceLang.getValue())
            .targetLanguageCode(targetLang.getValue())
            .text(text)
            .build();
        CompletableFuture<TranslateTextResponse> futureGet = translateClient.translateText(textRequest);
        futureGet.whenComplete((resp, err) -> {
            if (resp != null) {
                reference.set(resp.translatedText());
            } else {
                log.error("AWS Translate error: ", err);
                reference.set(text);
            }
        });
        futureGet.join();
        return reference.get();
    }

    private String translateLongText(String text, LanguageEnum targetLang, LanguageEnum sourceLang) {
        List<String> segments = splitTextBySentence(text, MAX_BYTES);
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            builder.append(translateSingleText(segment, targetLang, sourceLang)).append(" ");
        }
        return builder.toString().trim();
    }

    private List<String> splitTextBySentence(String text, int maxBytes) {
        List<String> segments = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder currentSegment = new StringBuilder();
        for (String sentence : sentences) {
            int sentenceBytes = sentence.getBytes(StandardCharsets.UTF_8).length;
            int currentBytes = currentSegment.toString().getBytes(StandardCharsets.UTF_8).length;
            if (currentBytes + sentenceBytes > maxBytes) {
                if (currentBytes > 0) {
                    segments.add(currentSegment.toString().trim());
                    currentSegment.setLength(0);
                }
                if (sentenceBytes > maxBytes) {
                    segments.add(splitLongSentence(sentence, maxBytes));
                } else {
                    currentSegment.append(sentence).append(" ");
                }
            } else {
                currentSegment.append(sentence).append(" ");
            }
        }
        if (currentSegment.length() > 0) {
            segments.add(currentSegment.toString().trim());
        }
        return segments;
    }

    private String splitLongSentence(String sentence, int maxBytes) {
        List<String> parts = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        String[] words = sentence.split(" ");
        for (String word : words) {
            if (word.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
                parts.add(word);
                continue;
            }
            if (result.toString().getBytes(StandardCharsets.UTF_8).length + word.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
                parts.add(result.toString().trim());
                result.setLength(0);
            }
            result.append(word).append(" ");
        }
        if (result.length() > 0) {
            parts.add(result.toString().trim());
        }
        return String.join(" ", parts);
    }

    private String translateHtml(String html, LanguageEnum targetLang, LanguageEnum sourceLang) {
        Document doc = Jsoup.parse(html);
        Element element = doc.body();
        if (element == null || element.children().isEmpty()) {
            return html;
        }
        translateParagraph(element, targetLang, sourceLang);
        return element.html();
    }

    private void translateParagraph(Element element, LanguageEnum targetLang, LanguageEnum sourceLang) {
        if (Arrays.asList("script", "style", "meta").contains(element.tagName())) {
            return;
        }
        translateElementText(element, targetLang, sourceLang);
        for (Element child : element.children()) {
            translateParagraph(child, targetLang, sourceLang);
        }
    }

    private void translateElementText(Element element, LanguageEnum targetLang, LanguageEnum sourceLang) {
        List<TextNode> textNodes = element.textNodes();
        if (ObjectUtils.isEmpty(textNodes)) {
            return;
        }
        for (TextNode textNode : textNodes) {
            String translatedText = translateSingleText(textNode.text().trim(), targetLang, sourceLang);
            textNode.text(translatedText);
        }
    }

    private boolean isHtml(String text) {
        return !Jsoup.parse(text).body().children().isEmpty();
    }
}
