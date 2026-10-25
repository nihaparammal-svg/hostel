package com.hostel.hostelmanagement.controller;
import com.hostel.hostelmanagement.model.Student;
import com.hostel.hostelmanagement.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class StudentController {
    private final StudentRepository studentRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();
    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    // =========================
    // STUDENT REGISTRATION PAGE
    // =========================
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("student", new Student());
        return "register";
    }
    // =========================
    // PROCESS REGISTRATION
    // =========================
    @PostMapping("/register")
    public String processRegistration(
            @RequestParam("name") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("studentId") String studentId,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        // Check duplicate email
        if (studentRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "This email is already registered."
            );
            return "redirect:/register";
        }
        // Check duplicate student ID
        if (studentRepository.findByStudentId(studentId).isPresent()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "This Student ID is already registered."
            );
            return "redirect:/register";
        }
        // Create student
        Student student = new Student();
        student.setFullName(fullName);
        student.setEmail(email);
        student.setPhone(phone);
        student.setStudentId(studentId);
        // Encrypt password
        student.setPassword(
                passwordEncoder.encode(password)
        );
        // Save student
        Student savedStudent =
                studentRepository.save(student);
        // Store the newly registered student's ID
        session.setAttribute(
                "loggedInStudentId",
                savedStudent.getId()
        );
        // Go to that student's dashboard
        return "redirect:/student-dashboard";
    }
    // =========================
    // STUDENT LOGIN PAGE
    // =========================
    @GetMapping("/student-login")
    public String showStudentLogin() {
        return "student-login";
    }
    // =========================
    // PROCESS STUDENT LOGIN
    // =========================
    @PostMapping("/student-login")
    public String processStudentLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {
        return studentRepository.findByEmail(email)
                .filter(student ->
                        passwordEncoder.matches(
                                password,
                                student.getPassword()
                        )
                )
                .map(student -> {
                    // Store THIS student's ID
                    session.setAttribute(
                            "loggedInStudentId",
                            student.getId()
                    );
                    System.out.println(
                            "STUDENT LOGIN: "
                                    + student.getFullName()
                                    + " | ID = "
                                    + student.getId()
                    );
                    return "redirect:/student-dashboard";
                })
                .orElseGet(() -> {
                    model.addAttribute(
                            "error",
                            "Invalid email or password."
                    );
                    return "student-login";
                });
    }
    // =========================
    // STUDENT DASHBOARD
    // =========================
    @GetMapping("/student-dashboard")
    public String studentDashboard(
            HttpSession session,
            Model model) {
        // Get the logged-in student's ID
        Object loggedInStudentId =
                session.getAttribute("loggedInStudentId");
        // No student logged in
        if (loggedInStudentId == null) {
            return "redirect:/student-login";
        }
        // Convert session ID to Long
        Long studentId;
        try {
            studentId = Long.valueOf(
                    loggedInStudentId.toString()
            );
        } catch (NumberFormatException e) {
            session.invalidate();
            return "redirect:/student-login";
        }
        // Find THIS student from database
        Student student =
                studentRepository.findById(studentId)
                        .orElse(null);
        // Student not found
        if (student == null) {
            session.invalidate();
            return "redirect:/student-login";
        }
        // Send the logged-in student to Thymeleaf
        model.addAttribute(
                "student",
                student
        );
        System.out.println(
                "DASHBOARD STUDENT: "
                        + student.getFullName()
                        + " | ID = "
                        + student.getId()
        );
        return "student-dashboard";
    }
    // =========================
    // STUDENT LOGOUT
    // =========================
    @GetMapping("/student-logout")
    public String studentLogout(
            HttpSession session) {
        session.invalidate();
        return "redirect:/student-login";
    }
}