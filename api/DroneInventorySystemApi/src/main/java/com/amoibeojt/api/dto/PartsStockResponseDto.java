package com.amoibeojt.api.dto;

import java.time.LocalDateTime;

public class PartsStockResponseDto {
    private Long stockId;
    private String name;
    private Long categoryId;
    private Long centerId;
    private Integer currentAmount;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    // Getter/Setter を記述（Lombok使用なら @Data を付けるだけでもOK）
}