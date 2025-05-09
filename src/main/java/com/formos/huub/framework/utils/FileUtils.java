/**
 * ***************************************************
 * * Description :
 * * File        : FileUtil
 * * Author      : Hung Tran
 * * Date        : Dec 27, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.utils;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * System temporary directory
     * <br>
     * Windows includes path separators, but Linux does not,
     * With windows \\==\,
     * <pre>
     *       java.io.tmpdir
     *       windows : C:\Users/xxx\AppData\Local\Temp\
     *       linux: /temp
     * </pre>
     */
    public static final String SYSTEM_TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator;

    /**
     * Define the calculation constant of GB
     */
    private static final int GB = 1024 * 1024 * 1024;

    /**
     * Define the calculation constant of MB
     */
    private static final int MB = 1024 * 1024;

    /**
     * Define the calculation constant of KB
     */
    private static final int KB = 1024;

    /**
     * Format decimal
     */
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public static final String IMAGE = "Image";
    public static final String TXT = "Text";
    public static final String MUSIC = "Music";
    public static final String VIDEO = "Video";
    public static final String OTHER = "Other";

    private static final Map<String, MediaType> MEDIA_TYPE_MAP = new HashMap<>();

    static {
        MEDIA_TYPE_MAP.put("PDF", MediaType.APPLICATION_PDF);
        MEDIA_TYPE_MAP.put("TXT", MediaType.TEXT_PLAIN);
        MEDIA_TYPE_MAP.put("DOC", MediaType.parseMediaType("application/msword"));
        MEDIA_TYPE_MAP.put("DOCX", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        MEDIA_TYPE_MAP.put("PPT", MediaType.parseMediaType("application/vnd.ms-powerpoint"));
        MEDIA_TYPE_MAP.put("PPTX", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        MEDIA_TYPE_MAP.put("XLS", MediaType.parseMediaType("application/vnd.ms-excel"));
        MEDIA_TYPE_MAP.put("XLSX", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        MEDIA_TYPE_MAP.put("PNG", MediaType.IMAGE_PNG);
        MEDIA_TYPE_MAP.put("JPEG", MediaType.IMAGE_JPEG);
        MEDIA_TYPE_MAP.put("JPG", MediaType.IMAGE_JPEG);
        MEDIA_TYPE_MAP.put("BMP", MediaType.parseMediaType("image/bmp"));
        MEDIA_TYPE_MAP.put("DIB", MediaType.parseMediaType("image/bmp"));
        MEDIA_TYPE_MAP.put("PCP", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("DIF", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("WMF", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("GIF", MediaType.IMAGE_GIF);
        MEDIA_TYPE_MAP.put("TIF", MediaType.parseMediaType("image/tiff"));
        MEDIA_TYPE_MAP.put("EPS", MediaType.parseMediaType("application/postscript"));
        MEDIA_TYPE_MAP.put("PSD", MediaType.parseMediaType("image/vnd.adobe.photoshop"));
        MEDIA_TYPE_MAP.put("CDR", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("IFF", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("TGA", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("PCD", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("MPT", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("MP4", MediaType.parseMediaType("video/mp4"));
        MEDIA_TYPE_MAP.put("MOV", MediaType.parseMediaType("video/quicktime"));
        MEDIA_TYPE_MAP.put("AVI", MediaType.parseMediaType("video/x-msvideo"));
        MEDIA_TYPE_MAP.put("SRT", MediaType.parseMediaType("application/octet-stream"));
        MEDIA_TYPE_MAP.put("VTT", MediaType.parseMediaType("text/vtt"));
        MEDIA_TYPE_MAP.put("HEIF", MediaType.parseMediaType("image/heif"));
        MEDIA_TYPE_MAP.put("WEBP", MediaType.parseMediaType("image/webp"));
        MEDIA_TYPE_MAP.put("HEIC", MediaType.parseMediaType("image/heic"));
        MEDIA_TYPE_MAP.put("SVG", MediaType.parseMediaType("image/svg+xml"));
        MEDIA_TYPE_MAP.put("ICO", MediaType.parseMediaType("image/x-icon"));
        MEDIA_TYPE_MAP.put("CVS", MediaType.parseMediaType("text/csv"));
    }

    public static MediaType contentType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        String fileExtension = filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
        return MEDIA_TYPE_MAP.getOrDefault(fileExtension, MediaType.APPLICATION_OCTET_STREAM);
    }

    /**
     * Get the file extension with the dot
     */
    public static String getExtensionName(final String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * Get file name without extension
     */
    public static String getFileNameNoEx(final String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * File Size Converter
     */
    public static String getSize(final long size) {
        String resultSize;
        if (size / GB >= 1) {
            // If the current Byte value is greater than or equal to 1GB
            resultSize = DF.format(size / (float) GB) + " GB";
        } else if (size / MB >= 1) {
            // If the current Byte value is greater than or equal to 1MB
            resultSize = DF.format(size / (float) MB) + " MB";
        } else if (size / KB >= 1) {
            // If the current Byte value is greater than or equal to 1KB
            resultSize = DF.format(size / (float) KB) + " KB";
        } else {
            resultSize = size + " B";
        }
        return resultSize;
    }

    /**
     * Get type file
     *
     * @param type
     * @return String
     */
    public static String getFileType(final String type) {
        final String documents = "txt doc pdf ppt pptx pps xlsx xls docx";
        final String music = "mp3 wav wma mpa ram ra aac aif m4a";
        final String video = "avi mpg mpe mpeg asf wmv mov qt rm mp4 flv m4v webm ogv ogg";
        final String image = "bmp dib pcp dif wmf gif jpg tif eps psd cdr iff tga pcd mpt png jpeg heif heic svg webp ico";
        if (image.contains(type)) {
            return IMAGE;
        } else if (documents.contains(type)) {
            return TXT;
        } else if (music.contains(type)) {
            return MUSIC;
        } else if (video.contains(type)) {
            return VIDEO;
        } else {
            return OTHER;
        }
    }

    /**
     * Check file size
     *
     * @param maxSize
     * @param size
     * @return boolean
     */
    public static boolean checkSize(final long maxSize, final long size) {
        // 1M
        int len = 1024 * 1024;
        return size > (maxSize * len);
    }

    /**
     * Check path is directory
     *
     * @param path
     * @return boolean
     */
    public static boolean isDirectory(final Path path) {
        if (path == null) {
            return false;
        }
        return Files.isDirectory(path);
    }

    /**
     * Removed special characters for fileName
     *
     * @param fileName
     * @return String
     */
    public String standardizedFileName(String fileName) {
        String ret = fileName;

        ret = ret.replace("\"", "'");
        ret = ret.replace("\\\\", "_");
        ret = ret.replaceAll("[/:*?<>|]", "_");
        return ret;
    }

    public static String makeFileNameWithTime(final String filename) {
        String suffix = FileUtils.getExtensionName(filename);
        String name = FileUtils.getFileNameNoEx(filename);
        return name + "." + suffix;
    }

    public static String standardizedFileKey(String keyName) {
        return keyName.trim().replaceAll("\\s+", "_");
    }

    public static ByteArrayOutputStream cropToImage(InputStream input, int size) throws IOException {
        String FILE_TYPE_CROP = "JPEG";
        BufferedImage image = ImageIO.read(input);
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= size && height <= size) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, FILE_TYPE_CROP, os);
            return os; // no need to crop
        }

        int x = 0, y = 0;
        int croppedSize = Math.min(width, height);

        if (width > height) {
            x = (width - croppedSize) / 2;
        } else {
            y = (height - croppedSize) / 2;
        }

        BufferedImage croppedImage = new BufferedImage(croppedSize, croppedSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = croppedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, croppedSize, croppedSize, x, y, x + croppedSize, y + croppedSize, null);
        g.dispose();

        BufferedImage scaledImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(croppedImage, 0, 0, size, size, null);
        g.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(scaledImage, FILE_TYPE_CROP, os);
        return os;
    }

    public static byte[] convertMarkdownToPdf(String markdownText, String additionalMarkdownText) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(String.join("", additionalMarkdownText, markdownText));
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlContent = renderer.render(document);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ConverterProperties converterProperties = new ConverterProperties();
        HtmlConverter.convertToPdf(htmlContent, byteArrayOutputStream, converterProperties);

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Exports a list of maps as a CSV response with a custom filename.
     *
     * @param response HttpServletResponse to send CSV data.
     * @param data     List of maps where keys are column names and values are row values.
     * @param filename Desired name of the exported CSV file.
     */
    public static void exportCsv(HttpServletResponse response, List<String> headers, List<Map<String, String>> data, String filename) {
        if (headers.isEmpty()) {
            throw new RuntimeException("Headers cannot be empty.");
        }

        if (!filename.toLowerCase().endsWith(".csv")) {
            filename += ".csv";
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (OutputStream outputStream = response.getOutputStream();
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
             CSVWriter csvWriter = new CSVWriter(bufferedWriter)) {

            bufferedWriter.write("\uFEFF");

            csvWriter.writeNext(headers.toArray(new String[0]));

            for (Map<String, String> row : data) {
                String[] values = headers.stream()
                    .map(header -> formatCsvValue(row.getOrDefault(header, "")))
                    .toArray(String[]::new);
                csvWriter.writeNext(values);
            }

            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting CSV: " + e.getMessage(), e);
        }
    }

    public static String formatCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        if (value.matches("\\d+-\\d+") || value.matches("\\d{15,}") || value.matches("0\\d+")) {
            return "=\"" + value + "\"";
        }

        if (value.startsWith("=") || value.startsWith("-")) {
            return "'" + value;
        }
        return value;
    }
}
