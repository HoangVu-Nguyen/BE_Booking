package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {
    private List<MessageResponse> messages;
    private Long nextCursor;
    private boolean hasNext;
}