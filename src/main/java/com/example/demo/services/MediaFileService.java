package com.example.demo.services;

import com.example.demo.models.MediaFile;
import com.example.demo.models.Post;
import com.example.demo.repositories.MediaFileRepository;
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
            String fileName = file.getOriginalFilename();
            String fileUrl = FirebaseService.uploadFile(file);  // Upload to Firebase

            MediaFile mediaFile = new MediaFile();
            mediaFile.setFileName(fileName);
            mediaFile.setUrl(fileUrl);
            mediaFile.setPost(post);
            savedMediaFiles.add(mediaFile);
        }
        return savedMediaFiles;
    }
}
