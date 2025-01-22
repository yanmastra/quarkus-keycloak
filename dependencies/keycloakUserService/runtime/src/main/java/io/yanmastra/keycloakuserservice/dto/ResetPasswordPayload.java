package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class ResetPasswordPayload {
    @JsonProperty("new_password")
    public String newPassword;
    @JsonProperty("confirm_password")
    public String confirmPassword;

    public boolean validate() {
        return StringUtils.isNotBlank(newPassword) && StringUtils.isNotBlank(confirmPassword) &&
                newPassword.equals(confirmPassword);
    }
}
