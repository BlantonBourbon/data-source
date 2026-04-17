package com.data.service.core.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ExportEmailService {

    private static final Logger log = LoggerFactory.getLogger(ExportEmailService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> SUPPORTED_FORMATS = Set.of("csv", "xlsx");
    private static final Map<String, List<String>> SUPPORTED_CONTENT_TYPES = Map.of(
            "csv", List.of("text/csv", "text/csv;charset=utf-8", "text/csv;charset=utf-8;"),
            "xlsx", List.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    );
    private static final int MAX_RECIPIENTS = 25;
    private static final int MAX_ATTACHMENTS = 2;
    private static final int MAX_ATTACHMENT_BYTES = 5 * 1024 * 1024;

    public ExportEmailResponse accept(String entity, ExportEmailRequest request) {
        if (request == null) {
            throw badRequest("Export email request body is required.");
        }

        List<String> recipients = normalizeRecipients(request.getRecipients());
        List<NormalizedAttachment> attachments = normalizeAttachments(request.getAttachments());
        int rowCount = normalizeRowCount(request.getRowCount());
        String exportTitle = normalizeExportTitle(request.getExportTitle());

        log.info(
                "Accepted export email request: entity={}, exportTitle={}, rowCount={}, recipientCount={}, attachmentCount={}, attachmentNames={}, formats={}, totalAttachmentBytes={}, deliveryMode=log-only",
                entity,
                exportTitle,
                rowCount,
                recipients.size(),
                attachments.size(),
                attachments.stream().map(NormalizedAttachment::fileName).toList(),
                attachments.stream().map(NormalizedAttachment::format).toList(),
                attachments.stream().mapToInt(NormalizedAttachment::sizeBytes).sum()
        );

        return new ExportEmailResponse(
                "accepted",
                "log-only",
                recipients.size(),
                attachments.size(),
                "Export email request accepted in log-only mode."
        );
    }

    private List<String> normalizeRecipients(List<String> rawRecipients) {
        if (rawRecipients == null || rawRecipients.isEmpty()) {
            throw badRequest("At least one recipient email is required.");
        }

        LinkedHashSet<String> uniqueRecipients = new LinkedHashSet<>();
        for (String rawRecipient : rawRecipients) {
            String normalizedRecipient = rawRecipient == null ? "" : rawRecipient.trim().toLowerCase(Locale.ROOT);
            if (normalizedRecipient.isEmpty()) {
                continue;
            }

            if (!EMAIL_PATTERN.matcher(normalizedRecipient).matches()) {
                throw badRequest("Recipient email addresses must be valid.");
            }

            uniqueRecipients.add(normalizedRecipient);
        }

        if (uniqueRecipients.isEmpty()) {
            throw badRequest("At least one recipient email is required.");
        }

        if (uniqueRecipients.size() > MAX_RECIPIENTS) {
            throw badRequest("Export emails support up to " + MAX_RECIPIENTS + " recipients per request.");
        }

        return List.copyOf(uniqueRecipients);
    }

    private List<NormalizedAttachment> normalizeAttachments(List<ExportEmailAttachmentRequest> rawAttachments) {
        if (rawAttachments == null || rawAttachments.isEmpty()) {
            throw badRequest("At least one export attachment is required.");
        }

        if (rawAttachments.size() > MAX_ATTACHMENTS) {
            throw badRequest("Export emails support up to " + MAX_ATTACHMENTS + " attachments per request.");
        }

        List<NormalizedAttachment> normalizedAttachments = new ArrayList<>();
        for (ExportEmailAttachmentRequest rawAttachment : rawAttachments) {
            if (rawAttachment == null) {
                throw badRequest("Export attachments cannot be empty.");
            }

            String format = normalizeFormat(rawAttachment.getFormat());
            String fileName = normalizeFileName(rawAttachment.getFileName(), format);
            String contentType = normalizeContentType(rawAttachment.getContentType(), format);
            int sizeBytes = normalizeAttachmentBytes(rawAttachment.getFileBase64());

            normalizedAttachments.add(new NormalizedAttachment(format, fileName, contentType, sizeBytes));
        }

        return List.copyOf(normalizedAttachments);
    }

    private String normalizeFormat(String rawFormat) {
        String format = rawFormat == null ? "" : rawFormat.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_FORMATS.contains(format)) {
            throw badRequest("Unsupported export format. Supported values are csv and xlsx.");
        }
        return format;
    }

    private String normalizeFileName(String rawFileName, String format) {
        String fileName = rawFileName == null ? "" : rawFileName.trim();
        if (fileName.isEmpty()) {
            throw badRequest("Each export attachment must include a file name.");
        }

        if (!fileName.toLowerCase(Locale.ROOT).endsWith("." + format)) {
            throw badRequest("Attachment file names must match their selected format.");
        }

        return fileName;
    }

    private String normalizeContentType(String rawContentType, String format) {
        String contentType = rawContentType == null ? "" : rawContentType.trim().toLowerCase(Locale.ROOT);
        List<String> supportedTypes = SUPPORTED_CONTENT_TYPES.get(format);
        if (supportedTypes == null || !supportedTypes.contains(contentType)) {
            throw badRequest("Attachment content type does not match the selected export format.");
        }
        return contentType;
    }

    private int normalizeAttachmentBytes(String rawBase64) {
        String fileBase64 = rawBase64 == null ? "" : rawBase64.trim();
        if (fileBase64.isEmpty()) {
            throw badRequest("Each export attachment must include a base64 payload.");
        }

        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(fileBase64);
        } catch (IllegalArgumentException exception) {
            throw badRequest("Export attachments must contain valid base64 content.");
        }

        if (decodedBytes.length == 0) {
            throw badRequest("Export attachments cannot be empty.");
        }

        if (decodedBytes.length > MAX_ATTACHMENT_BYTES) {
            throw badRequest("Each export attachment must be 5 MB or smaller.");
        }

        return decodedBytes.length;
    }

    private int normalizeRowCount(Integer rowCount) {
        if (rowCount == null || rowCount < 1) {
            throw badRequest("Export requests must include a positive row count.");
        }
        return rowCount;
    }

    private String normalizeExportTitle(String rawExportTitle) {
        String exportTitle = rawExportTitle == null ? "" : rawExportTitle.trim();
        return exportTitle.isEmpty() ? "<unspecified>" : exportTitle;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record NormalizedAttachment(
            String format,
            String fileName,
            String contentType,
            int sizeBytes
    ) {
    }
}
