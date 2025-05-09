/**
 * ***************************************************
 * * Description :
 * * File        : DateUtils
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.utils;

import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.enums.TimePeriodEnum;
import com.formos.huub.framework.exception.SystemException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.model.DateRange;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    /**
     * check date format.
     *
     * @param target value
     * @param pattern date time pattern
     * @return true if date is valid, else return false
     */
    public static boolean isDate(final String target, final DateTimeFormat pattern) {
        return isDate(target, pattern, false);
    }

    /**
     * check date format.
     *
     * @param target value
     * @param pattern date time pattern
     * @param strict specify date is matching
     * @return true if date is valid, else return false
     */
    public static boolean isDate(final String target, final DateTimeFormat pattern, final boolean strict) {
        boolean ret = true;

        if (pattern == null) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
        }

        if (StringUtils.isNotBlank(target)) {
            try {
                final DateFormat dateFormat = new SimpleDateFormat(pattern.getValue());
                final Date date = dateFormat.parse(target);

                String value = dateFormat.format(date);
                if (strict && !target.equals(value)) {
                    ret = false;
                }
            } catch (final ParseException e) {
                ret = false;
            }
        }

        return ret;
    }

    /**
     * check the consistency of the time string.
     *
     * @param target value
     * @param timeFormat date time pattern
     * @return true if time is valid else return false
     */
    public static boolean isTime(final String target, final DateTimeFormat timeFormat) {
        boolean ret = true;

        if (timeFormat == null) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
        }

        if (StringUtils.isNotBlank(target)) {
            DateTimeFormatter timeFormatter;
            try {
                timeFormatter = DateTimeFormatter.ofPattern(timeFormat.getValue()).withResolverStyle(ResolverStyle.LENIENT);
            } catch (final Exception e) {
                throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
            }
            try {
                LocalTime.parse(target, timeFormatter);
            } catch (final DateTimeParseException e) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * convert LocalDateTime object to String.
     *
     * @param target dateTime : LocalDateTime object to convert
     * @param pattern pattern : time format of target character string
     * @return string
     */
    public static String convertDateTimeToString(final LocalDateTime target, final DateTimeFormat pattern) {
        String result;
        if (pattern == null) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
        }

        DateTimeFormatter dateTimeFormatter;
        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(pattern.getValue());
        } catch (final Exception e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        try {
            result = target.format(dateTimeFormatter);
        } catch (final DateTimeException e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        return result;
    }

    /**
     * convert LocalDate object to String.
     *
     * @param target value
     * @param pattern date time pattern
     * @return string
     */
    public static String convertDateToString(final LocalDate target, final DateTimeFormat pattern) {
        String result;
        if (pattern == null) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
        }

        DateTimeFormatter dateTimeFormatter;
        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(pattern.getValue());
        } catch (final Exception e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        try {
            result = target.format(dateTimeFormatter);
        } catch (final DateTimeException e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        return result;
    }

    public static String convertDateTimeToString(final LocalDate target, final DateTimeFormat pattern) {
        String result;
        if (pattern == null) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
        }

        DateTimeFormatter dateTimeFormatter;
        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(pattern.getValue());
        } catch (final Exception e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        try {
            result = target.format(dateTimeFormatter);
        } catch (final DateTimeException e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        return result;
    }

    /**
     * convert String object to LocalDate.
     *
     * @param target value
     * @param pattern date time pattern
     * @return string
     */
    public static LocalDate convertStringToLocalDate(final String target, final DateTimeFormat pattern) {
        LocalDate localDate;
        if (pattern == null) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
        }

        DateTimeFormatter dateTimeFormatter = null;
        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(pattern.getValue());
        } catch (final Exception e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        try {
            localDate = LocalDate.parse(target, dateTimeFormatter);
        } catch (final DateTimeParseException e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        return localDate;
    }

    /**
     * convert String object to LocalDateTime.
     *
     * @param target value
     * @param pattern date time pattern
     * @return string
     */
    public static LocalDateTime convertStringToDateTime(final String target, final DateTimeFormat pattern) {
        LocalDateTime localDate;
        if (pattern == null) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
        }

        DateTimeFormatter dateTimeFormatter;
        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(pattern.getValue());
        } catch (final Exception e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        try {
            localDate = LocalDateTime.parse(target, dateTimeFormatter);
        } catch (final DateTimeParseException e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001), e);
        }

        return localDate;
    }

    /**
     * convert String to java.util.Date
     *
     * @param target value
     * @param pattern date time pattern
     * @return string
     */
    public static Date convertStringToDate(final String target, final DateTimeFormat pattern) {
        if (StringUtils.isBlank(target)) {
            return null;
        }

        Date result;
        try {
            result = new SimpleDateFormat(pattern.getValue()).parse(target);
        } catch (final ParseException e) {
            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
        }

        return result;
    }

    /**
     * convert String To Instant
     * @param target value
     * @param pattern pattern
     * @return Instant
     */
    public static Instant convertStringToInstant(final String target, final DateTimeFormat pattern) {
        Date date = convertStringToDate(target, pattern);

        if (date != null) {
            return date.toInstant();
        }
        return null;
    }

    /**
     * convert String To Instant
     * @param utcString String
     * @return Instant
     */
    public static Instant convertUtcStringToInstant(String utcString) {
        try {
            return Instant.parse(utcString);
        } catch (Exception e) {
            //            throw new SystemException(MessageHelper.getMessage(Message.Keys.E0001));
            return null;
        }
    }

    /**
     * convert instant to string.
     *
     * @param target value
     * @return string
     */
    public static String convertInstantToString(final Instant target) {
        return convertLocalDate(target).toString();
    }

    public static String convertInstantToString(final Instant target, final DateTimeFormat toPattern) {
        return convertDateToString(convertLocalDate(target), toPattern);
    }

    public static String convertInstantToStringTime(final Instant target, final DateTimeFormat toPattern) {
        return convertDateTimeToString(convertLocalDateTime(target), toPattern);
    }

    public static String convertInstantToStringTime(final Instant target, final DateTimeFormat toPattern, final String timeZone) {
        return convertDateTimeToString(target.atZone(getTimeZoneFromString(timeZone)).toLocalDateTime(), toPattern);
    }

    /**
     * get last date of month.
     *
     * @param target value
     * @param frmPattern from pattern
     * @param toPattern to pattern
     * @return string
     */
    public static String getLastDateOfMonth(final String target, final DateTimeFormat frmPattern, final DateTimeFormat toPattern) {
        final Date date = convertStringToDate(target, frmPattern);

        if (date == null) {
            return null;
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));

        return convertDateToString(calendar.getTime(), toPattern);
    }

    /**
     * convert date to string.
     *
     * @param target value
     * @param pattern date time pattern
     * @return string
     */
    public static String convertDateToString(final Date target, final DateTimeFormat pattern) {
        return new SimpleDateFormat(pattern.getValue()).format(target.getTime());
    }

    public static String convertDateToEnglishString(final Instant target, final DateTimeFormat pattern) {
        return target.atZone(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern(pattern.getValue(), Locale.ENGLISH));
    }

    /**
     * convert date time format.
     *
     * @param target value
     * @param frmPattern from pattern
     * @param toPattern to pattern
     * @return string
     */
    public static String changeDateTimeFormat(final String target, final DateTimeFormat frmPattern, final DateTimeFormat toPattern) {
        final Date date = convertStringToDate(target, frmPattern);

        if (date == null) {
            return null;
        }

        return convertDateToString(date, toPattern);
    }

    /**
     * convert date to local date.
     *
     * @param date date
     * @return LocalDate
     */
    public static LocalDate convertLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * convert Instant to local date.cons
     *
     * @param instant Instant
     * @return LocalDate
     */
    public static LocalDate convertLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime convertLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * compare date.
     *
     * @param fromDate from date
     * @param toDate to date
     * @return boolean
     */
    public static boolean compareDate(final String fromDate, final String toDate) {
        return compareDate(
            convertStringToLocalDate(fromDate, DateTimeFormat.SLASH_YYYYMMDD),
            convertStringToLocalDate(toDate, DateTimeFormat.SLASH_YYYYMMDD)
        );
    }

    /**
     * compare Instant.
     * @param fromInstant
     * @param toInstant
     * @return
     */
    public static boolean compareInstant(final Instant fromInstant, final Instant toInstant) {
        return ((fromInstant.compareTo(toInstant) < 0) || (fromInstant.compareTo(toInstant) == 0));
    }

    /**
     * compare date.
     *
     * @param fromDate from date
     * @param toDate to date
     * @return boolean
     */

    public static boolean compareDate(final LocalDate fromDate, final LocalDate toDate) {
        return ((fromDate.compareTo(toDate) < 0) || (fromDate.compareTo(toDate) == 0));
    }

    /**
     * Find the current age of members by birthday
     *
     * @param birthday
     * @return
     */
    public static int getCurrentAgeByBirthday(Instant birthday) {
        if (ObjectUtils.isEmpty(birthday)) {
            return 0;
        }
        LocalDate birthdate = birthday.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        return Period.between(birthdate, today).getYears();
    }

    public static boolean isBeforeOrEqual(Instant before, Instant after) {
        return before.isBefore(after) || before.equals(after);
    }

    public static boolean isBeforeOrEqualDate(Instant before, Instant after) {
        var mBefore = before.truncatedTo(ChronoUnit.DAYS);
        var mAfter = after.truncatedTo(ChronoUnit.DAYS);
        return mBefore.isBefore(mAfter) || mBefore.equals(mAfter);
    }

    public static String convertDateToCron(LocalDateTime localDateTime) {
        return String.format(
            "%s %s %s %s %s %s %s",
            localDateTime.getSecond(),
            localDateTime.getMinute(),
            localDateTime.getHour(),
            localDateTime.getDayOfMonth(),
            localDateTime.getMonth(),
            "?",
            localDateTime.getYear()
        );
    }

    /**
     * Get date from instant.
     * @param zoneId zoneId
     * @param instant instant
     * @return LocalDate
     */
    public static LocalDate getDateFromInstant(final ZoneId zoneId, final Instant instant) {
        return instant.atZone(zoneId).toLocalDate();
    }

    public static LocalTime getTimeFromInstant(final ZoneId zoneId, final Instant instant) {
        return instant.atZone(zoneId).toLocalTime().truncatedTo(ChronoUnit.SECONDS);
    }

    public static ZoneId getTimeZone(String timeZone) {
        return TimeZone.getTimeZone(ZoneId.SHORT_IDS.get(timeZone)).toZoneId();
    }

    public static ZoneId getTimeZoneFromString(String timeZone) {
        return TimeZone.getTimeZone(timeZone).toZoneId();
    }

    public static String formatDateWithTimeZone(Instant instant, String timeZone) {
        return formatDate(instant, DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YYYY.getValue()), timeZone);
    }

    public static String formatTimeWithTimeZone(Instant instant, String timeZone) {
        return formatTime(instant, DateTimeFormatter.ofPattern(DateTimeFormat.HH_MM_AM_PM.getValue()), timeZone);
    }

    public static String formatDateTimeWithTimeZone(Instant instant, String timeZone) {
        return formatDateTime(instant, DateTimeFormatter.ofPattern(DateTimeFormat.MM_dd_yyyy_hh_mm_a.getValue()), timeZone);
    }

    public static String formatDate(Instant instant, DateTimeFormatter formatter, String timeZone) {
        return instant.atZone(getTimeZone(timeZone)).toLocalDate().format(formatter);
    }

    public static String formatTime(Instant instant, DateTimeFormatter formatter, String timeZone) {
        return instant.atZone(DateUtils.getTimeZone(timeZone)).toLocalTime().format(formatter);
    }

    public static String formatDateTime(Instant instant, DateTimeFormatter formatter, String timeZone) {
        return StringUtils.makeDateTimeWithTimeZone(instant.atZone(DateUtils.getTimeZone(timeZone)).toLocalDateTime().format(formatter));
    }

    public static DateRange calculateDateRangeForPeriod(Instant inputDate, TimePeriodEnum period, String timeZone) {
        ZoneId zone = DateUtils.getTimeZone(timeZone);
        LocalDateTime startDate = inputDate.atZone(zone).toLocalDateTime();
        LocalDateTime endDate = inputDate.atZone(zone).toLocalDateTime();
        switch (period) {
            case WEEKLY:
                startDate = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)); // Start date is the previous or same Monday
                endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)); // End date is the next or same Saturday
                break;
            case MONTHLY:
                startDate = startDate.with(TemporalAdjusters.firstDayOfMonth()); // Start date is the first day of the month
                endDate = startDate.with(TemporalAdjusters.lastDayOfMonth()); // End date is the last day of the month
                break;
            case YEARLY:
                startDate = startDate.with(TemporalAdjusters.firstDayOfYear()); // Start date is the first day of the year
                endDate = startDate.with(TemporalAdjusters.lastDayOfYear()); // End date is the last day of the year
                break;
            case DAILY:
                break;
            default:
                throw new IllegalArgumentException("Invalid period enum value");
        }
        Instant startInstant = startDate.atZone(zone).toInstant();
        Instant endInstant = endDate.atZone(zone).toInstant();
        return new DateRange(startInstant, endInstant);
    }

    public static Instant getFirstDateOfMonth(Instant inputDate, String timeZone) {
        ZoneId zone = DateUtils.getTimeZone(timeZone);
        LocalDateTime startDate = inputDate.atZone(zone).toLocalDateTime();
        startDate = startDate.with(TemporalAdjusters.firstDayOfMonth()); // Start date is the first day of the month
        return startDate.atZone(zone).toInstant();
    }

    public static Instant convertTimeToUTC(String eventDate, String localTime, String timezone) {
        if (StringUtils.isBlank(eventDate)) {
            return null;
        }
        return convertTimeToUTC(Instant.parse(eventDate), LocalTime.parse(localTime), timezone);
    }

    public static Instant convertTimeToUTC(Instant eventDate, LocalTime localTime, String timezone) {
        // Create LocalDateTime directly from eventDate and localTime
        LocalDateTime localDateTime = eventDate.atZone(ZoneOffset.UTC).toLocalDate().atTime(localTime);

        ZoneId zoneOffset = getTimeZoneFromString(timezone);
        // Convert to Instant with UTC+0 time zone offset
        return localDateTime.atZone(zoneOffset).toInstant();
    }

    public static Instant convertTimeToUTC(String eventDate, String timezone) {
        if (StringUtils.isBlank(eventDate)) {
            return null;
        }
        return convertTimeToUTC(Instant.parse(eventDate), timezone);
    }

    public static Instant convertTimeToUTC(Instant eventDate, String timezone) {
        // Create LocalDateTime directly from eventDate and localTime
        LocalDateTime localDateTime = LocalDateTime.ofInstant(eventDate, ZoneOffset.UTC);

        ZoneId zoneOffset = getTimeZoneFromString(timezone);
        // Convert to Instant with UTC+0 time zone offset
        return localDateTime.atZone(zoneOffset).toInstant();
    }

    public static boolean checkDateValid(String startDate, String endDate) {
        Instant startDateIns = Instant.parse(startDate);
        Instant endDateIns = Instant.parse(endDate);
        return startDateIns.isBefore(endDateIns) || startDateIns.equals(endDateIns);
    }

    public static int getDateOfMonthUTC(Instant date) {
        return getDateOfMonth(date, ZoneOffset.UTC);
    }

    public static int getDateOfMonth(Instant date, ZoneOffset zone) {
        return date.atZone(zone).getDayOfMonth();
    }

    public static String getTimeZoneString(String zoneName) {
        ZoneId zone = ZoneId.of(zoneName);
        ZonedDateTime zdt = ZonedDateTime.now(zone);
        return zdt.getOffset().toString();
    }

    public static String convertInstantToEnglishString(Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        Month month = localDateTime.getMonth();

        return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public static String convertTimeToUtcToString(String dateStr, String timezone) {
        LocalDate localDate = LocalDate.parse(dateStr);
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(zoneId);
        ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        return utcDateTime.format(DateTimeFormatter.ofPattern(DateTimeFormat.YYYY_MM_DD.getValue()));
    }


    public static ZonedDateTime combineDateTimeWithZone(Instant date, String timezone) {
        if (date == null || timezone == null || timezone.isEmpty()) {
            throw new IllegalArgumentException("Input parameters must not be null or empty.");
        }
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime specialDateTime = ZonedDateTime.ofInstant(date, ZoneId.of("UTC"));
        specialDateTime = specialDateTime.withZoneSameInstant(zoneId);
        return specialDateTime;
    }

    public static long compareToSpecialDateTime(Instant date, String timezone) {
        if (date == null || timezone == null || timezone.isEmpty()) {
            throw new IllegalArgumentException("Input parameters must not be null or empty.");
        }
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime specialDateTime = combineDateTimeWithZone(date, timezone);
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        if (currentDateTime.isAfter(specialDateTime)) {
            return 0;
        }
        return ChronoUnit.MILLIS.between(currentDateTime, specialDateTime);
    }

    public static long hoursToMillis(int hours) {
        return hours * 60 * 60 * 1000L;
    }
}
