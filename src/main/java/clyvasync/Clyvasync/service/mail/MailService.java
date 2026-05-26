package clyvasync.Clyvasync.service.mail;

import clyvasync.Clyvasync.dto.detail.CancellationMailMessage;
import clyvasync.Clyvasync.dto.event.PaymentRequestMailMessage;
import clyvasync.Clyvasync.dto.request.StateEmailRequest;

public interface MailService {
    void sendStateEmail(StateEmailRequest request);
     void sendPaymentRequestEmail(PaymentRequestMailMessage msg) ;
    void sendCancellationEmail(CancellationMailMessage msg);
}
