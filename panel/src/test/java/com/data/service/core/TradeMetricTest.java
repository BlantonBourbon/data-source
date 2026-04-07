package com.data.service.core;

import com.data.service.core.model.TradeEntity;
import com.data.service.core.repository.TradeRepository;
import com.data.service.core.search.MetricRequest;
import com.data.service.core.search.SearchCriteria;
import com.data.service.core.search.SearchOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class TradeMetricTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tradeRepository.deleteAll();
        tradeRepository.save(TradeEntity.builder()
                .tradeType("SPOT")
                .amount(100.0)
                .currency("USD")
                .tradeDate(LocalDate.now())
                .build());
        tradeRepository.save(TradeEntity.builder()
                .tradeType("SPOT")
                .amount(200.0)
                .currency("USD")
                .tradeDate(LocalDate.now())
                .build());
        tradeRepository.save(TradeEntity.builder()
                .tradeType("FORWARD")
                .amount(300.0)
                .currency("EUR")
                .tradeDate(LocalDate.now())
                .build());
    }

    @Test
    void testSumMetric() throws Exception {
        MetricRequest request = new MetricRequest();
        request.setMetricType("SUM");
        request.setField("amount");
        request.setFilters(List.of(
                new SearchCriteria("tradeType", SearchOperation.EQUALITY, "SPOT")));

        mockMvc.perform(post("/api/user/trades/metric")
                .with(oauth2Login())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("300.0"));
    }

    @Test
    void testCountMetric() throws Exception {
        MetricRequest request = new MetricRequest();
        request.setMetricType("COUNT");
        request.setFilters(List.of(
                new SearchCriteria("amount", SearchOperation.GREATER_THAN, 150.0)));

        mockMvc.perform(post("/api/user/trades/metric")
                .with(oauth2Login())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }
}
