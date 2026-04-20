package com.data.service.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LocalProfileStartupTest {

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void localProfileDisablesSslForLocalDevelopment() {
        assertThat(environment.getProperty("server.ssl.enabled", Boolean.class)).isFalse();
    }

    @Test
    void localProfileDisablesBackendAuthentication() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("local-dev-user"))
                .andExpect(jsonPath("$.username").value("local-dev"))
                .andExpect(jsonPath("$.permissions[0]").value("module:xms:read"));

        mockMvc.perform(get("/api/user/cryptoassets"))
                .andExpect(status().isOk());
    }

    @Test
    void localProfileAllowsExportEmailRequestsWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/user/trades/export/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validExportEmailPayload()))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));
    }

    @Test
    void localLoginEntrypointRedirectsDirectlyToRequestedFrontendPath() throws Exception {
        mockMvc.perform(get("/api/auth/login").param("returnUrl", "/workspace"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/workspace"));
    }

    private String validExportEmailPayload() {
        return """
                {
                  "to": ["alice@example.com"],
                  "cc": [],
                  "attachments": [
                    {
                      "fileName": "trades-export.csv",
                      "contentType": "text/csv;charset=utf-8;",
                      "fileBase64": "aWQsdHJhZGVUeXBlCjEsU3BvdAo="
                    }
                  ]
                }
                """;
    }
}
