package io.yanmastra.authorization.logging;

import java.time.ZonedDateTime;

public class RequestLogData {
    public String userAgent;
    public String method;
    public String uri;
    public String ipAddress;
    public String principalName;
    public int status;
    public ZonedDateTime timestamp;
}
