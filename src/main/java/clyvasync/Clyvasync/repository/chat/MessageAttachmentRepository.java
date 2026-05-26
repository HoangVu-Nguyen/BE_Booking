package clyvasync.Clyvasync.repository.chat;

import clyvasync.Clyvasync.modules.chat.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment,Long> {
}
