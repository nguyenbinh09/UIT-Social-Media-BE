package com.example.demo.utils;
import org.apache.commons.io.FilenameUtils;
import com.example.demo.enums.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

public class MediaFIleUtils {
    public static MediaType determineMediaType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType != null) {
            if (contentType.startsWith("image")) {
                return MediaType.IMAGE;
            } else if (contentType.startsWith("video")) {
                return MediaType.VIDEO;
            } else if (contentType.startsWith("application")) {
                return MediaType.DOCUMENT;
            }
        }

        String extension = Objects.requireNonNull(FilenameUtils.getExtension(file.getOriginalFilename())).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg", "png", "gif" -> MediaType.IMAGE;
            case "mp4", "avi", "mkv" -> MediaType.VIDEO;
            case "pdf", "doc", "docx", "xls", "xlsx" -> MediaType.DOCUMENT;
            default -> MediaType.OTHER;
        };
    }
}
