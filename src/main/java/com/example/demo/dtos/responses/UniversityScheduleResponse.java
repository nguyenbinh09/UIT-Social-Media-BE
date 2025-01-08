package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class UniversityScheduleResponse {
    private int thu;
    private String phonghoc;
    private boolean online;
    private String tiet;
    private String khoaql;
    private String hinhthucgd;
    private String tenmh;
    private String mamon;
    private String malop;
    private int sotc;
    private int dadk;
    private String loaimh;
    private boolean thuchanh;
    private String ht2_lichgapsv;
    private String ngonngu;
    private int hocky;
    private int namhoc;
    private LocalDate ngaybd;
    private LocalDate ngaykt;

    private List<LecturerResponse> magv;
}
