package com.Perfume_e_commerce.models.product;

import lombok.Data;

@Data
public class ProductStory {
    private StorySection intro;
    private StorySection overture;

    @Data
    public static class StorySection {
        private String title;
        private String content;
    }
}
