package clyvasync.Clyvasync.config;

import clyvasync.Clyvasync.constant.MessagingConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue emailQueue() { return new Queue(MessagingConstants.EMAIL_QUEUE); }

    @Bean
    public TopicExchange registerExchange() { return new TopicExchange(MessagingConstants.REGISTER_EXCHANGE); }

    @Bean
    public Binding binding(Queue emailQueue, TopicExchange registerExchange) {
        return BindingBuilder.bind(emailQueue).to(registerExchange).with(MessagingConstants.REGISTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean
    public Queue paymentMailQueue() {
        return new Queue(MessagingConstants.PAYMENT_MAIL_QUEUE, true);
    }

    @Bean
    public DirectExchange bookingExchange() {
        return new DirectExchange(MessagingConstants.BOOKING_EXCHANGE);
    }

    @Bean
    public Binding bindingPaymentMail(Queue paymentMailQueue, DirectExchange bookingExchange) {
        return BindingBuilder.bind(paymentMailQueue)
                .to(bookingExchange)
                .with(MessagingConstants.PAYMENT_MAIL_ROUTING_KEY);
    }
    @Bean
    public Queue kycProcessEkycQueue() {
        return new Queue(MessagingConstants.KYC_PROCESS_EKYC_QUEUE, true);
    }

    @Bean
    public TopicExchange kycExchange() {
        return new TopicExchange(MessagingConstants.KYC_EXCHANGE);
    }

    @Bean
    public Binding kycSubmittedBinding(Queue kycProcessEkycQueue, TopicExchange kycExchange) {
        return BindingBuilder
                .bind(kycProcessEkycQueue)
                .to(kycExchange)
                .with(MessagingConstants.KYC_SUBMITTED_ROUTING_KEY);
    }
}