package com.Perfume_e_commerce.dto;

import lombok.Data;

@Data
public class ProductFilterDTO {
    private String search;
    private String category;
    private String gender;
    private Boolean featured;
    private Boolean onSale;
    private Boolean outOfStock;
    private Integer page = 0;
    private Integer size = 10;
}