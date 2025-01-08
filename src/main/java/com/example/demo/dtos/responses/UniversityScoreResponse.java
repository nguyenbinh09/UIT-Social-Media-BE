package com.example.demo.dtos.responses;

import lombok.Data;

import java.util.List;

@Data
public class UniversityScoreResponse {
    private String name;
    private List<ScoreDetails> score;
    private String currentAcademicYear;

    @Data
    public static class ScoreDetails {
        private String mamh; // Mã môn học
        private String malop; // Mã lớp
        private String diem; // Tổng điểm
        private String diem1; // Điểm thành phần 1
        private String diem2; // Điểm thành phần 2
        private String diem3; // Điểm thành phần 3
        private String diem4; // Điểm thành phần 4
        private String heso1; // Hệ số 1
        private String heso2; // Hệ số 2
        private String heso3; // Hệ số 3
        private String heso4; // Hệ số 4
        private int hocky; // Học kỳ
        private int namhoc; // Năm học
        private int sotc; // Số tín chỉ
        private String tenmh; // Tên môn học
        private String loaimh; // Loại môn học
    }
}
