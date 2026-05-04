-- Tạo bảng review_images
CREATE TABLE review_images (
                               id BIGINT PRIMARY KEY,
                               review_id BIGINT NOT NULL,
                               image_url VARCHAR(511) NOT NULL,

    -- Thiết lập khóa ngoại trực tiếp trong câu lệnh tạo bảng (hoặc dùng ALTER TABLE sau đó)
                               CONSTRAINT fk_review_images_review
                                   FOREIGN KEY (review_id)
                                       REFERENCES reviews(id)
                                       ON DELETE CASCADE
);

-- Trong PostgreSQL, INDEX được tạo bằng câu lệnh riêng biệt
CREATE INDEX idx_review_images_review ON review_images(review_id);