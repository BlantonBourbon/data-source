package com.data.service.core.export;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExportEmailRequest {
    private List<String> recipients = new ArrayList<>();
    private List<ExportEmailAttachmentRequest> attachments = new ArrayList<>();
    private Integer rowCount;
    private String exportTitle;
}
