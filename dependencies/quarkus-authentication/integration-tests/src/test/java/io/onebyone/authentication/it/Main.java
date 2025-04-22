package io.onebyone.authentication.it;

import io.onebyone.authentication.utils.DateTimeUtils;

public class Main {
    public static void main(String[] args) {
        checkDate();
    }

    private static void checkDate() {
        String dateTime = "2025-03-31T01:32:58+08:00";
        String dateTime1 = "2025-03-31T01:32:58.0+08:00";
        String dateTime2 = "2025-03-31T01:32:58+08:00";
        String dateTime3 = "2025-03-31T01:32:58+08:00";

        boolean isDateTime = DateTimeUtils.isDate(dateTime);
        System.out.println("isDateTime:"+isDateTime);
        isDateTime = DateTimeUtils.isDate(dateTime);
        System.out.println("isDate:"+isDateTime);
    }
}
