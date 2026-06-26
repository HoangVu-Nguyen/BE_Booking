package clyvasync.Clyvasync.dto.response;

import java.util.List;

public record AiChatResponse(
        String aiMessage, // Lời chào, tư vấn của con AI (VD: "Em tìm thấy mấy căn view đồi thông cực chill cho anh nè!")
        List<GlobalSearchResponse> suggestedRooms // Mảng data để Frontend vẽ UI Card
) {}