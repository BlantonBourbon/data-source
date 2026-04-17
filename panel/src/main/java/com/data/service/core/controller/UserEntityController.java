package com.data.service.core.controller;

import com.data.service.core.export.ExportEmailRequest;
import com.data.service.core.export.ExportEmailResponse;
import com.data.service.core.export.ExportEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/{entity}")
public class UserEntityController extends GenericEntityController {

    private final ExportEmailService exportEmailService;

    public UserEntityController(EntityRegistry registry,
                                ObjectMapper objectMapper,
                                ExportEmailService exportEmailService) {
        super(registry, objectMapper);
        this.exportEmailService = exportEmailService;
    }

    @PostMapping("/export/email")
    public ResponseEntity<ExportEmailResponse> sendExportEmail(@PathVariable String entity,
                                                               @RequestBody ExportEmailRequest request) {
        getServiceOrThrow(entity);
        return ResponseEntity.accepted().body(exportEmailService.accept(entity, request));
    }
}
