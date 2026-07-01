package clyvasync.Clyvasync.modules.chat.entity;

import clyvasync.Clyvasync.enums.type.ChatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Lấy tất cả conversation theo loại (VD: chat 1-1, chat nhóm, hỗ trợ khách hàng...)
    List<Conversation> findByType(ChatType type);

    // Lấy conversation theo referenceId (VD: liên kết với 1 booking hoặc 1 homestay cụ thể)
    Optional<Conversation> findByReferenceIdAndType(Long referenceId, ChatType type);

    // Lấy danh sách conversation, sắp xếp theo tin nhắn mới nhất trước (phục vụ hiển thị inbox)
    List<Conversation> findAllByOrderByLastMessageAtDesc();

    // Lấy conversation theo loại, sắp xếp theo tin nhắn mới nhất
    List<Conversation> findByTypeOrderByLastMessageAtDesc(ChatType type);

    // Đếm số lượng conversation theo loại (thống kê)
    long countByType(ChatType type);

    // Tìm các conversation chưa có tin nhắn nào (lastMessageAt null) - phục vụ dọn dẹp/kiểm tra
    @Query("SELECT c FROM Conversation c WHERE c.lastMessageAt IS NULL")
    List<Conversation> findConversationsWithoutMessages();
}