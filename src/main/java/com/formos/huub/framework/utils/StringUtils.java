/**
 * ***************************************************
 * * Description :
 * * File        : StringUtils
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.utils;

import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.enums.CodeEnum;
import com.formos.huub.framework.enums.DateTimeFormat;

import java.math.BigDecimal;
import java.net.URL;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.itextpdf.styledxmlparser.jsoup.Jsoup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.DigestUtils;

@Slf4j
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private static final char SEPARATOR = '_';

    private static final String UNKNOWN = "unknown";

    private static final String EMPTY_STRING = "";

    private static final int DEF_COUNT = 20;

    private static final SecureRandom SECURE_RANDOM;

    static {
        SECURE_RANDOM = new SecureRandom();
        SECURE_RANDOM.nextBytes(new byte[64]);
    }

    /**
     * Generate Checksum String
     *
     * @param content String
     * @return String
     */
    public static String generateChecksum(String content) {
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }

    /**
     * toCamelCase
     *
     * @return toCamelCase(" hello_world ") == "helloWorld"
     * toCapitalizeCamelCase("hello_world") == "HelloWorld"
     * toUnderScoreCase("helloWorld") = "hello_world"
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }

        s = s.toLowerCase();

        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * toCapitalizeCamelCase
     *
     * @return toCamelCase(" hello_world ") == "helloWorld"
     * toCapitalizeCamelCase("hello_world") == "HelloWorld"
     * toUnderScoreCase("helloWorld") = "hello_world"
     */
    public static String toCapitalizeCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * toUnderScoreCase
     *
     * @return toCamelCase(" hello_world ") == "helloWorld"
     * toCapitalizeCamelCase("hello_world") == "HelloWorld"
     * toUnderScoreCase("helloWorld") = "hello_world"
     */
    static String toUnderScoreCase(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            boolean nextUpperCase = true;

            if (i < (s.length() - 1)) {
                nextUpperCase = Character.isUpperCase(s.charAt(i + 1));
            }

            if ((i > 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    sb.append(SEPARATOR);
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * convert list to array
     *
     * @param collection
     * @return
     */
    public static String[] toStringArray(Collection<String> collection) {
        String[] result = null;
        if (collection != null) {
            result = new String[collection.size()];
            result = collection.toArray(result);
        }
        return result;
    }

    /**
     * compare enum and string
     *
     * @param enums CodeEnum
     * @param value String
     * @return boolean
     */
    public static boolean equals(final CodeEnum enums, final String value) {
        return equals(enums.getValue(), value);
    }

    /**
     * Generate GroupNumber
     * @param prefix
     * @return
     */
    public static String generateGroupNumber(String prefix) {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000);
        return prefix + number;
    }

    public static String generateCode() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(999999);
        return String.format("%06d", number);
    }

    public static String generate6DigitalCode() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(899999);
        return String.format("%06d", number);
    }

    /**
     * Make String with contain
     * @param value String
     * @return String
     */
    public static String makeStringWithContain(String value) {
        if (Objects.isNull(value)) return null;
        if (isBlank(value)) value = EMPTY_STRING;
        return "%" + wildcards(value) + "%";
    }

    public static String wildcards(String searchKeyword) {
        if (Objects.isNull(searchKeyword)) return null;
        if (isBlank(searchKeyword)) return EMPTY_STRING;
        searchKeyword = searchKeyword.replace("%", "\\%");
        searchKeyword = searchKeyword.replace("_", "\\_");
        return searchKeyword.toLowerCase().trim();
    }

    /**
     * Generates a random alphanumeric string of the specified length.
     *
     * @param count the length of the random string to be generated
     * @return a randomly generated alphanumeric string of the specified length
     */
    public static String generateRandomAlphanumericString(int count) {
        return RandomStringUtils.random(count, 0, 0, true, true, null, SECURE_RANDOM);
    }

    /**
     * Generate random alpha numeric string
     * @return String
     */
    public static String generateRandomAlphanumericString() {
        return RandomStringUtils.random(DEF_COUNT, 0, 0, true, true, null, SECURE_RANDOM);
    }

    /**
     * Generate random numeric string
     * @return String
     */
    public static String generateRandomNumericString(int count) {
        return RandomStringUtils.random(count, 0, 0, false, true, null, SECURE_RANDOM);
    }

    /**
     * format StringDate
     * @param value String
     * @return String
     */
    public static String formatStringDate(String value) {
        var dateConvert = DateUtils.convertStringToLocalDate(value, DateTimeFormat.ISO_8601_BASIC_DATE_PATTERN);
        return String.valueOf(dateConvert);
    }

    /**
     * convert list string to string
     * @param value List String
     * @return String
     */
    public static String convertListToString(List<String> value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return value.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public static String convertSetToString(Set<String> value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return value.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * convert string to list string
     * @param value String
     * @return List String
     */
    public static List<String> convertStringToListString(String value) {
        if (isEmpty(value)) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(",")).collect(Collectors.toList());
    }

    public static Set<String> convertStringToSetString(String value) {
        if (isEmpty(value)) {
            return new HashSet<>();
        }
        return Arrays.stream(value.split(",")).collect(Collectors.toSet());
    }

    /**
     * convert string to list integer
     * @param value String
     * @return List Integer
     */
    public static List<Integer> convertStringToListInteger(String value) {
        if (isEmpty(value)) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

    /**
     *
     * return null if value is empty
     *
     * @param value
     * @return value or null
     */
    public static String replaceEmptyToNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value;
    }

    /**
     * @param strings
     * @return
     */
    public static String appendStrings(String... strings) {
        return String.join("", strings);
    }

    /**
     * appendStrings time zone
     * @param strings String
     * @param strings String
     * @return string
     */
    public static String makeDateTimeWithTimeZone(String... strings) {
        String rs = StringUtils.appendStrings(strings);
        return StringUtils.appendStrings(rs, AppConstants.KEY_SPACE);
    }

    /**
     * appendStringsToUppercase
     * @param strings
     * @return String
     */
    public static String appendStringsToUppercase(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (!"".equals(strings[i])) {
                sb.append(toCapitalizeCamelCase(strings[i]));
            }
            if (i < strings.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String capitalizeFirstLetterInEachWord(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return Arrays.stream(input.split("\\s+"))
            .map(word -> word.isEmpty() ? word : toCapitalizeCamelCase(word))
            .collect(Collectors.joining(" "));
    }

    /**
     *
     * add trim character
     * @param string
     * @param charr
     * @return
     */
    public static String trimStringToCharacter(String string, Character charr) {
        if (string != null) {
            int lastCharacterIndex = string.lastIndexOf(charr);
            if (lastCharacterIndex != -1) {
                string = string.substring(0, lastCharacterIndex).trim();
            }
        }
        return string;
    }

    private static final String PHONE_NUMBER_PATTERN = "\\(\\d{3}\\) \\d{3}-\\d{4}";

    public static boolean validatePhoneNumber(String phoneNumber) {
        if (Objects.isNull(phoneNumber)) {
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_NUMBER_PATTERN);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public static String convertToE164(String phoneNumber) {
        String cleanedPhoneNumber = phoneNumber.replaceAll("\\D", "");
        if (cleanedPhoneNumber.startsWith("+")) {
            return cleanedPhoneNumber;
        } else {
            String countryCode = "+1"; // US country code
            return countryCode + cleanedPhoneNumber;
        }
    }

    public static String getFileNameFromURL(String path) {
        try {
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + path, e);
        }
    }

    public static String stripHtmlTags(String content) {
        return Jsoup.parse(content).text();
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Format the price with two decimal places and include the dollar sign ($), e.g., $100.00, $100.23, $100.60
     *
     * @param value Double
     * @return String
     */
    public static String convertToUSCurrency(Double value) {
        if (Objects.isNull(value)) {
            return "$0.00";
        }
        BigDecimal amount = BigDecimal.valueOf(value);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        return currencyFormat.format(amount);
    }
}
