-- 1. Thêm cột status để quản lý trạng thái ảnh (PENDING, ACTIVE)
ALTER TABLE homestay_images
    ADD COLUMN status VARCHAR(50);

-- 2. Gỡ bỏ ràng buộc NOT NULL của cột homestay_id để cho phép lưu ảnh chờ
ALTER TABLE homestay_images
    ALTER COLUMN homestay_id DROP NOT NULL;