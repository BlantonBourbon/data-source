package com.data.service.core.export;

public record ExportEmailResponse(
        String status,
        String deliveryMode,
        int recipientCount,
        int attachmentCount,
        String message
) {
}
