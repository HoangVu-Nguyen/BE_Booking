CREATE TABLE review_images (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               review_id BIGINT NOT NULL,
                               image_url VARCHAR(511) NOT NULL,

                               INDEX idx_review_images_review (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE review_images
    ADD CONSTRAINT fk_review_images_review
        FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE;