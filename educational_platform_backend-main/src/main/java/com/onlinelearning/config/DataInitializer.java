package com.onlinelearning.config;

import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.entity.UserType;
import com.onlinelearning.repository.UserAccountRepository;
import com.onlinelearning.repository.UserTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Создаем типы пользователей
        if (userTypeRepository.count() == 0) {
            UserType admin = new UserType("ADMIN");
            UserType instructor = new UserType("INSTRUCTOR");
            UserType student = new UserType("STUDENT");

            userTypeRepository.save(admin);
            userTypeRepository.save(instructor);
            userTypeRepository.save(student);

            System.out.println("User types initialized successfully");
        }

        // Создаем тестового администратора (опционально)
        if (!userAccountRepository.findByEmail("admin@example.com").isPresent()) {
            UserType adminType = userTypeRepository.findByTypeName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN type not found"));

            UserAccount admin = new UserAccount();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Администратор");
            admin.setLastName("Системы");
            admin.setUserType(adminType);

            userAccountRepository.save(admin);
            System.out.println("Test admin created");
        }
    }
}