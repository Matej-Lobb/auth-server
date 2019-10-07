package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.TokensRepository;
import sk.mlobb.authserver.model.LicenseType;
import sk.mlobb.authserver.model.Token;
import sk.mlobb.authserver.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class TokenService {

    private final TokensRepository tokensRepository;

    @Autowired
    public TokenService(TokensRepository tokensRepository) {
        this.tokensRepository = tokensRepository;
    }

    public Token createToken(User user, LicenseType licenseType) {
        final Token token = Token.builder()
                .refreshToken(UUID.randomUUID().toString())
                .token(UUID.randomUUID().toString())
                .refreshTokenValidity(LocalDateTime.now())
                .tokenValidity(LocalDateTime.now())
                .licenseType(licenseType)
                .user(user)
                .build();
        log.debug("Created token: {} for user: {} license type: {}", token.getToken(), user.getUsername(), licenseType);
        tokensRepository.save(token);
        return token;
    }

    public void removeToken(long userId) {
        final Token token = tokensRepository.findByUserId(userId);
        if (token != null) {
            log.debug("Removing token : {} assigned for user: {}", token.getToken(), token.getUser().getUsername());
            tokensRepository.deleteById(token.getId());
        }
        log.debug("No token for userId: {}", userId);
    }

    public void removeToken(Token token) {
        tokensRepository.deleteById(token.getId());
    }

    public Token getToken(String token) {
        log.debug("Getting token: {}", token);
        return tokensRepository.findByToken(token);
    }

    public Token getTokenByRefreshToken(String refreshToken) {
        log.debug("Getting refresh token: {}", refreshToken);
        return tokensRepository.findByRefreshToken(refreshToken);
    }

    public Token updateToken(Token token) {
        return tokensRepository.save(token);
    }
}
