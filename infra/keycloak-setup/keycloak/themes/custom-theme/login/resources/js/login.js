/* =============================================================================
   Custom Keycloak Theme — Login JS
   Handles: password visibility toggle, form loading states, input enhancements
   ============================================================================= */

(function () {
  'use strict';

  // -------------------------------------------------------------------------
  // Password visibility toggle
  // -------------------------------------------------------------------------
  function setupPasswordToggle() {
    document.querySelectorAll('input[type="password"]').forEach(function (input) {
      var wrapper = document.createElement('div');
      wrapper.className = 'password-field-container';
      input.parentNode.insertBefore(wrapper, input);
      wrapper.appendChild(input);

      var toggle = document.createElement('button');
      toggle.type = 'button';
      toggle.setAttribute('aria-label', 'Toggle password visibility');
      toggle.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>';
      toggle.style.cssText = [
        'position:absolute', 'right:14px', 'top:50%', 'transform:translateY(-50%)',
        'background:none', 'border:none', 'cursor:pointer',
        'color:#9999ab', 'padding:4px', 'display:flex',
        'align-items:center', 'transition:color 0.2s'
      ].join(';');

      wrapper.style.position = 'relative';
      wrapper.appendChild(toggle);

      toggle.addEventListener('click', function () {
        var isHidden = input.type === 'password';
        input.type = isHidden ? 'text' : 'password';
        toggle.style.color = isHidden ? '#4B49AC' : '#9999ab';
        toggle.setAttribute('aria-pressed', String(isHidden));
      });

      toggle.addEventListener('mouseenter', function () {
        if (input.type === 'password') toggle.style.color = '#4B49AC';
      });
      toggle.addEventListener('mouseleave', function () {
        if (input.type === 'password') toggle.style.color = '#9999ab';
      });
    });
  }

  // -------------------------------------------------------------------------
  // Submit button loading state
  // -------------------------------------------------------------------------
  function setupLoadingState() {
    var forms = document.querySelectorAll('form');
    forms.forEach(function (form) {
      form.addEventListener('submit', function () {
        var submitBtn = form.querySelector('input[type="submit"], button[type="submit"]');
        if (!submitBtn) return;
        setTimeout(function () {
          var originalText = submitBtn.value || submitBtn.textContent;
          submitBtn.disabled = true;
          if (submitBtn.tagName === 'INPUT') {
            submitBtn.value = 'Please wait…';
          } else {
            submitBtn.innerHTML = '<span class="kc-loading-spinner"></span> Please wait…';
          }
          // Re-enable after 8s as a safety fallback
          setTimeout(function () {
            submitBtn.disabled = false;
            if (submitBtn.tagName === 'INPUT') {
              submitBtn.value = originalText;
            } else {
              submitBtn.textContent = originalText;
            }
          }, 8000);
        }, 0);
      });
    });
  }

  // -------------------------------------------------------------------------
  // Auto-focus first visible empty input
  // -------------------------------------------------------------------------
  function setupAutoFocus() {
    var inputs = document.querySelectorAll('input[type="text"], input[type="email"], input[type="password"]');
    for (var i = 0; i < inputs.length; i++) {
      if (!inputs[i].value && inputs[i].offsetParent !== null) {
        inputs[i].focus();
        break;
      }
    }
  }

  // -------------------------------------------------------------------------
  // OTP input formatting (spaces every 3 digits for readability)
  // -------------------------------------------------------------------------
  function setupOtpInput() {
    var otp = document.getElementById('otp');
    if (!otp) return;
    otp.setAttribute('maxlength', '6');
    otp.setAttribute('inputmode', 'numeric');
    otp.setAttribute('autocomplete', 'one-time-code');
    otp.addEventListener('input', function () {
      this.value = this.value.replace(/\D/g, '').slice(0, 6);
    });
  }

  // -------------------------------------------------------------------------
  // Dismiss alerts on click
  // -------------------------------------------------------------------------
  function setupAlertDismiss() {
    document.querySelectorAll('.alert').forEach(function (alert) {
      alert.style.cursor = 'pointer';
      alert.title = 'Click to dismiss';
      alert.addEventListener('click', function () {
        alert.style.opacity = '0';
        alert.style.transform = 'translateY(-6px)';
        alert.style.transition = 'opacity 0.25s ease, transform 0.25s ease';
        setTimeout(function () { alert.remove(); }, 300);
      });
    });
  }

  // -------------------------------------------------------------------------
  // Init
  // -------------------------------------------------------------------------
  document.addEventListener('DOMContentLoaded', function () {
    setupPasswordToggle();
    setupLoadingState();
    setupAutoFocus();
    setupOtpInput();
    setupAlertDismiss();
  });
}());
