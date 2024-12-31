package com.example.demo.dtos.responses;

import com.example.demo.enums.MediaType;
import com.example.demo.models.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaFileResponse {
    private String url;
    private MediaType type;

    public MediaFileResponse toDto(MediaFile url) {
        MediaFileResponse mediaFileResponse = new MediaFileResponse();
        mediaFileResponse.setUrl(url.getUrl());
        mediaFileResponse.setType(url.getMediaType());
        return mediaFileResponse;
    }

    public List<MediaFileResponse> mapsToDto(List<MediaFile> mediaFiles) {
        return mediaFiles.stream()
                .map(this::toDto)
                .toList();
    }
}