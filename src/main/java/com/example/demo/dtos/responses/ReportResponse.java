package com.example.demo.dtos.responses;

import com.example.demo.models.PostReport;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private Long id;
    private PostResponse post;
    private UserResponse reportedBy;
    private String reason;
    private String status;
    private String adminNotes;

    public ReportResponse toDTO(PostReport report, ProfileResponseBuilder profileResponseBuilder) {
        this.setId(report.getId());
        this.setPost(new PostResponse().toDTO(report.getPost(), profileResponseBuilder));
        this.setReportedBy(new UserResponse().toDTO(report.getReportedBy(), profileResponseBuilder));
        this.setReason(report.getReason());
        this.setStatus(report.getStatus().name());
        this.setAdminNotes(report.getAdminNotes());
        return this;
    }

    public List<ReportResponse> mapReportsToDTOs(List<PostReport> reports, ProfileResponseBuilder profileResponseBuilder) {
        return reports.stream()
                .map(report -> new ReportResponse().toDTO(report, profileResponseBuilder))
                .collect(Collectors.toList());
    }
}
