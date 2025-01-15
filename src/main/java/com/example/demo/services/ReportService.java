package com.example.demo.services;

import com.example.demo.dtos.requests.CreateReportRequest;
import com.example.demo.dtos.responses.ReportResponse;
import com.example.demo.dtos.responses.ResolveReportRequest;
import com.example.demo.enums.NotificationType;
import com.example.demo.enums.PostStatus;
import com.example.demo.enums.ReportStatus;
import com.example.demo.models.*;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.repositories.PostRepository;
import com.example.demo.repositories.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final ProfileResponseBuilder profileResponseBuilder;
    private final ProfileService profileService;
    private final NotificationRepository notificationRepository;
    private final FirebaseService firebaseService;
    private final NotificationService notificationService;

    @Transactional
    public ResponseEntity<?> reportPost(Long postId, CreateReportRequest reportRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (reportRepository.existsByPostAndReportedByAndStatus(post, currentUser, ReportStatus.PENDING)) {
            return ResponseEntity.badRequest().body("You have already reported this post.");
        }

        PostReport report = new PostReport();
        report.setPost(post);
        report.setReportedBy(currentUser);
        report.setReason(reportRequest.getReason());
        reportRepository.save(report);

        return ResponseEntity.ok("Post has been reported successfully.");
    }

    @Transactional
    public ResponseEntity<?> getReports(ReportStatus status, int page, int size, String sortBy, String sortDir, PagedResourcesAssembler assembler) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PostReport> reports = reportRepository.findByStatus(status, pageable);

        List<ReportResponse> reportResponses = new ReportResponse().mapReportsToDTOs(reports.getContent(), profileResponseBuilder);
        Page<ReportResponse> reportResponsePage = new PageImpl<>(reportResponses, pageable, reports.getTotalElements());

        return ResponseEntity.ok(assembler.toModel(reportResponsePage));
    }

    @Transactional
    public ResponseEntity<?> resolveReport(Long reportId, ResolveReportRequest resolveReportRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        PostReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (report.getStatus() != ReportStatus.PENDING) {
            return ResponseEntity.badRequest().body("Report has already been resolved.");
        }

        Post post = report.getPost();

        if (resolveReportRequest.isRemovePost()) {
            post.setStatus(PostStatus.DELETED);
            postRepository.save(post);

            User postOwner = post.getUser();
            String title = "Post has been removed";
            String message = "Your post has been removed due to violation of community guidelines.";
            String avatar = "https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/admin-avatar.jpg?alt=media&token=91551f76-4094-42de-b3c4-2ceb0622e812";
            String actionUrl = "/posts/" + report.getPost().getId();

            Notification warning = new Notification();
            warning.setSender(currentUser);
            warning.setReceiver(postOwner);
            warning.setType(NotificationType.WARNING);
            warning.setMessage(message);
            warning.setActionUrl(actionUrl);
            notificationRepository.save(warning);

            firebaseService.pushNotificationToUser(warning, postOwner);
            if (postOwner.getFcmToken() != null && !postOwner.getId().equals(currentUser.getId())) {
                Map<String, String> dataPayload = Map.of(
                        "type", NotificationType.WARNING.name(),
                        "postId", report.getPost().getId().toString()
                );

                notificationService.sendNotification(postOwner, title, message, avatar, dataPayload);
            }
        }

        report.setStatus(ReportStatus.RESOLVED);
        report.setAdminNotes(resolveReportRequest.getAdminNotes());
        reportRepository.save(report);

        return ResponseEntity.ok("Report has been resolved.");
    }

    @Transactional
    public ResponseEntity<?> warnPostOwner(Long reportId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        PostReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        User postOwner = report.getPost().getUser();

        String title = "Post flagged";
        String message = "Your post has been flagged and violates community guidelines. Please adhere to the rules.";
        String avatar = "https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/admin-avatar.jpg?alt=media&token=91551f76-4094-42de-b3c4-2ceb0622e812";
        String actionUrl = "/posts/" + report.getPost().getId();

        Notification warning = new Notification();
        warning.setSender(currentUser);
        warning.setReceiver(postOwner);
        warning.setType(NotificationType.WARNING);
        warning.setMessage(message);
        warning.setActionUrl(actionUrl);
        notificationRepository.save(warning);

        firebaseService.pushNotificationToUser(warning, postOwner);
        if (postOwner.getFcmToken() != null && !postOwner.getId().equals(currentUser.getId())) {
            Map<String, String> dataPayload = Map.of(
                    "type", NotificationType.WARNING.name(),
                    "postId", report.getPost().getId().toString()
            );

            notificationService.sendNotification(postOwner, title, message, avatar, dataPayload);
        }
        return ResponseEntity.ok("Warning sent to post owner.");
    }
}
