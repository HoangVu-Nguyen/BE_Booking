-- Chạy lệnh này để cột ID tự động nhảy số
ALTER TABLE review_images
ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY;