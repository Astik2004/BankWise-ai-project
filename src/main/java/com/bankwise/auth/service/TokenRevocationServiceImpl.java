package com.bankwise.auth.service;

import com.bankwise.auth.domain.RevokedToken;
import com.bankwise.auth.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenRevocationServiceImpl implements TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    public void revoke(String tokenId, Instant expirationTime) {

        validateTokenData(tokenId, expirationTime);

        if (isRevoked(tokenId)) {
            return;
        }

        RevokedToken revokedToken = new RevokedToken();

        revokedToken.setTokenId(tokenId);
        revokedToken.setExpiresAt(expirationTime);
        revokedToken.setRevokedAt(Instant.now());

        try {
            revokedTokenRepository.save(revokedToken);
        } catch (DuplicateKeyException exception) {
            return;
        }
    }

    @Override
    public boolean isRevoked(String tokenId) {

        if (!StringUtils.hasText(tokenId)) {
            return false;
        }

        return revokedTokenRepository.existsByTokenId(tokenId);
    }

    private void validateTokenData(String tokenId, Instant expirationTime) {

        if (!StringUtils.hasText(tokenId)) {
            throw new IllegalArgumentException("Token ID must not be empty");
        }

        if (expirationTime == null) {
            throw new IllegalArgumentException("Token expiration time must not be null");
        }

        if (!expirationTime.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Token expiration time must be in the future");
        }
    }
}