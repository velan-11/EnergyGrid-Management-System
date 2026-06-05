package com.cts.workorder_service.controller;

import com.cts.workorder_service.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Multipart upload endpoint backing the Evidence Upload flow.
 *
 * - POST /api/upload          → accepts the file, returns { fileUrl, sha256 }
 * - GET  /api/upload/{name}   → serves an uploaded file (auth-guarded)
 *
 * Files live under `eg.upload.dir` (default ./uploads/, created on first use).
 * SHA-256 is computed once on the server for tamper evidence — clients store
 * the hash alongside the file URL on the evidence record.
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "application/pdf",
            "video/mp4"
    );

    private final Path uploadDir;

    public UploadController(@Value("${eg.upload.dir:./uploads}") String dir) throws IOException {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER')")
    public ResponseEntity<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file received");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("File exceeds 10MB limit");
        }
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_TYPES.contains(mimeType)) {
            throw new BadRequestException("File type not allowed: " + mimeType);
        }

        String original = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot).toLowerCase();
        }
        String filename = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename).normalize();

        // Defence against path traversal
        if (!target.startsWith(uploadDir)) {
            throw new BadRequestException("Invalid file path");
        }

        byte[] bytes = file.getBytes();
        Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
        String sha256 = sha256Hex(bytes);

        return ResponseEntity.ok(Map.of(
                "fileUrl", "/api/upload/" + filename,
                "filename", filename,
                "originalName", original,
                "mimeType", mimeType,
                "sizeBytes", file.getSize(),
                "sha256", sha256
        ));
    }

    @GetMapping("/{filename:.+}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','CUSTOMER','AUDITOR')")
    public ResponseEntity<Resource> serve(@PathVariable String filename,
                                          HttpServletRequest req) throws MalformedURLException {
        Path target = uploadDir.resolve(filename).normalize();
        if (!target.startsWith(uploadDir) || !Files.exists(target)) {
            return ResponseEntity.notFound().build();
        }
        UrlResource resource = new UrlResource(target.toUri());
        String contentType;
        try {
            contentType = Files.probeContentType(target);
        } catch (IOException e) {
            contentType = null;
        }
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
