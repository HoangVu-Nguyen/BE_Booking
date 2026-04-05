package clyvasync.Clyvasync.service.mail;

import clyvasync.Clyvasync.dto.request.StateEmailRequest;

public interface MailService {
    void sendStateEmail(StateEmailRequest request);
}
