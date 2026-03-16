<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('password','password-confirm'); section>
    <#if section = "header">
        ${msg("updatePasswordTitle")}
    <#elseif section = "form">

        <div id="kc-logo">
            <img src="${url.resourcesPath}/img/logo.png" alt="${realm.displayName!'Application'}" onerror="this.style.display='none'">
        </div>

        <h1 id="kc-page-title">${msg("updatePasswordTitle")}</h1>
        <p class="kc-page-subtitle">Choose a strong, unique password for your account.</p>

        <form id="kc-passwd-update-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">

            <input type="text" id="username" name="username" value="${username}" autocomplete="username"
                   readonly="readonly" style="display:none;"/>

            <div class="${properties.kcFormGroupClass!} <#if messagesPerField.existsError('password')>has-error</#if>">
                <label for="password-new" class="${properties.kcLabelClass!}">${msg("passwordNew")}</label>
                <input type="password" id="password-new" name="password-new"
                       class="${properties.kcInputClass!}"
                       autofocus autocomplete="new-password"
                       placeholder="${msg("passwordNew")}"
                       aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"/>
                <#if messagesPerField.existsError('password')>
                    <span id="input-error-password" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                        ${kcSanitize(messagesPerField.getFirstError('password'))?no_esc}
                    </span>
                </#if>
            </div>

            <div class="${properties.kcFormGroupClass!} <#if messagesPerField.existsError('password-confirm')>has-error</#if>">
                <label for="password-confirm" class="${properties.kcLabelClass!}">${msg("passwordConfirm")}</label>
                <input type="password" id="password-confirm" name="password-confirm"
                       class="${properties.kcInputClass!}"
                       autocomplete="new-password"
                       placeholder="${msg("passwordConfirm")}"
                       aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"/>
                <#if messagesPerField.existsError('password-confirm')>
                    <span id="input-error-password-confirm" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                        ${kcSanitize(messagesPerField.getFirstError('password-confirm'))?no_esc}
                    </span>
                </#if>
            </div>

            <!-- Password strength indicator -->
            <div id="kc-password-strength" style="margin-bottom:16px;">
                <div style="height:4px;border-radius:4px;background:#e2e2ef;overflow:hidden;margin-bottom:6px;">
                    <div id="kc-strength-bar" style="height:100%;width:0%;border-radius:4px;transition:width 0.3s,background 0.3s;background:#e74c3c;"></div>
                </div>
                <span id="kc-strength-label" style="font-size:0.8rem;color:#9999ab;font-weight:500;"></span>
            </div>

            <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                <#if isAppInitiatedAction??>
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" value="${msg("doSubmit")}" />
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                            type="submit" name="cancel-aia" value="true">${msg("doCancel")}</button>
                <#else>
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" value="${msg("doSubmit")}"/>
                </#if>
            </div>
        </form>

        <script>
        (function () {
          var input = document.getElementById('password-new');
          var bar   = document.getElementById('kc-strength-bar');
          var label = document.getElementById('kc-strength-label');
          if (!input) return;

          function score(pw) {
            var s = 0;
            if (pw.length >= 8)  s++;
            if (pw.length >= 12) s++;
            if (/[A-Z]/.test(pw)) s++;
            if (/[a-z]/.test(pw)) s++;
            if (/[0-9]/.test(pw)) s++;
            if (/[^A-Za-z0-9]/.test(pw)) s++;
            return s;
          }

          input.addEventListener('input', function () {
            var pw = input.value;
            if (!pw) { bar.style.width = '0%'; label.textContent = ''; return; }
            var s = score(pw);
            var pct   = Math.min(100, Math.round(s / 6 * 100));
            var color = s <= 2 ? '#e74c3c' : s <= 4 ? '#f39c12' : '#2ecc71';
            var text  = s <= 2 ? 'Weak' : s <= 4 ? 'Moderate' : 'Strong';
            bar.style.width = pct + '%';
            bar.style.background = color;
            label.style.color = color;
            label.textContent = text;
          });
        }());
        </script>

    </#if>
</@layout.registrationLayout>
