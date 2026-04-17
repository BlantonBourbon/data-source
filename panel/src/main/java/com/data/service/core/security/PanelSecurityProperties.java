package com.data.service.core.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConfigurationProperties(prefix = "panel.security")
public class PanelSecurityProperties {

    private boolean h2ConsoleEnabled;
    private String frontendBaseUrl;
    private final PingFederate pingFederate = new PingFederate();
    private final ClaimMapping claimMapping = new ClaimMapping();
    private final LocalDev localDev = new LocalDev();
    private final List<ApplicationClient> applicationClients = new ArrayList<>();

    public boolean isH2ConsoleEnabled() {
        return h2ConsoleEnabled;
    }

    public void setH2ConsoleEnabled(boolean h2ConsoleEnabled) {
        this.h2ConsoleEnabled = h2ConsoleEnabled;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public PingFederate getPingFederate() {
        return pingFederate;
    }

    public ClaimMapping getClaimMapping() {
        return claimMapping;
    }

    public LocalDev getLocalDev() {
        return localDev;
    }

    public List<ApplicationClient> getApplicationClients() {
        return applicationClients;
    }

    public Optional<ApplicationClient> findApplicationClientByCertificateCn(String certificateCn) {
        if (certificateCn == null || certificateCn.isBlank()) {
            return Optional.empty();
        }

        return applicationClients.stream()
                .filter(client -> certificateCn.trim().equalsIgnoreCase(client.getCertificateCn()))
                .findFirst();
    }

    public static class PingFederate {
        private String registrationId = "pingfed";
        private String logoutSuccessUrl = "/";
        private String endSessionEndpoint;

        public String getRegistrationId() {
            return registrationId;
        }

        public void setRegistrationId(String registrationId) {
            this.registrationId = registrationId;
        }

        public String getLogoutSuccessUrl() {
            return logoutSuccessUrl;
        }

        public void setLogoutSuccessUrl(String logoutSuccessUrl) {
            this.logoutSuccessUrl = logoutSuccessUrl;
        }

        public String getEndSessionEndpoint() {
            return endSessionEndpoint;
        }

        public void setEndSessionEndpoint(String endSessionEndpoint) {
            this.endSessionEndpoint = endSessionEndpoint;
        }
    }

    public static class ClaimMapping {
        private String usernameClaim = "preferred_username";
        private String displayNameClaim = "name";
        private String emailClaim = "email";
        private String groupsClaim = "groups";
        private String entitlementsClaim = "entitlements";
        private String permissionsClaim = "permissions";
        private String dataScopesClaim = "data_scopes";

        public String getUsernameClaim() {
            return usernameClaim;
        }

        public void setUsernameClaim(String usernameClaim) {
            this.usernameClaim = usernameClaim;
        }

        public String getDisplayNameClaim() {
            return displayNameClaim;
        }

        public void setDisplayNameClaim(String displayNameClaim) {
            this.displayNameClaim = displayNameClaim;
        }

        public String getEmailClaim() {
            return emailClaim;
        }

        public void setEmailClaim(String emailClaim) {
            this.emailClaim = emailClaim;
        }

        public String getGroupsClaim() {
            return groupsClaim;
        }

        public void setGroupsClaim(String groupsClaim) {
            this.groupsClaim = groupsClaim;
        }

        public String getEntitlementsClaim() {
            return entitlementsClaim;
        }

        public void setEntitlementsClaim(String entitlementsClaim) {
            this.entitlementsClaim = entitlementsClaim;
        }

        public String getPermissionsClaim() {
            return permissionsClaim;
        }

        public void setPermissionsClaim(String permissionsClaim) {
            this.permissionsClaim = permissionsClaim;
        }

        public String getDataScopesClaim() {
            return dataScopesClaim;
        }

        public void setDataScopesClaim(String dataScopesClaim) {
            this.dataScopesClaim = dataScopesClaim;
        }
    }

    public static class ApplicationClient {
        private String name;
        private String certificateCn;
        private final List<String> authorities = new ArrayList<>();
        private final Map<String, String> claims = new LinkedHashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCertificateCn() {
            return certificateCn;
        }

        public void setCertificateCn(String certificateCn) {
            this.certificateCn = certificateCn;
        }

        public List<String> getAuthorities() {
            return authorities;
        }

        public Map<String, String> getClaims() {
            return claims;
        }
    }

    public static class LocalDev {
        private boolean authDisabled;
        private String id = "local-dev-user";
        private String username = "local-dev";
        private String displayName = "Local Developer";
        private String email = "local.dev@example.test";
        private final List<String> groups = new ArrayList<>();
        private final List<String> entitlements = new ArrayList<>();
        private final List<String> permissions = new ArrayList<>();
        private final Map<String, Object> dataScopes = new LinkedHashMap<>();
        private final Map<String, Object> claims = new LinkedHashMap<>();

        public boolean isAuthDisabled() {
            return authDisabled;
        }

        public void setAuthDisabled(boolean authDisabled) {
            this.authDisabled = authDisabled;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public List<String> getGroups() {
            return groups;
        }

        public List<String> getEntitlements() {
            return entitlements;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public Map<String, Object> getDataScopes() {
            return dataScopes;
        }

        public Map<String, Object> getClaims() {
            return claims;
        }

        public BackendUserContext toBackendUserContext() {
            return new BackendUserContext(
                    id,
                    username,
                    displayName,
                    email,
                    List.copyOf(groups),
                    List.copyOf(entitlements),
                    List.copyOf(permissions),
                    Map.copyOf(dataScopes),
                    Map.copyOf(claims)
            );
        }
    }
}
