-- BẢNG 1: HỒ SƠ KYC CỦA HOST
CREATE TABLE host_kyc_profiles (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL UNIQUE,

                                   legal_name VARCHAR(255) NOT NULL,
                                   id_card_number VARCHAR(20) NOT NULL UNIQUE,
                                   id_card_issued_date DATE,
                                   id_card_issued_by VARCHAR(255),

                                   bank_name VARCHAR(100) NOT NULL,
                                   bank_account_number VARCHAR(50) NOT NULL,
                                   bank_account_owner VARCHAR(255) NOT NULL,

                                   status VARCHAR(50) DEFAULT 'PENDING_REVIEW' NOT NULL,

                                   reviewed_by BIGINT,
                                   rejection_reason TEXT,

                                   version INT DEFAULT 0 NOT NULL, -- Dùng cho Optimistic Locking
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Ràng buộc khóa ngoại đặt thẳng ở Database
                                   CONSTRAINT fk_host_kyc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_host_kyc_status ON host_kyc_profiles(status);
CREATE INDEX idx_host_kyc_id_card ON host_kyc_profiles(id_card_number);

-- BẢNG 2: KHO LƯU TRỮ GIẤY TỜ CHỨNG MINH
CREATE TABLE host_kyc_documents (
                                    id BIGSERIAL PRIMARY KEY,
                                    profile_id BIGINT NOT NULL,

                                    document_type VARCHAR(50) NOT NULL,
                                    file_url TEXT NOT NULL,

                                    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
                                    rejection_note TEXT,

                                    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Ràng buộc khóa ngoại
                                    CONSTRAINT fk_kyc_documents_profile FOREIGN KEY (profile_id) REFERENCES host_kyc_profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_kyc_docs_profile ON host_kyc_documents(profile_id);