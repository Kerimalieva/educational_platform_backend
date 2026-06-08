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
        if (userTypeRepository.count() == 0) {
            UserType admin = new UserType("ADMIN");
            UserType instructor = new UserType("INSTRUCTOR");
            UserType student = new UserType("STUDENT");

            userTypeRepository.save(admin);
            userTypeRepository.save(instructor);
            userTypeRepository.save(student);

            System.out.println("User types initialized successfully");
        }

        if (!userAccountRepository.findByEmail("admin@example.com").isPresent()) {
            UserType adminType = userTypeRepository.findByTypeName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN type not found"));

            UserAccount admin = new UserAccount();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setUserType(adminType);
            userAccountRepository.save(admin);
            System.out.println("Test admin created");
        }

        if (!userAccountRepository.findByEmail("instructor@test.com").isPresent()) {
            UserType instructorType = userTypeRepository.findByTypeName("INSTRUCTOR")
                    .orElseThrow(() -> new RuntimeException("INSTRUCTOR type not found"));

            UserAccount instructor = new UserAccount();
            instructor.setEmail("instructor@test.com");
            instructor.setPassword(passwordEncoder.encode("Password123!"));
            instructor.setFirstName("John");
            instructor.setLastName("Doe");
            instructor.setUserType(instructorType);
            userAccountRepository.save(instructor);
            System.out.println("Test instructor created");
        }

        if (!userAccountRepository.findByEmail("student@test.com").isPresent()) {
            UserType studentType = userTypeRepository.findByTypeName("STUDENT")
                    .orElseThrow(() -> new RuntimeException("STUDENT type not found"));

            UserAccount student = new UserAccount();
            student.setEmail("student@test.com");
            student.setPassword(passwordEncoder.encode("Password123!"));
            student.setFirstName("Jane");
            student.setLastName("Smith");
            student.setUserType(studentType);
            userAccountRepository.save(student);
            System.out.println("Test student created");
        }
    }
}