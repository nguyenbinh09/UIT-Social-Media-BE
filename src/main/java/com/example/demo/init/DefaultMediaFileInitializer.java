//package com.example.demo.init;
//
//import com.example.demo.enums.MediaType;
//import com.example.demo.models.MediaFile;
//import com.example.demo.repositories.MediaFileRepository;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DefaultMediaFileInitializer {
//
//    @Autowired
//    private MediaFileRepository mediaFileRepository;
//
//    @PostConstruct
//    public void initDefaultMediaFile() {
//        if (!mediaFileRepository.existsByFileName("male-default-avatar.png")) {
//            MediaFile defaultAvatar = new MediaFile();
//            defaultAvatar.setFileName("male-default-avatar.png");
//            defaultAvatar.setUrl("https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/male-default-avatar.png?alt=media&token=7e8f5970-35fa-4d3d-97ae-361dfb91903d"); // URL của ảnh avatar mặc định
//            defaultAvatar.setMediaType(MediaType.IMAGE);
//            mediaFileRepository.save(defaultAvatar);
//        }
//        if (!mediaFileRepository.existsByFileName("female-default-avatar.png")) {
//            MediaFile defaultAvatar = new MediaFile();
//            defaultAvatar.setFileName("female-default-avatar.png");
//            defaultAvatar.setUrl("https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/female-default-avatar.png?alt=media&token=248e1df8-df98-44ba-803b-56e620f1c762"); // URL của ảnh avatar mặc định
//            defaultAvatar.setMediaType(MediaType.IMAGE);
//            mediaFileRepository.save(defaultAvatar);
//        }
//    }
//}
