package com.bankwise.auth.service;

import java.time.Instant;

public interface TokenRevocationService {

    void revoke(String tokenId, Instant expirationTime);
    boolean isRevoked(String tokenId);
}
