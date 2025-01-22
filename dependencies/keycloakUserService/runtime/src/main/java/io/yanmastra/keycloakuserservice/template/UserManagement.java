package io.yanmastra.keycloakuserservice.template;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

import java.util.Map;

@CheckedTemplate(basePath = "UserManagement")
public class UserManagement {
    public static native TemplateInstance userConfirmationMail(Map<String, String> data);
    public static native TemplateInstance userResetPasswordMail(Map<String, String> data);
    public static native TemplateInstance resetPasswordPage(Map<String, String> data);
}
