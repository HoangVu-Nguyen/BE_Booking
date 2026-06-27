package clyvasync.Clyvasync.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class KycSubmittedEvent {
    private Long profileId;
    private List<Long> documentIds;
}
