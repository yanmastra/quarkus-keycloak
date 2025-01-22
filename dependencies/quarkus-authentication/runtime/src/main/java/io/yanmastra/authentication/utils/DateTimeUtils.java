package io.yanmastra.authentication.utils;

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

public class DateTimeUtils {
    public static final String ZONED_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final DateTimeFormatter zonedDtf = DateTimeFormatter.ofPattern(ZONED_DATE_TIME_FORMAT);
    private static final DateFormat displayableDateTime = new SimpleDateFormat("E, dd-MMM-yyyy HH:mm");
    private static final DateFormat displayableDate = new SimpleDateFormat("E, dd-MMM-yyyy");
    private static final DateFormat utcDate;
    static {
        utcDate = new SimpleDateFormat(ZONED_DATE_TIME_FORMAT);
        utcDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private static final DateTimeFormatter displayableDateTimeDtf = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy HH:mm");
    private static final DateTimeFormatter displayableDateDtf = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy");
    private static final DateTimeFormatter utcDateDtf = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC"));
    private static final Logger logger = Logger.getLogger(DateTimeUtils.class);

    public static String displayDateTime(Date date) {
        try {
            return displayableDateTime.format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    public static String displayDateTime(LocalDateTime date) {
        try {
            return date.format(displayableDateTimeDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    public static String displayDateTime(ZonedDateTime date) {
        try {
            return date.format(displayableDateTimeDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    public static String displayDate(Date date) {
        try {
            return displayableDate.format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    public static String displayDate(LocalDate date) {
        try {
            return date.format(displayableDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    public static String displayDate(ZonedDateTime date) {
        try {
            return date.format(displayableDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return "Incorrect date";
        }
    }

    public static String formattedUtcDate(Date date) {
        try {
            return utcDate.format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String formattedUtcDate(LocalDateTime date) {
        try {
            return date.format(utcDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String formattedUtcDate(ZonedDateTime date) {
        try {
            return date.format(utcDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String formattedZonedDate(Date date) {
        try {
            return new SimpleDateFormat(ZONED_DATE_TIME_FORMAT).format(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String formattedZonedDate(LocalDateTime date) {
        try {
            return date.format(zonedDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String formattedZonedDate(ZonedDateTime date) {
        try {
            return date.format(zonedDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static Date fromUtc(String date) {
        try {
            return utcDate.parse(date);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static LocalDateTime fromUtcToLocal(String date) {
        try {
            return LocalDateTime.parse(date, utcDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static ZonedDateTime fromUtcToZoned(String date) {
        try {
            return ZonedDateTime.parse(date, utcDateDtf);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
