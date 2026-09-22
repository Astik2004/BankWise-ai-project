package com.bankwise.security.authentication;

import com.bankwise.auth.domain.User;
import com.bankwise.auth.repository.UserRepository;
import com.bankwise.security.principal.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String normalizedEmail = normalizeEmail(username);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + normalizedEmail
                ));

        return new CustomUserPrincipal(user);
    }

    private String normalizeEmail(String email) {

        if (!StringUtils.hasText(email)) {
            throw new UsernameNotFoundException(
                    "Email cannot be empty"
            );
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}