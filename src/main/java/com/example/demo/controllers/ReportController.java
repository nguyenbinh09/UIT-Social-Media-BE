package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreateReportRequest;
import com.example.demo.dtos.responses.ResolveReportRequest;
import com.example.demo.enums.ReportStatus;
import com.example.demo.services.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final ReportService reportService;

    @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER')")
    @PostMapping("/reportPost/{postId}")
    public ResponseEntity<?> reportPost(@PathVariable Long postId, @RequestBody CreateReportRequest reportRequest) {
        try {
            return reportService.reportPost(postId, reportRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/getReports")
    public ResponseEntity<?> getReports(@RequestParam(value = "status", required = false) ReportStatus status,
                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                        @RequestParam(value = "sortBy", defaultValue = "createAt") String sortBy,
                                        @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                                        PagedResourcesAssembler assembler) {
        try {
            return reportService.getReports(status, page, size, sortBy, sortDir, assembler);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/resolveReport/{reportId}")
    public ResponseEntity<?> resolveReport(@PathVariable Long reportId, @RequestBody ResolveReportRequest resolveReportRequest) {
        try {
            return reportService.resolveReport(reportId, resolveReportRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
