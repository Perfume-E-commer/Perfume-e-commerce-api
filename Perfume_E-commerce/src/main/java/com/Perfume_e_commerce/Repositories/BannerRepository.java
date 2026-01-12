package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.marketing.Banner;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BannerRepository extends MongoRepository<Banner, String> {
    List<Banner> findByActiveTrueOrderByDisplayOrderAsc();
}
