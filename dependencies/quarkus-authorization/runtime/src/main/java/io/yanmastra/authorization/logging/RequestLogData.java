package io.yanmastra.authorization.logging;

import java.time.ZonedDateTime;

public class RequestLogData {
    public String userAgent;
    public String method;
    public String uri;
    public String ipAddress;
    public String principalName;
    public Integer status;
    public ZonedDateTime timestamp;
    public String requestPayload;

    public RequestLogData() {}

    @Override
    public String toString() {
        return "RequestLogData{" +
                "userAgent='" + userAgent + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", principalName='" + principalName + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", requestPayload='" + requestPayload + '\'' +
                '}';
    }
}
