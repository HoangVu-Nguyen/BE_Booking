package clyvasync.Clyvasync.controller.ai;
import clyvasync.Clyvasync.tool.HomestaySearchTool;
import clyvasync.Clyvasync.tool.RoomBookingTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class ChatAIController {
    private final ChatClient chatClient;

    public ChatAIController(
            ChatClient.Builder builder,
            HomestaySearchTool searchTool,
            RoomBookingTool bookingTool
    ) {
        this.chatClient = builder
                .defaultTools(searchTool, bookingTool)
                .build();
    }

    @PostMapping("/api/chat")
    public String chat(@RequestBody String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
