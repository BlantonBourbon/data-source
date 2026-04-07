package com.data.service.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/{entity}")
public class UserEntityController extends DynamicRestController {

    public UserEntityController(EntityRegistry registry, ObjectMapper objectMapper) {
        super(registry, objectMapper);
    }
}
