package com.petadoption.petadoption.dto;

import lombok.Data;

@Data
public class AdoptionReviewRequest {
    private Integer status;
    private String rejectReason;
}
