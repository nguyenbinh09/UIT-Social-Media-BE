package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class ModerationResponse {
    private List<String> predictions;

    public ModerationResponse() {
    }

    public ModerationResponse(List<String> predictions) {
        this.predictions = predictions;
    }
}
