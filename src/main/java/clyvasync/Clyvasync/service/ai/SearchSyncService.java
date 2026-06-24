package clyvasync.Clyvasync.service.ai;

import clyvasync.Clyvasync.dto.response.HomestaySearchResultResponse;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;

import java.util.List;

public interface SearchSyncService {
     void syncRoomToIndex(Long roomId, Long homestayId, String homestayName, String city, int beds, int guests, double price, String allAmenitiesText);
     void triggerSyncForRoom(Long roomId);
     List<HomestaySearchResultResponse> hybridSearch(String query, int limit);
}
