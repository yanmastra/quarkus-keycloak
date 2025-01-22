package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImpersonatePayload {
    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    @JsonProperty("subject_token")
    private String subjectToken;
    @JsonProperty("requested_subject")
    private String requestSubject;

    public ImpersonatePayload() {
    }

    public ImpersonatePayload(String grantType, String clientId, String clientSecret, String subjectToken, String requestSubject) {
        this.grantType = grantType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.subjectToken = subjectToken;
        this.requestSubject = requestSubject;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getSubjectToken() {
        return subjectToken;
    }

    public void setSubjectToken(String subjectToken) {
        this.subjectToken = subjectToken;
    }

    public String getRequestSubject() {
        return requestSubject;
    }

    public void setRequestSubject(String requestSubject) {
        this.requestSubject = requestSubject;
    }

    @Override
    public String toString() {
        return "ImpersonatePayload{" +
                "grantType='" + grantType + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", subjectToken='" + subjectToken + '\'' +
                ", requestSubject='" + requestSubject + '\'' +
                '}';
    }
}
