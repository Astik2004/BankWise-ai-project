package com.bankwise.auth.repository;

import com.bankwise.auth.domain.RevokedToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RevokedTokenRepository extends MongoRepository<RevokedToken, String> {

    boolean existsByTokenId(String tokenId);
    Optional<RevokedToken> findByTokenId(String tokenId);
}
