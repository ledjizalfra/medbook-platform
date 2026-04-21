<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<@layout.registrationLayout
    displayMessage=!messagesPerField.existsError('username','password')
    displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??
    bodyClass="login-pf"
    additionalStylesheets=["${url.resourcesPath}/css/medbook.css"]>

    <#-- Titolo pagina -->
    <#if section = "header">
        ${msg("loginAccountTitle")}

    <#-- Form di login -->
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">

                <#if realm.password>
                    <form id="kc-form-login" onsubmit="login.disabled = true; return true;"
                          action="${url.loginAction}" method="post">

                        <#-- Username / Email -->
                        <@field.group name="username" label=msg("usernameOrEmail")>
                            <@field.input name="username"
                                          value=login.username!''
                                          type="text"
                                          autocomplete="username"
                                          autofocus=true
                                          aria-invalid=messagesPerField.existsError('username','password')/>
                        </@field.group>

                        <#-- Password -->
                        <@field.group name="password" label=msg("password")>
                            <@field.passwordWrapper>
                                <@field.input name="password"
                                              type="password"
                                              autocomplete="current-password"
                                              aria-invalid=messagesPerField.existsError('username','password')/>
                            </@field.passwordWrapper>
                        </@field.group>

                        <#-- Ricordami + Password dimenticata -->
                        <div class="${properties.kcFormGroupClass!}">
                            <#if realm.rememberMe && !usernameEditDisabled??>
                                <div class="checkbox">
                                    <label>
                                        <#if login.rememberMe??>
                                            <input tabindex="5" id="rememberMe" name="rememberMe"
                                                   type="checkbox" checked> ${msg("rememberMe")}
                                        <#else>
                                            <input tabindex="5" id="rememberMe" name="rememberMe"
                                                   type="checkbox"> ${msg("rememberMe")}
                                        </#if>
                                    </label>
                                </div>
                            </#if>

                            <#if realm.resetPasswordAllowed>
                                <div class="${properties.kcFormOptionsWrapperClass!}">
                                    <span>
                                        <a tabindex="6" href="${url.loginResetCredentialsUrl}">
                                            ${msg("doForgotPassword")}
                                        </a>
                                    </span>
                                </div>
                            </#if>
                        </div>

                        <#-- Pulsante login -->
                        <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                            <input type="hidden" id="id-hidden-input" name="credentialId"
                                   <#if auth.selectedCredential?has_content>
                                       value="${auth.selectedCredential}"
                                   </#if>/>
                            <@buttons.loginButton/>
                        </div>

                        <#-- Link: Torna alla landing page -->
                        <a href="${properties.landingPageUrl}" class="medbook-back-link">
                            ${msg("backToHome")}
                        </a>

                    </form>
                </#if>

            </div>
        </div>

    <#-- Link registrazione (sotto il form) -->
    <#elseif section = "info">
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span>${msg("noAccount")}
                        <a tabindex="8" href="${url.registrationUrl}">${msg("doRegister")}</a>
                    </span>
                </div>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
