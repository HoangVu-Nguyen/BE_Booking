CREATE TABLE review_images (
                               id BIGSERIAL PRIMARY KEY,
                               review_id BIGINT NOT NULL,
                               image_url TEXT NOT NULL,
                               display_order INT DEFAULT 0,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_review_images_review
                                   FOREIGN KEY (review_id)
                                       REFERENCES reviews(id)
                                       ON DELETE CASCADE
);

CREATE INDEX idx_review_images_review_id ON review_images(review_id);

COMMENT ON TABLE review_images IS 'Lưu trữ danh sách hình ảnh đi kèm với các bài đánh giá homestay từ khách hàng';