package com.data.service.core.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
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
    private static final Map<String, List<String>> SUPPORTED_CONTENT_TYPES = Map.of(
            "csv", List.of("text/csv", "text/csv;charset=utf-8", "text/csv;charset=utf-8;"),
            "xlsx", List.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    );
    private static final int MAX_RECIPIENTS = 25;
    private static final int MAX_ATTACHMENTS = 2;
    private static final int MAX_ATTACHMENT_BYTES = 5 * 1024 * 1024;

    public void send(String entity, ExportEmailRequest request) {
        if (request == null) {
            throw badRequest("Export email request body is required.");
        }

        List<String> to = normalizeRequiredEmails(request.getTo(), "to");
        List<String> cc = normalizeOptionalEmails(request.getCc(), "cc");
        validateRecipientLimit(to.size() + cc.size());
        PreparedAttachments attachments = normalizeAttachments(request.getAttachments());

        log.info(
                "Accepted export email request: entity={}, toCount={}, ccCount={}, recipientCount={}, attachmentCount={}, attachmentNames={}, totalAttachmentBytes={}",
                entity,
                to.size(),
                cc.size(),
                to.size() + cc.size(),
                attachments.files().size(),
                attachments.files().stream().map(File::getName).toList(),
                attachments.totalBytes()
        );

        sendExportEmail(to, cc, attachments.files());
    }

    private List<String> normalizeRequiredEmails(List<String> rawEmails, String fieldName) {
        List<String> normalizedEmails = normalizeEmailList(rawEmails, fieldName);
        if (normalizedEmails.isEmpty()) {
            throw badRequest("At least one " + fieldName + " email address is required.");
        }
        return normalizedEmails;
    }

    private List<String> normalizeOptionalEmails(List<String> rawEmails, String fieldName) {
        return normalizeEmailList(rawEmails, fieldName);
    }

    private List<String> normalizeEmailList(List<String> rawEmails, String fieldName) {
        LinkedHashSet<String> uniqueRecipients = new LinkedHashSet<>();
        for (String rawRecipient : rawEmails == null ? List.<String>of() : rawEmails) {
            String normalizedRecipient = rawRecipient == null ? "" : rawRecipient.trim().toLowerCase(Locale.ROOT);
            if (normalizedRecipient.isEmpty()) {
                continue;
            }

            if (!EMAIL_PATTERN.matcher(normalizedRecipient).matches()) {
                throw badRequest("The " + fieldName + " email addresses must be valid.");
            }

            uniqueRecipients.add(normalizedRecipient);
        }

        return List.copyOf(uniqueRecipients);
    }

    private void validateRecipientLimit(int recipientCount) {
        if (recipientCount > MAX_RECIPIENTS) {
            throw badRequest("Export emails support up to " + MAX_RECIPIENTS + " total to/cc recipients per request.");
        }
    }

    private PreparedAttachments normalizeAttachments(List<ExportEmailAttachmentRequest> rawAttachments) {
        if (rawAttachments == null || rawAttachments.isEmpty()) {
            throw badRequest("At least one export attachment is required.");
        }

        if (rawAttachments.size() > MAX_ATTACHMENTS) {
            throw badRequest("Export emails support up to " + MAX_ATTACHMENTS + " attachments per request.");
        }

        Path tempDirectory = createTemporaryAttachmentDirectory();
        List<File> files = new ArrayList<>();
        Set<String> fileNames = new HashSet<>();
        int totalBytes = 0;
        try {
            for (ExportEmailAttachmentRequest rawAttachment : rawAttachments) {
                if (rawAttachment == null) {
                    throw badRequest("Export attachments cannot be empty.");
                }

                String fileName = normalizeFileName(rawAttachment.getFileName());
                if (!fileNames.add(fileName.toLowerCase(Locale.ROOT))) {
                    throw badRequest("Attachment file names must be unique.");
                }

                normalizeContentType(rawAttachment.getContentType(), fileName);
                byte[] bytes = normalizeAttachmentBytes(rawAttachment.getFileBase64());
                Path attachmentPath = writeAttachmentFile(tempDirectory, fileName, bytes);
                File attachmentFile = attachmentPath.toFile();
                attachmentFile.deleteOnExit();

                files.add(attachmentFile);
                totalBytes += bytes.length;
            }

            return new PreparedAttachments(List.copyOf(files), tempDirectory, totalBytes);
        } catch (RuntimeException exception) {
            deleteTemporaryAttachments(new PreparedAttachments(List.copyOf(files), tempDirectory, totalBytes));
            throw exception;
        }
    }

    private String normalizeFileName(String rawFileName) {
        String fileName = rawFileName == null ? "" : rawFileName.trim();
        if (fileName.isEmpty()) {
            throw badRequest("Each export attachment must include a file name.");
        }

        if (resolveAttachmentExtension(fileName) == null) {
            throw badRequest("Attachment file names must end with .csv or .xlsx.");
        }

        if (!isFileNameOnly(fileName)) {
            throw badRequest("Attachment file names cannot include path segments.");
        }

        return fileName;
    }

    private boolean isFileNameOnly(String fileName) {
        try {
            return Path.of(fileName).getFileName().toString().equals(fileName);
        } catch (InvalidPathException exception) {
            return false;
        }
    }

    private void normalizeContentType(String rawContentType, String fileName) {
        String contentType = rawContentType == null ? "" : rawContentType.trim().toLowerCase(Locale.ROOT);
        String attachmentExtension = resolveAttachmentExtension(fileName);
        List<String> supportedTypes = attachmentExtension == null ? null : SUPPORTED_CONTENT_TYPES.get(attachmentExtension);
        if (supportedTypes == null || !supportedTypes.contains(contentType)) {
            throw badRequest("Attachment content type does not match the selected export format.");
        }
    }

    private String resolveAttachmentExtension(String fileName) {
        String normalizedFileName = fileName.toLowerCase(Locale.ROOT);
        if (normalizedFileName.endsWith(".csv")) {
            return "csv";
        }
        if (normalizedFileName.endsWith(".xlsx")) {
            return "xlsx";
        }
        return null;
    }

    private byte[] normalizeAttachmentBytes(String rawBase64) {
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

        return decodedBytes;
    }

    private Path createTemporaryAttachmentDirectory() {
        try {
            Path tempDirectory = Files.createTempDirectory("export-email-attachments-");
            tempDirectory.toFile().deleteOnExit();
            return tempDirectory;
        } catch (IOException exception) {
            throw serverError("Failed to prepare export attachments.", exception);
        }
    }

    private Path writeAttachmentFile(Path tempDirectory, String fileName, byte[] bytes) {
        Path attachmentPath = tempDirectory.resolve(fileName).normalize();
        if (!attachmentPath.startsWith(tempDirectory)) {
            throw badRequest("Attachment file names cannot include path segments.");
        }

        try {
            return Files.write(attachmentPath, bytes, StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException exception) {
            throw badRequest("Attachment file names must be unique.");
        } catch (IOException exception) {
            throw serverError("Failed to prepare export attachment.", exception);
        }
    }

    private void sendExportEmail(List<String> to, List<String> cc, List<File> attachments) {
        // Hook the internal mail handler here. It should receive only to/cc and List<File> attachments.
        // These files stay available after this method returns, so async handlers can read them later.
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException serverError(String message, Throwable cause) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    private void deleteTemporaryAttachments(PreparedAttachments attachments) {
        for (File file : attachments.files()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException exception) {
                log.warn("Failed to delete temporary export attachment: fileName={}", file.getName(), exception);
            }
        }

        try {
            Files.deleteIfExists(attachments.directory());
        } catch (IOException exception) {
            log.warn("Failed to delete temporary export attachment directory: directory={}", attachments.directory(), exception);
        }
    }

    private record PreparedAttachments(
            List<File> files,
            Path directory,
            int totalBytes
    ) {
    }
}
