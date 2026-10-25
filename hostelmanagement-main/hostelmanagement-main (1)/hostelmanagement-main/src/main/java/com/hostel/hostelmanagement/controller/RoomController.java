package com.hostel.hostelmanagement.controller;

import com.hostel.hostelmanagement.model.Room;
import com.hostel.hostelmanagement.repository.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/admin/room-management")
    public String roomManagement(HttpSession session, Model model) {
        if (session.getAttribute("loggedInAdminUsername") == null) {
            return "redirect:/login";
        }

        model.addAttribute("room", new Room());
        model.addAttribute("rooms", roomRepository.findAll());

        return "room-management";
    }

    @PostMapping("/admin/rooms/save")
    public String saveRoom(
            @ModelAttribute("room") Room room,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInAdminUsername") == null) {
            return "redirect:/login";
        }

        String roomNumber = room.getRoomNumber() == null
                ? ""
                : room.getRoomNumber().trim();

        if (roomNumber.isEmpty()
                || room.getRoomType() == null
                || room.getRoomType().trim().isEmpty()
                || room.getCapacity() <= 0
                || room.getStatus() == null
                || room.getStatus().trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please enter valid details for every room field."
            );

            return "redirect:/admin/room-management";
        }

        if (roomRepository.existsByRoomNumber(roomNumber)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "That room number already exists."
            );

            return "redirect:/admin/room-management";
        }

        room.setId(null);
        room.setRoomNumber(roomNumber);
        room.setRoomType(room.getRoomType().trim());
        room.setStatus(room.getStatus().trim());

        roomRepository.save(room);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Room added successfully!"
        );

        return "redirect:/admin/room-management";
    }

    @PostMapping("/admin/rooms/delete/{id}")
    public String deleteRoom(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInAdminUsername") == null) {
            return "redirect:/login";
        }

        if (!roomRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Room not found."
            );

            return "redirect:/admin/room-management";
        }

        roomRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Room deleted successfully!"
        );

        return "redirect:/admin/room-management";
    }

    @GetMapping("/available-rooms")
    public String availableRooms(Model model) {
        List<Room> rooms =
                roomRepository.findByStatusIgnoreCase("Available");

        model.addAttribute("rooms", rooms);

        return "available-rooms";
    }
}