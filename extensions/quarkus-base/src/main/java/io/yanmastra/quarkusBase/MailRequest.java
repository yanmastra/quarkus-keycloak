package io.yanmastra.quarkusBase;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MailRequest {
    @JsonProperty("template_code")
    private String templateCode;
    @JsonProperty("from_email")
    private String fromEmail;
    @JsonProperty("from_name")
    private String fromName;
    @JsonProperty("replay_to")
    private String replayTo;
    @JsonProperty("send_to")
    private Set<String> sendTo;

    private MailRequest(){}
}
