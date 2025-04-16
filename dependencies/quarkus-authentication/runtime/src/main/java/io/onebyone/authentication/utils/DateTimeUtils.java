package io.onebyone.authentication.utils;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public interface DateTimeUtils {
    String ZONED_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    String DATE_ONLY = "yyyy-MM-dd";
    DateTimeFormatter zonedDtf = DateTimeFormatter.ofPattern(ZONED_DATE_TIME_FORMAT);
    DateFormat displayableDateTime = new SimpleDateFormat("E, dd-MMM-yyyy HH:mm");
    DateFormat displayableDate = new SimpleDateFormat("E, dd-MMM-yyyy");
    DateFormat utcDateFormat =  new SimpleDateFormat(ZONED_DATE_TIME_FORMAT);
    DateTimeFormatter displayableDateTimeDtf = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy HH:mm");
    DateTimeFormatter displayableDateDtf = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy");
    DateTimeFormatter utcDateDtf = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC"));
    DateFormat sysDateOnly = new SimpleDateFormat(DATE_ONLY);
    Logger logger = Logger.getLogger(DateTimeUtils.class);
    String IS_DATE = "\\d{4}-\\d{2}-\\d{2}"; //"^(19|20)\\d\\d-(0[1-9]|1[012])-([012]\\d|3[01])";
    String IS_DATE_TIME = "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}Z)$";
    String IS_DATE_TIME2 = "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z)$";
    String IS_DATE_TIME1 = "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)$";
    String IS_DATE_TIME3 = "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}+\\d{2}:\\d{2})$";
    String IS_DATE_TIME4 = "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}+\\d{2}:\\d{2})$";
    String IS_DATE_TIME5 = "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}+\\d{2}:\\d{2})$";

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

    static String displayDateTime(ZonedDateTime date) {
        try {
            return date.format(displayableDateTimeDtf);
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
}
