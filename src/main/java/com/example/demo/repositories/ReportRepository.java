package com.example.demo.repositories;

import com.example.demo.enums.ReportStatus;
import com.example.demo.models.Post;
import com.example.demo.models.PostReport;
import com.example.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<PostReport, Long> {
//    List<PostReport> findByStatus(ReportStatus status, Pageable pageable);

    boolean existsByPostAndReportedByAndStatus(Post post, User currentUser, ReportStatus status);

    Page<PostReport> findByStatus(ReportStatus status, Pageable pageable);
}
