package com.bankwise.security.principal;

import com.bankwise.auth.domain.AccountStatus;
import com.bankwise.auth.domain.Role;
import com.bankwise.auth.domain.User;

import lombok.Getter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public final class CustomUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final Set<Role> roles;
    private final AccountStatus accountStatus;
    private final boolean emailVerified;

    public CustomUserPrincipal(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.roles = user.getRoles() != null
                ? Set.copyOf(user.getRoles())
                : Collections.emptySet();
        this.accountStatus = user.getAccountStatus();
        this.emailVerified = user.isEmailVerified();
    }

    public static CustomUserPrincipal fromUser(User user) {
        Objects.requireNonNull(user, "User must not be null");
        return new CustomUserPrincipal(user);
    }


    public UUID getUserId() {
        return userId;
    }


    @Override
    public String getUsername() {
        return email;
    }


    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return roles.stream()
                .filter(Objects::nonNull)
                .map(Role::name)
                .map(role ->
                        new SimpleGrantedAuthority("ROLE_" + role)
                )
                .collect(Collectors.toUnmodifiableSet());
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE && emailVerified;
    }
}