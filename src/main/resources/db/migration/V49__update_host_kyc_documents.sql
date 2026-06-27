ALTER TABLE host_kyc_documents
    ADD COLUMN ai_score DECIMAL(5,2),
ADD COLUMN ocr_data TEXT;