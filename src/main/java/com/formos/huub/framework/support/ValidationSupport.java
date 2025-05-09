/**
 * ***************************************************
 * * Description :
 * * File        : ValidationSupport
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.support;

import com.formos.huub.framework.enums.CodeEnum;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.utils.BooleanUtils;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.EnumUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ValidationSupport {

    public static final Pattern PATTERN_ALPHA = Pattern.compile("[\\p{Alpha}]*");

    public static final Pattern PATTERN_ALPHA_NUMERIC = Pattern.compile("[\\p{Alnum}]*");

    public static final Pattern PATTERN_PUNCT = Pattern.compile("[\\p{Punct}]*");

    public static final Pattern PATTERN_NUMERIC = Pattern.compile("[\\p{Digit}]*");

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * check half size numeric.
     *
     * @param target value
     * @return true if target is only contain half size numeric
     */
    public boolean isNumeric(final String target, final boolean isFloat) {
        if (isFloat) return NumberUtils.isParsable(target);
        return StringUtils.isNumeric(target);
    }

    /**
     * check enum is valid or not.
     *
     * @param enumClass enum class
     * @param target value of enum
     * @return true if enum is valid
     */
    public boolean isValidEnum(final Class<? extends CodeEnum> enumClass, final String target) {
        return EnumUtils.isValidEnum(enumClass, target);
    }

    /**
     * required check.
     *
     * @param target value
     * @return true if target is blank
     */
    public boolean isBlank(final String target) {
        return StringUtils.isBlank(target);
    }

    /**
     * check minimum character.
     *
     * @param target value
     * @param min min length
     * @return true if length of target is greater or equal specify length
     */
    public boolean checkMin(final String target, final long min) {
        return (StringUtils.length(target) >= min);
    }

    /**
     * check maximum character.
     *
     * @param target value
     * @param max max length
     * @return true if length of target is equal or smaller specify length
     */
    public boolean checkMax(final String target, final long max) {
        return (StringUtils.length(target) <= max);
    }

    /**
     * check length.
     *
     * @param target value
     * @param length specify length
     * @return true if length of target is equals specify length
     */
    public boolean checkLength(final String target, final long length) {
        return (StringUtils.length(target) == length);
    }

    /**
     * check alpha and numeric.
     *
     * @param target value
     * @return true if string only contain numeric
     */
    public boolean isAlphaOrNumeric(final String target) {
        return StringUtils.isAlphanumeric(target);
    }

    /**
     * check alpha character.
     *
     * @param target value
     * @return true if string only contain alpha character
     */
    public boolean isAlpha(final String target) {
        return StringUtils.isAlpha(target);
    }

    /**
     * check alpha, numeric and symbol.
     *
     * @param target value
     * @return true if string only contain alpha, numeric and symbol
     */
    public boolean isAlphaNumericSymbol(final String target) {
        return check(target, PATTERN_ALPHA_NUMERIC, PATTERN_PUNCT);
    }

    /**
     * check alpha, numeric and symbol.
     *
     * @param target value
     * @return true if string only contain alpha, numeric and symbol
     */
    public boolean isBooleanSymbol(final String target) {
        return BooleanUtils.toBooleanObject(target) != null;
    }

    /**
     * compare date.
     *
     * @param sFromDate from date
     * @param sToDate to date
     * @param pattern pattern
     * @return true if to date greater than from date, else return false
     */
    public boolean compareDate(final String sFromDate, final String sToDate, final DateTimeFormat pattern) {
        final Date frmDate = DateUtils.convertStringToDate(sFromDate, pattern);
        final Date toDate = DateUtils.convertStringToDate(sToDate, pattern);

        if (frmDate == null || toDate == null) {
            return false;
        }

        return toDate.after(frmDate);
    }

    /**
     * Check String Date is in the past
     * @param sourceDateStr source
     * @return true if sourceDateStr in the past, else return false
     */
    public boolean isPastInstant(final String sourceDateStr) {
        if (StringUtils.isBlank(sourceDateStr)) {
            return false;
        }

        final Instant sourceDate = Instant.parse(sourceDateStr);
        return sourceDate.isBefore(Instant.now());
    }

    /**
     * Check String Date is in the future
     * @param sourceDateStr source
     * @return true if sourceDateStr in the future, else return false
     */
    public boolean isFutureInstant(final String sourceDateStr) {
        if (StringUtils.isBlank(sourceDateStr)) {
            return false;
        }

        final Instant sourceDate = Instant.parse(sourceDateStr);
        return sourceDate.isAfter(Instant.now());
    }

    /**
     * check pattern.
     *
     * @param target
     *            value
     * @param patterns
     *            pattern check
     * @return string
     */
    private boolean check(final String target, final Pattern... patterns) {
        Assert.notNull(target, "value must not be null");
        String temp = target;
        for (final Pattern pattern : patterns) {
            final Matcher matcher = pattern.matcher(temp);
            temp = matcher.replaceAll("");
        }

        return StringUtils.isEmpty(temp);
    }

    /**
     * Check Age
     * @param value
     * @param minAge
     * @param maxAge
     * @return true if value in between min age and max age
     */
    public boolean checkAge(final String value, final int minAge, final int maxAge) {
        if (!isNumeric(value, false)) return false;
        int age = NumberUtils.toInt(value);
        return age >= minAge && age <= maxAge;
    }
}
