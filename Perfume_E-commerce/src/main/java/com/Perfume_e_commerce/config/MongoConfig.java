package com.Perfume_e_commerce.config;

import com.Perfume_e_commerce.models.user.User;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    static class StringToWishlistItemConverter implements Converter<String, User.WishlistItem> {
        @Override
        public User.WishlistItem convert(String source) {
            if (source == null)
                return null;
            return new User.WishlistItem(source, null);
        }
    }

    static class DocumentToWishlistItemConverter implements Converter<Document, User.WishlistItem> {
        @Override
        public User.WishlistItem convert(Document source) {
            if (source == null)
                return null;
            Object pid = source.get("productId");
            Object size = source.get("size");
            String productId = pid != null ? pid.toString() : null;
            String sz = size != null ? size.toString() : null;
            return new User.WishlistItem(productId, sz);
        }
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new StringToWishlistItemConverter());
        converters.add(new DocumentToWishlistItemConverter());
        return new MongoCustomConversions(converters);
    }
}
