package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.repository.room.RoomCalendarRepository;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomCalendarServiceImpl implements RoomCalendarService {
    private final RoomCalendarRepository roomCalendarRepository;

    @Override
    public int lockRoomRange(Long roomId, LocalDate checkIn, LocalDate checkOut, int qty) {
        return roomCalendarRepository.lockRoomRange(roomId, checkIn, checkOut, qty);
    }

    @Override
    public int unlockRoomRange(Long roomId, LocalDate checkIn, LocalDate checkOut, int qty) {
        return roomCalendarRepository.unlockRoomRange(roomId, checkIn, checkOut, qty);
    }

    @Override
    public List<LocalDate> getUnavailableDates(Long roomId, int month, int year) {
        java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        return roomCalendarRepository.findUnavailableDates(roomId, startOfMonth, endOfMonth);
    }
}
