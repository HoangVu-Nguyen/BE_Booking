package clyvasync.Clyvasync.controller.ai;
import clyvasync.Clyvasync.tool.HomestaySearchTool;
import clyvasync.Clyvasync.tool.RoomBookingTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import clyvasync.Clyvasync.dto.response.AiChatResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.client.ChatClient;

@RestController
public class ChatAIController {

    private final ChatClient chatClient;

    public ChatAIController(
            ChatClient.Builder builder,
            HomestaySearchTool searchTool,
             RoomBookingTool bookingTool
    ) {
        this.chatClient = builder
                .defaultTools(searchTool)

                .defaultSystem("Bạn là trợ lý ảo đặt phòng. Luôn vui vẻ, nhiệt tình. " +
                        "Khi tìm thấy phòng, hãy trích xuất dữ liệu phòng vào mảng suggestedRooms.")
                .build();
    }

    @PostMapping("/api/chat")
    public AiChatResponse chat(@RequestBody String userMessage) {

        return chatClient.prompt()
                .user(userMessage)
                .call()
                .entity(AiChatResponse.class);
    }
}