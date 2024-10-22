package com.example.demo.services;

import com.example.demo.enums.MediaType;
import com.example.demo.models.MediaFile;
import com.example.demo.models.Post;
import com.example.demo.repositories.MediaFileRepository;
import com.example.demo.utils.MediaFIleUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class MediaFileService {
    private final FirebaseService FirebaseService;
    private final MediaFileRepository mediaFileRepository;

    public List<MediaFile> uploadMediaFile(Post post, List<MultipartFile> mediaFiles) {
        List<MediaFile> savedMediaFiles = new ArrayList<>();
        for (MultipartFile file : mediaFiles) {
            MediaType mediaType = MediaFIleUtils.determineMediaType(file);
            String fileName = file.getOriginalFilename();
            String fileUrl = FirebaseService.uploadFile(file);  // Upload to Firebase

            MediaFile mediaFile = new MediaFile();
            mediaFile.setFileName(fileName);
            mediaFile.setUrl(fileUrl);
            mediaFile.setPost(post);
            mediaFile.setMediaType(mediaType);
            savedMediaFiles.add(mediaFile);
        }
        return savedMediaFiles;
    }

    public void deleteMediaFiles(List<MediaFile> mediaFiles) {
        for(MediaFile mediaFile : mediaFiles) {
            FirebaseService.deleteFile(mediaFile.getUrl());  // Delete from Firebase
            mediaFileRepository.delete(mediaFile);
        }
    }
}
