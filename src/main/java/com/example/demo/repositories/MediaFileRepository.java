package com.example.demo.repositories;

import com.example.demo.models.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    boolean existsByFileName(String s);

    MediaFile findByFileName(String s);
}
