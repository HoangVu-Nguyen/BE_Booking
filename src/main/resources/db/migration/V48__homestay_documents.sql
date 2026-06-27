CREATE TABLE homestay_documents (
                                    id BIGSERIAL PRIMARY KEY,
                                    homestay_id BIGINT NOT NULL,
                                    document_type VARCHAR(50) NOT NULL,
                                    file_name VARCHAR(255),
                                    file_url TEXT NOT NULL,
                                    status VARCHAR(50) DEFAULT 'PENDING',
                                    rejection_reason TEXT,
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_homestay_documents_homestay FOREIGN KEY (homestay_id) REFERENCES homestays(id) ON DELETE CASCADE
);