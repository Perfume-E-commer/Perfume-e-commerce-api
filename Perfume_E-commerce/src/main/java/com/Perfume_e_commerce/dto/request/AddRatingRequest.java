package com.Perfume_e_commerce.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddRatingRequest {
    @Min(1) @Max(5)
    private int stars;

    @NotBlank
    private String comment;
}
