package clyvasync.Clyvasync.repository.chat;

import clyvasync.Clyvasync.modules.chat.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment,Long> {
    List<MessageAttachment> findByMessageIdIn(List<Long> messageIds);
}
