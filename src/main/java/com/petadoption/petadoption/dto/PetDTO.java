package com.petadoption.petadoption.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PetDTO {
    private Long id;
    private String name;
    private String type;
    private String breed;
    private Integer ageMonths;
    private String gender;
    private String color;
    private BigDecimal weight;
    private String description;
    private String healthStatus;
    private String vaccineStatus;
    private Integer sterilization;
    private List<String> images;
    private Long ownerId;
    private String ownerName;
    private String status;
    private String location;
    private String contactInfo;
    private String requirements;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createTime;
}
