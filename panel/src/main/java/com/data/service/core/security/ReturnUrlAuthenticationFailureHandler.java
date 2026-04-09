package com.data.service.core.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class ReturnUrlAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String FAILURE_MESSAGE = "Sign-in failed. Please try again.";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String redirectTarget = UriComponentsBuilder.fromPath("/auth/login")
                .queryParam("returnUrl", ReturnUrlSupport.resolveAndClear(request))
                .queryParam("error", FAILURE_MESSAGE)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectTarget);
    }
}
