package com.data.service.core.export;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExportEmailRequest {
    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<ExportEmailAttachmentRequest> attachments = new ArrayList<>();
}
