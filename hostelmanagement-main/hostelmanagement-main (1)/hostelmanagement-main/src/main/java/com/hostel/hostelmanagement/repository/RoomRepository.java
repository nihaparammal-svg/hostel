package com.hostel.hostelmanagement.repository;

import com.hostel.hostelmanagement.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByStatusIgnoreCase(String status);

    boolean existsByRoomNumber(String roomNumber);
}