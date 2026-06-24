package clyvasync.Clyvasync.service.ai;

public interface SearchSyncService {
     void syncRoomToIndex(Long roomId, Long homestayId, String homestayName, String city, int beds, int guests, double price, String allAmenitiesText);
     void triggerSyncForRoom(Long roomId);
}
