package io.yanmastra.quarkusBase.utils;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

public interface DateTimeUtils {
    String ZONED_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    String DATE_ONLY = "yyyy-MM-dd";
    DateTimeFormatter zonedDtf = DateTimeFormatter.ofPattern(ZONED_DATE_TIME_FORMAT);
    DateFormat displayableDateTime = new SimpleDateFormat("E, dd-MMM-yyyy HH:mm");
    SimpleDateFormat displayableDate = new SimpleDateFormat("E, dd-MMM-yyyy");
    DateFormat utcDateFormat =  new SimpleDateFormat(ZONED_DATE_TIME_FORMAT);
    DateTimeFormatter displayableDateTimeDtf = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy HH:mm");
    DateTimeFormatter displayableDateDtf = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy");
    DateTimeFormatter utcDateDtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC"));
    DateFormat sysDateOnly = new SimpleDateFormat(DATE_ONLY);
    DateTimeFormatter sysDateDtf = DateTimeFormatter.ofPattern(DATE_ONLY);
    Logger logger = Logger.getLogger(DateTimeUtils.class);
    String IS_DATE = "\\d{4}-\\d{2}-\\d{2}"; //"^(19|20)\\d\\d-(0[1-9]|1[012])-([012]\\d|3[01])";
    String IS_DATE_TIME  =
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}Z)$";

    String IS_DATE_TIME1 =
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)$";

    String IS_DATE_TIME2 =
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z)$";

    String IS_DATE_TIME3 =
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+\\d{2}:\\d{2})$";

    String IS_DATE_TIME4 =
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{2}:\\d{2})$";

    String IS_DATE_TIME5 =
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}\\+\\d{2}:\\d{2})$";

    Set<String> IS_DATE_TIMES = Set.of(IS_DATE_TIME, IS_DATE_TIME1, IS_DATE_TIME2, IS_DATE_TIME3, IS_DATE_TIME4, IS_DATE_TIME5);

    Set<String> IS_ZONED_DATE_TIME = Set.of(IS_DATE_TIME, IS_DATE_TIME1, IS_DATE_TIME2);
    Set<String> IS_OFFSET_DATE_TIME = Set.of(IS_DATE_TIME3, IS_DATE_TIME4, IS_DATE_TIME5);

    static String displayDateTime(Date date) {
        try {
            return displayableDateTime.format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String displayDateTime(LocalDateTime date) {
        try {
            return date.format(displayableDateTimeDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String displayDateTime(ZonedDateTime date, ZoneId zone) {
        try {
            return date.format(displayableDateTimeDtf.withZone(zone));
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String displayDateTime(ZonedDateTime date) {
        try {
            return date.format(displayableDateTimeDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String displayDate(Date date, ZoneId zone) {
        try {
            DateFormat df = new SimpleDateFormat(displayableDate.toPattern());
            df.setTimeZone(TimeZone.getTimeZone(zone));
            return df.format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String displayDate(Date date) {
        try {
            return displayableDate.format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String displayDate(LocalDate date) {
        try {
            return date.format(displayableDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String displayDate(ZonedDateTime date) {
        try {
            return date.format(displayableDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String displayDate(ZonedDateTime date, ZoneId zoneId) {
        try {
            return date.format(displayableDateDtf.withZone(zoneId));
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    static String formattedUtcDate(Date date) {
        try {
            utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return utcDateFormat.format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static String formattedUtcDate(LocalDateTime date) {
        try {
            return date.format(utcDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static String formattedUtcDate(ZonedDateTime date) {
        try {
            return date.format(utcDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static String formattedZonedDate(Date date) {
        try {
            return new SimpleDateFormat(ZONED_DATE_TIME_FORMAT).format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static String formattedZonedDate(LocalDateTime date) {
        try {
            return date.format(zonedDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static String formattedZonedDate(ZonedDateTime date) {
        try {
            return date.format(zonedDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static Date fromUtc(String date) {
        try {
            utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return utcDateFormat.parse(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static Date fromDateOnly(String date) {
        try {
            return sysDateOnly.parse(date);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static String toDateOnly(Date date) {
        try {
            return sysDateOnly.format(date);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static LocalDateTime fromUtcToLocal(String date) {
        try {
            return LocalDateTime.parse(date, utcDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static ZonedDateTime fromUtcToZoned(String date) {
        try {
            return ZonedDateTime.parse(date, utcDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static boolean isDate(String date) {
        if (StringUtils.isBlank(date)) return false;
        return date.matches(IS_DATE);
    }

    static TemporalAccessor parseDateTime(String value) {
        try {
            return DateTimeFormatter.ISO_DATE_TIME.parseBest(
                    value,
                    OffsetDateTime::from,
                    ZonedDateTime::from
            );
        }catch (DateTimeException dte) {
            logger.error(dte.getMessage());
            return null;
        }
    }

    static boolean looksLikeDateTime(String v) {
        if (StringUtils.isBlank(v)) return false;
        if (v.length() < 19) return false;
        if (v.length() < 20) return false;
        if (v.charAt(4) != '-' || v.charAt(7) != '-') return false;
        if (v.charAt(10) != 'T') return false;

        char tz = v.charAt(v.length() - 1);
        return tz == 'Z' || v.contains("+") || v.contains("-");
    }

    static LocalDate fromUtcToLocalDate(String date) {
        try {
            return LocalDate.parse(date, utcDateDtf.withZone(ZoneId.systemDefault()));
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static LocalDate toLocalDate(String date) {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date, sysDateDtf.withZone(ZoneId.systemDefault()));
            return localDate;
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }

        try {
            localDate = LocalDate.parse(date, utcDateDtf.withZone(ZoneId.systemDefault()));
            return localDate;
        } catch (Throwable e){
            logger.warn(e.getMessage());
            return null;
        }
    }

    static String toDateOnly(LocalDate localDate) {
        try {
            return sysDateDtf.format(localDate);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    static String toDateOnly(ZonedDateTime dateTime) {
        try {
            return sysDateDtf.format(dateTime);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
