<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true displayMessage=!messagesPerField.existsError('username'); section>
    <#if section = "header">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">

        <div id="kc-logo">
            <img src="${url.resourcesPath}/img/logo.png" alt="${realm.displayName!'Application'}" onerror="this.style.display='none'">
        </div>

        <h1 id="kc-page-title">${msg("emailForgotTitle")}</h1>
        <p class="kc-page-subtitle">${msg("emailInstruction"!"Enter your email and we'll send you a reset link.")}</p>

        <form id="kc-reset-password-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!} <#if messagesPerField.existsError('username')>has-error</#if>">
                <label for="username" class="${properties.kcLabelClass!}">
                    <#if !realm.loginWithEmailAllowed>${msg("username")}
                    <#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}
                    <#else>${msg("email")}
                    </#if>
                </label>
                <input type="text" id="username" name="username" class="${properties.kcInputClass!}" autofocus
                       value="${(auth.attemptedUsername!'')}"
                       placeholder="<#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>"
                       aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"/>
                <#if messagesPerField.existsError('username')>
                    <span id="input-error-username" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                        ${kcSanitize(messagesPerField.getFirstError('username'))?no_esc}
                    </span>
                </#if>
            </div>

            <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                       type="submit" value="${msg("doSubmit")}"/>
            </div>
        </form>

        <div id="kc-info-message">
            <a href="${url.loginUrl}">&larr; ${msg("backToLogin")}</a>
        </div>

    <#elseif section = "info" >
        <p class="instruction">${msg("emailInstruction")}</p>
    </#if>
</@layout.registrationLayout>
