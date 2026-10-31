package com.bankwise.auth.config;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.AuthProvider;
import com.bankwise.auth.domain.Role;
import com.bankwise.auth.domain.User;
import com.bankwise.auth.repository.UserRepository;
import com.bankwise.knowledgebase.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KnowledgeBaseService knowledgeBaseService;

    @Override
    public void run(String... args) {
        log.info("Running AdminDataInitializer to verify and initialize system administrator accounts...");

        // Ensure primary admin account exists and has guaranteed ADMIN role & password
        upsertAdminUser("admin@gmail.com", "Admin@123", "Bank", "Admin");
        upsertAdminUser("admin@bankwise.com", "AdminPassword123", "BankWise", "Admin");
    }

    private void upsertAdminUser(String rawEmail, String rawPassword, String firstName, String lastName) {
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);

        try {
            Optional<User> existingUserOpt = userRepository.findByEmail(email);

            if (existingUserOpt.isPresent()) {
                User user = existingUserOpt.get();
                log.info("User [{}] exists in database. Elevating to ADMIN role and updating credentials...", email);

                Set<Role> roles = user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>();
                roles.add(Role.ADMIN);
                roles.add(Role.USER);

                user.setRoles(roles);
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
                user.setAccountStatus(AccountStatus.ACTIVE);
                user.setEmailVerified(true);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                user.setPasswordChangedAt(Instant.now());

                User saved = userRepository.save(user);
                initKnowledgeBase(saved);
                log.info("Admin account [{}] updated successfully with guaranteed ADMIN privileges.", email);

            } else {
                log.info("User [{}] not found. Creating new administrator account...", email);

                User newAdmin = new User();
                newAdmin.setEmail(email);
                newAdmin.setFirstName(firstName);
                newAdmin.setLastName(lastName);
                newAdmin.setPasswordHash(passwordEncoder.encode(rawPassword));
                newAdmin.setRoles(Set.of(Role.ADMIN, Role.USER));
                newAdmin.setAccountStatus(AccountStatus.ACTIVE);
                newAdmin.setEmailVerified(true);
                newAdmin.setAuthProvider(AuthProvider.LOCAL);
                newAdmin.setFailedLoginAttempts(0);
                newAdmin.setLockedUntil(null);
                newAdmin.setPasswordChangedAt(Instant.now());

                User saved = userRepository.save(newAdmin);
                initKnowledgeBase(saved);
                log.info("New admin account [{}] created successfully with password: [{}]", email, rawPassword);
            }
        } catch (Exception e) {
            log.error("Failed to initialize or update admin account [{}]: {}", email, e.getMessage(), e);
        }
    }

    private void initKnowledgeBase(User user) {
        try {
            knowledgeBaseService.getOrCreateDefault(user.getId());
        } catch (Exception e) {
            log.warn("Could not create default knowledge base for user [{}]: {}", user.getEmail(), e.getMessage());
        }
    }
}
