package clyvasync.Clyvasync.consumer;

import clyvasync.Clyvasync.dto.event.KycSubmittedEvent;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.kyc.HostKycService;

import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import clyvasync.Clyvasync.repository.kyc.HostKycDocumentRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.kyc.HostKycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KycConsumer {

    private final HostKycService mockEkycService;
    private final HostKycDocumentRepository documentRepository;
    private final HostKycProfileRepository profileRepository;

    @RabbitListener(queues = "q.kyc.process-ekyc")
    public void handleKycProcessing(KycSubmittedEvent event) {
        log.info(">>>> [MQ Consumer] Nhận được yêu cầu xử lý eKYC cho Profile ID: {}", event.getProfileId());

        try {
            // Bước 1: Find lại ảnh từ danh sách documentIds
            List<HostKycDocument> documents = documentRepository.findAllById(event.getDocumentIds());

            // Trích xuất danh sách fileUrl (hoặc objectKey) để gửi cho bên thứ 3
            List<String> imageUrls = documents.stream()
                    .map(HostKycDocument::getFileUrl)
                    .collect(Collectors.toList());

            if (imageUrls.isEmpty()) {
                log.warn(">>>> [MQ Consumer] Không tìm thấy ảnh nào cho Profile ID: {}", event.getProfileId());
                return; // Kết thúc sớm nếu không có ảnh
            }

            // Bước 2 & 3: Bắn dữ liệu sang Mock Service để giả lập AI xử lý
            mockEkycService.processEkyc(event.getProfileId(), imageUrls);

            log.info(">>>> [MQ Consumer] Xử lý eKYC hoàn tất cho Profile ID: {}", event.getProfileId());

        } catch (Exception e) {
            log.error(">>>> [MQ Consumer] Lỗi khi xử lý eKYC cho Profile ID {}: {}", event.getProfileId(), e.getMessage());
            throw e; // Ném lỗi để RabbitMQ biết là xử lý thất bại (có thể cấu hình retry)
        }
    }
}