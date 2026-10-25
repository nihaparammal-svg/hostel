package com.hostel.hostelmanagement.config;
import com.hostel.hostelmanagement.model.Admin;
import com.hostel.hostelmanagement.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class AdminDataInitializer implements CommandLineRunner {
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();
    public AdminDataInitializer(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    @Override
    public void run(String... args) {
        if (adminRepository.findByUsername("admin").isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(
                    passwordEncoder.encode("admin123")
            );
            adminRepository.save(admin);
            System.out.println(
                    "DEFAULT ADMIN CREATED: username=admin"
            );
        }
    }
}