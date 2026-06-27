package clyvasync.Clyvasync.service.kyc.impl;

import clyvasync.Clyvasync.dto.event.KycProcessedEvent;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.auth.RoleService;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.auth.impl.RoleServiceImpl;
import clyvasync.Clyvasync.service.kyc.RealEkycService;
import clyvasync.Clyvasync.service.media.S3Service;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealEkycServiceImpl implements RealEkycService {
    private final RestTemplate restTemplate;
    private final HostKycProfileRepository profileRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    private final SocketEmitterService socketEmitterService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final RoleService roleService;

    @Value("${ekyc.fpt.api-key}")
    private  String fptApiKey;

    @Value("${ekyc.fpt.url}")
    private  String fptUrl;

    @Override
    @Transactional
    public void processEkyc(Long profileId, List<String> imageUrls) {
        log.info(">>>> [Real eKYC] Bắt đầu gọi API FPT cho Profile ID: {}", profileId);

        HostKycProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Profile"));

        try {
            String frontImageUrl = imageUrls.get(0);
            byte[] imageBytes = s3Service.downloadFileAsBytes(frontImageUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("api-key", fptApiKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "cccd_front.jpg";
                }
            };
            body.add("image", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(fptUrl, requestEntity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            int errorCode = rootNode.path("errorCode").asInt();

            if (errorCode == 0) {
                JsonNode dataNode = rootNode.path("data").get(0);
                String extractedId = dataNode.path("id").asText();
                String extractedName = dataNode.path("name").asText();

                log.info(">>>> [Real eKYC] FPT đọc được: ID={}, Name={}", extractedId, extractedName);

                boolean isIdMatch = profile.getIdCardNumber().trim().equals(extractedId.trim());
                boolean isNameMatch = profile.getLegalName().trim().equalsIgnoreCase(extractedName.trim());

                if (isIdMatch && isNameMatch) {
                    profile.setStatus(KycProfileStatus.APPROVED);
                    profile.setRejectionReason(null);
                    roleService.upgradeToHost(profileId);
                    log.info(">>>> [Real eKYC] THÀNH CÔNG! Dữ liệu khớp 100%.");
                } else {
                    profile.setStatus(KycProfileStatus.REJECTED);
                    profile.setRejectionReason("Thông tin bạn nhập không khớp với hình ảnh CCCD.");
                    log.warn(">>>> [Real eKYC] SAI LỆCH. Nhập: {}/{}, Thật: {}/{}",
                            profile.getIdCardNumber(), profile.getLegalName(), extractedId, extractedName);
                }
            } else {
                String errorMessage = rootNode.path("errorMessage").asText();
                profile.setStatus(KycProfileStatus.REJECTED);
                profile.setRejectionReason("Ảnh không đạt yêu cầu: " + errorMessage);
                log.error(">>>> [Real eKYC] Ảnh lỗi: {}", errorMessage);
            }

        } catch (Exception e) {
            log.error(">>>> [Real eKYC] Exception khi gọi FPT API: {}", e.getMessage());
            throw new RuntimeException("Lỗi kết nối đến dịch vụ eKYC", e);
        }


        profileRepository.save(profile);
        eventPublisher.publishEvent(new KycProcessedEvent(
                profile.getUserId(),
                profile.getStatus(),
                profile.getRejectionReason()
        ));
    }
}
