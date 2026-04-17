package com.data.service.core.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class ReturnUrlAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String FAILURE_MESSAGE = "Sign-in failed. Please try again.";
    private static final Logger log = LoggerFactory.getLogger(ReturnUrlAuthenticationFailureHandler.class);
    private final PanelSecurityProperties securityProperties;

    public ReturnUrlAuthenticationFailureHandler(PanelSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String callbackState = request.getParameter("state");
        String callbackCode = request.getParameter("code");
        String sessionId = request.getSession(false) == null ? "<none>" : request.getSession(false).getId();
        String errorCode = "<none>";
        String errorDescription = "<none>";

        if (exception instanceof OAuth2AuthenticationException oauth2AuthenticationException) {
            errorCode = oauth2AuthenticationException.getError().getErrorCode();
            if (oauth2AuthenticationException.getError().getDescription() != null
                    && !oauth2AuthenticationException.getError().getDescription().isBlank()) {
                errorDescription = oauth2AuthenticationException.getError().getDescription();
            }
        }

        log.warn(
                "OIDC authentication failure: errorCode={}, errorDescription={}, exceptionType={}, exceptionMessage={}, sessionId={}, requestedSessionId={}, requestedSessionValid={}, callbackState={}, callbackCodePresent={}, requestUri={}, host={}, xForwardedHost={}, xForwardedProto={}, xForwardedPort={}",
                errorCode,
                errorDescription,
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                sessionId,
                request.getRequestedSessionId(),
                request.isRequestedSessionIdValid(),
                callbackState == null || callbackState.isBlank() ? "<none>" : callbackState,
                callbackCode != null && !callbackCode.isBlank(),
                request.getQueryString() == null || request.getQueryString().isBlank()
                        ? request.getMethod() + " " + request.getRequestURI()
                        : request.getMethod() + " " + request.getRequestURI() + "?" + request.getQueryString(),
                request.getHeader("Host"),
                request.getHeader("X-Forwarded-Host"),
                request.getHeader("X-Forwarded-Proto"),
                request.getHeader("X-Forwarded-Port"),
                exception
        );

        String loginUrl = ReturnUrlSupport.toFrontendRedirectTarget("/auth/login", securityProperties.getFrontendBaseUrl());
        String redirectTarget = UriComponentsBuilder.fromUriString(loginUrl)
                .queryParam("returnUrl", ReturnUrlSupport.resolveAndClear(request))
                .queryParam("error", FAILURE_MESSAGE)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectTarget);
    }
}
