package com.example.demo.services;

import com.example.demo.dtos.requests.CreateReportRequest;
import com.example.demo.dtos.responses.ReportResponse;
import com.example.demo.dtos.responses.ResolveReportRequest;
import com.example.demo.enums.ReportStatus;
import com.example.demo.models.Post;
import com.example.demo.models.PostReport;
import com.example.demo.models.User;
import com.example.demo.repositories.PostRepository;
import com.example.demo.repositories.ReportRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final ProfileResponseBuilder profileResponseBuilder;

    public ResponseEntity<?> reportPost(Long postId, CreateReportRequest reportRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (reportRepository.existsByPostAndReportedBy(post, currentUser)) {
            return ResponseEntity.badRequest().body("You have already reported this post.");
        }

        PostReport report = new PostReport();
        report.setPost(post);
        report.setReportedBy(currentUser);
        report.setReason(reportRequest.getReason());
        reportRepository.save(report);

        return ResponseEntity.ok("Post has been reported successfully.");
    }

    public ResponseEntity<?> getReports(ReportStatus status, int page, int size, String sortBy, String sortDir, PagedResourcesAssembler assembler) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PostReport> reports = reportRepository.findByStatusWithPage(status, pageable);

        List<ReportResponse> reportResponses = new ReportResponse().mapReportsToDTOs(reports.getContent(), profileResponseBuilder);
        Page<ReportResponse> reportResponsePage = new PageImpl<>(reportResponses, pageable, reports.getTotalElements());

        return ResponseEntity.ok(assembler.toModel(reportResponsePage));
    }

    public ResponseEntity<?> resolveReport(Long reportId, ResolveReportRequest resolveReportRequest) {
        return ResponseEntity.ok("Report has been resolved successfully.");
    }
}
