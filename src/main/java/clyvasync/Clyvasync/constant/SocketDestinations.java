package clyvasync.Clyvasync.constant;

public interface SocketDestinations {

    // Tiền tố hệ thống (Prefixes)
    String TOPIC_PREFIX = "/topic/";
    String USER_QUEUE_PREFIX = "/queue";

    // Các kênh riêng tư dành cho từng User/Host (Private Queues)
    String WALLET_QUEUE = "/queue/wallet";
    String BOOKING_QUEUE = "/queue/booking";
    String NOTIFICATION_QUEUE = "/queue/notifications";
    String CHAT_TOPIC = "/topic/conversations/";

    // Các kênh công khai (Public Topics Broadcast)
    String ROOM_STATUS_TOPIC = "room-status";
    String SYSTEM_MAINTENANCE_TOPIC = "system-status";

}