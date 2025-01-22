package io.yanmastra.authorization.utils;

import org.jboss.logging.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {
    private static final DateFormat displayableDateTime = new SimpleDateFormat("E, dd-MMM-yyyy HH:mm");
    private static final DateFormat displayableDate = new SimpleDateFormat("E, dd-MMM-yyyy");
    private static DateFormat utcDate = null;
    private static final DateTimeFormatter displayableDateTimeDtf = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy HH:mm");
    private static final DateTimeFormatter displayableDateDtf = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy");
    private static final DateTimeFormatter utcDateDtf = DateTimeFormatter.ISO_DATE_TIME;
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

    static void initUtcDate(){
        if (utcDate == null) {
            utcDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            utcDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }

    public static String formattedUtcDate(Date date) {
        initUtcDate();
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

    public static Date fromUtc(String date) {
        initUtcDate();
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
}
