package com.data.service.core.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentUserController {

    private final BackendUserContextMapper userContextMapper;
    private final PanelSecurityProperties securityProperties;

    public CurrentUserController(BackendUserContextMapper userContextMapper,
                                 PanelSecurityProperties securityProperties) {
        this.userContextMapper = userContextMapper;
        this.securityProperties = securityProperties;
    }

    @GetMapping("/api/me")
    public BackendUserContext currentUser(@AuthenticationPrincipal OAuth2User user) {
        if (user == null && securityProperties.getLocalDev().isAuthDisabled()) {
            return securityProperties.getLocalDev().toBackendUserContext();
        }

        return userContextMapper.toBackendUserContext(user);
    }
}
