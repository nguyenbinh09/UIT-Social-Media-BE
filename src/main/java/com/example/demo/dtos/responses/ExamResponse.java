package com.example.demo.dtos.responses;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExamResponse {
    private String mamh;
    private String malop;
    private String caTietThi;
    private String thuThi;
    private LocalDate ngayThi;
    private String phongThi;
    private String ghiChu;
}
