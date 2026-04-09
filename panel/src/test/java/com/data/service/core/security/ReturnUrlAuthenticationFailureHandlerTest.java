package com.data.service.core.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReturnUrlAuthenticationFailureHandlerTest {

    private final ReturnUrlAuthenticationFailureHandler failureHandler = new ReturnUrlAuthenticationFailureHandler();

    @Test
    void redirectsBackToFrontendLoginWithErrorAndStoredReturnUrl() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(AuthController.RETURN_URL_SESSION_ATTRIBUTE, "/workspace");
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(
                request,
                response,
                new OAuth2AuthenticationException(new OAuth2Error("access_denied"))
        );

        assertEquals(
                "/auth/login?returnUrl=%2Fworkspace&error=Sign-in%20failed.%20Please%20try%20again.",
                response.getRedirectedUrl()
        );
    }
}
