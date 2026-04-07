package com.data.service.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grafana/{entity}")
public class GrafanaEntityController extends DynamicRestController {

    public GrafanaEntityController(EntityRegistry registry, ObjectMapper objectMapper) {
        super(registry, objectMapper);
    }
}
