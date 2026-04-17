package com.data.service.core.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.Arrays;
import java.util.stream.Collectors;

class LoggingAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger log = LoggerFactory.getLogger(LoggingAuthorizationRequestRepository.class);

    private final HttpSessionOAuth2AuthorizationRequestRepository delegate =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.loadAuthorizationRequest(request);

        log.info(
                "OIDC auth request load: sessionId={}, requestedSessionId={}, requestedSessionValid={}, callbackState={}, savedState={}, requestUri={}, cookies={}, host={}, xForwardedHost={}, xForwardedProto={}, xForwardedPort={}",
                sessionId(request),
                nullSafe(request.getRequestedSessionId()),
                request.isRequestedSessionIdValid(),
                nullSafe(request.getParameter("state")),
                authorizationRequest == null ? "<missing>" : nullSafe(authorizationRequest.getState()),
                requestSummary(request),
                cookieNames(request),
                nullSafe(request.getHeader("Host")),
                nullSafe(request.getHeader("X-Forwarded-Host")),
                nullSafe(request.getHeader("X-Forwarded-Proto")),
                nullSafe(request.getHeader("X-Forwarded-Port"))
        );

        return authorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        delegate.saveAuthorizationRequest(authorizationRequest, request, response);

        if (authorizationRequest == null) {
            log.info(
                    "OIDC auth request cleared: sessionId={}, requestUri={}, cookies={}",
                    sessionId(request),
                    requestSummary(request),
                    cookieNames(request)
            );
            return;
        }

        log.info(
                "OIDC auth request saved: sessionId={}, state={}, redirectUri={}, authorizationUri={}, requestUri={}, cookies={}, host={}, xForwardedHost={}, xForwardedProto={}, xForwardedPort={}",
                sessionId(request),
                nullSafe(authorizationRequest.getState()),
                nullSafe(authorizationRequest.getRedirectUri()),
                nullSafe(authorizationRequest.getAuthorizationUri()),
                requestSummary(request),
                cookieNames(request),
                nullSafe(request.getHeader("Host")),
                nullSafe(request.getHeader("X-Forwarded-Host")),
                nullSafe(request.getHeader("X-Forwarded-Proto")),
                nullSafe(request.getHeader("X-Forwarded-Port"))
        );
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.removeAuthorizationRequest(request, response);

        if (authorizationRequest == null) {
            log.warn(
                    "OIDC auth request remove failed: sessionId={}, requestedSessionId={}, requestedSessionValid={}, callbackState={}, requestUri={}, cookies={}, host={}, xForwardedHost={}, xForwardedProto={}, xForwardedPort={}",
                    sessionId(request),
                    nullSafe(request.getRequestedSessionId()),
                    request.isRequestedSessionIdValid(),
                    nullSafe(request.getParameter("state")),
                    requestSummary(request),
                    cookieNames(request),
                    nullSafe(request.getHeader("Host")),
                    nullSafe(request.getHeader("X-Forwarded-Host")),
                    nullSafe(request.getHeader("X-Forwarded-Proto")),
                    nullSafe(request.getHeader("X-Forwarded-Port"))
            );
            return null;
        }

        log.info(
                "OIDC auth request removed: sessionId={}, callbackState={}, savedState={}, redirectUri={}, requestUri={}, cookies={}",
                sessionId(request),
                nullSafe(request.getParameter("state")),
                nullSafe(authorizationRequest.getState()),
                nullSafe(authorizationRequest.getRedirectUri()),
                requestSummary(request),
                cookieNames(request)
        );

        return authorizationRequest;
    }

    private String sessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? "<none>" : session.getId();
    }

    private String requestSummary(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null || query.isBlank()
                ? request.getMethod() + " " + request.getRequestURI()
                : request.getMethod() + " " + request.getRequestURI() + "?" + query;
    }

    private String cookieNames(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return "<none>";
        }

        return Arrays.stream(cookies)
                .map(Cookie::getName)
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "<none>" : value;
    }
}
