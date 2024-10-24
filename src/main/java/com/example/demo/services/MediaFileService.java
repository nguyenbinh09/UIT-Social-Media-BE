package com.example.demo.services;

import com.example.demo.enums.FeedItemType;
import com.example.demo.enums.MediaType;
import com.example.demo.models.Comment;
import com.example.demo.models.MediaFile;
import com.example.demo.models.Post;
import com.example.demo.repositories.CommentRepository;
import com.example.demo.repositories.MediaFileRepository;
import com.example.demo.repositories.PostReposiroty;
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
    private PostReposiroty postReposiroty;
    private CommentRepository commentRepository;

    public List<MediaFile> uploadMediaFile(Long id, FeedItemType feedItemType, List<MultipartFile> mediaFiles) {
        List<MediaFile> savedMediaFiles = new ArrayList<>();
        for (MultipartFile file : mediaFiles) {
            MediaType mediaType = MediaFIleUtils.determineMediaType(file);
            String fileName = file.getOriginalFilename();
            String fileUrl = FirebaseService.uploadFile(file);  // Upload to Firebase

            MediaFile mediaFile = new MediaFile();
            mediaFile.setFileName(fileName);
            mediaFile.setUrl(fileUrl);
            mediaFile.setMediaType(mediaType);
            switch (feedItemType) {
                case POST:
                    Post post = postReposiroty.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
                    mediaFile.setPost(post);
                    break;
                case COMMENT:
                    Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
                    mediaFile.setComment(comment);
                    break;
            }
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
