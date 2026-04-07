package com.data.service.core;

import com.data.service.core.model.CryptoAssetEntity;
import com.data.service.core.repository.CryptoAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.security.auth.x500.X500Principal;
import java.math.BigDecimal;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CryptoAssetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CryptoAssetRepository cryptoAssetRepository;

    @BeforeEach
    void setUp() {
        cryptoAssetRepository.deleteAll();

        cryptoAssetRepository.save(CryptoAssetEntity.builder()
                .symbol("BTC")
                .marketCap(new BigDecimal("900000000000"))
                .listingDate(LocalDate.of(2009, 1, 3))
                .build());

        cryptoAssetRepository.save(CryptoAssetEntity.builder()
                .symbol("ETH")
                .marketCap(new BigDecimal("300000000000"))
                .listingDate(LocalDate.of(2015, 7, 30))
                .build());
    }

    @Test
    void testCryptoAssetsAreLoadedInRepository() {
        List<CryptoAssetEntity> assets = cryptoAssetRepository.findAll();
        assertThat(assets).hasSize(2);

        CryptoAssetEntity btc = assets.stream().filter(a -> a.getSymbol().equals("BTC")).findFirst().orElseThrow();
        assertThat(btc.getMarketCap()).isEqualByComparingTo(new BigDecimal("900000000000"));
    }

    @Test
    void testRestfulServiceReturnsCryptoAssets() throws Exception {
        mockMvc.perform(get("/api/user/cryptoassets")
                .with(oauth2Login())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].symbol").exists())
                .andExpect(jsonPath("$[1].symbol").exists());
    }

    @Test
    void testGrafanaRouteReturnsCryptoAssetsForTrustedCertificate() throws Exception {
        mockMvc.perform(get("/api/grafana/cryptoassets")
                .requestAttr("jakarta.servlet.request.X509Certificate",
                        new X509Certificate[]{certificate("CN=grafana-test, OU=Observability, O=Example Corp")})
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].symbol").exists())
                .andExpect(jsonPath("$[1].symbol").exists());
    }

    private X509Certificate certificate(String subjectDn) {
        X509Certificate certificate = mock(X509Certificate.class);
        X500Principal principal = new X500Principal(subjectDn);
        when(certificate.getSubjectX500Principal()).thenReturn(principal);
        when(certificate.getSubjectDN()).thenReturn((Principal) principal);
        return certificate;
    }
}
