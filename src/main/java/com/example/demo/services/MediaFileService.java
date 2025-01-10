package com.example.demo.services;

import com.example.demo.enums.FeedItemType;
import com.example.demo.enums.MediaType;
import com.example.demo.models.Comment;
import com.example.demo.models.MediaFile;
import com.example.demo.models.Message;
import com.example.demo.models.Post;
import com.example.demo.repositories.CommentRepository;
import com.example.demo.repositories.MediaFileRepository;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.PostRepository;
import com.example.demo.utils.MediaFIleUtils;
import com.google.cloud.storage.Blob;
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
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private MessageRepository messageRepository;

    public List<MediaFile> uploadMediaFile(Long id, FeedItemType feedItemType, List<MultipartFile> mediaFiles) {
        List<MediaFile> savedMediaFiles = new ArrayList<>();
        for (MultipartFile file : mediaFiles) {
            MediaFile mediaFile = new MediaFile();
            MediaType mediaType = MediaFIleUtils.determineMediaType(file);
            String mediaURL = FirebaseService.uploadFile(file);

            System.out.println("Media size: " + file.getSize() / 1024F);

            mediaFile.setFileName(file.getOriginalFilename());
            mediaFile.setUrl(mediaURL);
            mediaFile.setMediaType(mediaType);
            mediaFile.setSize(file.getSize() / 1024F);
            System.out.println("Media size: " + file.getSize() / 1024F + " " + mediaFile.getSize());
            switch (feedItemType) {
                case POST:
                    Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
                    mediaFile.setPost(post);
                    break;
                case COMMENT:
                    Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
                    mediaFile.setComment(comment);
                    break;
                case MESSAGE:
                    Message message = messageRepository.findById(id).orElseThrow(() -> new RuntimeException("Message not found"));
                    mediaFile.setMessage(message);
                    break;
            }
            savedMediaFiles.add(mediaFile);
        }
        mediaFileRepository.saveAll(savedMediaFiles);
        return savedMediaFiles;
    }

    public void deleteMediaFiles(List<MediaFile> mediaFiles) {
        for (MediaFile mediaFile : mediaFiles) {
            FirebaseService.deleteFile(mediaFile.getUrl());  // Delete from Firebase
            mediaFileRepository.delete(mediaFile);
        }
    }
}
