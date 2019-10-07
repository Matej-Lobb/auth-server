package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.model.License;
import sk.mlobb.authserver.model.LicenseType;
import sk.mlobb.authserver.model.Token;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.exception.InvalidRequestException;
import sk.mlobb.authserver.model.exception.UnauthorizedException;
import sk.mlobb.authserver.model.rest.auth.AuthRequest;
import sk.mlobb.authserver.model.rest.auth.AuthResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AuthorizationService {

    @Value("${authorization.tokens.refreshTokenValidity}")
    private long refreshTokenValidityTime;

    @Value("${authorization.tokens.tokenValidityTime}")
    private long tokenValidityTime;

    private final LicenseService licenseService;
    private final TokenService tokenService;

    @Autowired
    public AuthorizationService(TokenService tokenService, LicenseService licenseService) {
        this.licenseService = licenseService;
        this.tokenService = tokenService;
    }

    public AuthResponse authorize(AuthRequest authRequest) throws InvalidRequestException, UnauthorizedException {
        if (authRequest == null) {
            throw new InvalidRequestException("Received invalid authorize request !");
        }
        return constructAuthResponse(authorizeWithLicense(authRequest));
    }

    public AuthResponse refreshToken(String refreshToken) throws InvalidRequestException, UnauthorizedException {
        log.info("Refreshing token with refresh token: {}", refreshToken);
        Token token = tokenService.getTokenByRefreshToken(refreshToken);
        if (token == null) {
            throw new InvalidRequestException("Invalid refresh token!");
        }
        long remainingRefreshTokenValidity =
                refreshTokenValidityTime - getRemainingSeconds(token.getRefreshTokenValidity());
        if (remainingRefreshTokenValidity <= 0) {
            tokenService.removeToken(token);
            throw new UnauthorizedException("Refresh token expired!");
        }
        log.debug("Refreshing user token. Username: {} refresh token: {} remaining validity: {}",
                token.getUser().getUsername(), refreshToken, remainingRefreshTokenValidity);
        token.setToken(UUID.randomUUID().toString());
        token.setTokenValidity(LocalDateTime.now());
        token = tokenService.updateToken(token);
        return AuthResponse.builder()
                .refreshToken(refreshToken)
                .refreshTokenValidity(remainingRefreshTokenValidity)
                .token(token.getToken())
                .tokenValidity(tokenValidityTime)
                .info("OK")
                .build();
    }

    public AuthResponse checkToken(String rawToken) throws InvalidRequestException, UnauthorizedException {
        log.info("Checking token: {}", rawToken);
        Token token = tokenService.getToken(rawToken);
        if (token == null) {
            throw new InvalidRequestException("Invalid token!");
        }
        long remainingTokenValidity = tokenValidityTime - getRemainingSeconds(token.getTokenValidity());
        if (remainingTokenValidity <= 0) {
            throw new UnauthorizedException("Token expired!");
        }
        long remainingRefreshTokenValidity =
                refreshTokenValidityTime - getRemainingSeconds(token.getRefreshTokenValidity());
        log.debug("Token: {} is ok, validity: {} refresh token validity {}", token.getToken(), remainingTokenValidity,
                remainingRefreshTokenValidity);
        return AuthResponse.builder()
                .token(token.getToken())
                .tokenValidity(remainingTokenValidity)
                .refreshToken(token.getRefreshToken())
                .refreshTokenValidity(remainingRefreshTokenValidity)
                .info("OK")
                .build();
    }

    private License authorizeWithLicense(AuthRequest authRequest) throws UnauthorizedException {
        String rawLicense = authRequest.getLicense();
        log.info("Authenticating license: {}", rawLicense);
        License result = licenseService.getLicense(rawLicense);
        if (result == null) {
            throw new UnauthorizedException("Invalid license!");
        }
        return result;
    }

    private long getRemainingSeconds(LocalDateTime localDateTime) {
        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(localDateTime, end);
        return duration.getSeconds();
    }

    private AuthResponse constructAuthResponse(License license) {
        LicenseType licenseType = license.getLicenseType();
        User user = license.getUser();
        log.debug("Constructing authorize response for user: {} license type: {}", user.getUsername(), licenseType);
        tokenService.removeToken(user.getId());
        Token token = tokenService.createToken(user, licenseType);
        return AuthResponse.builder()
                .refreshTokenValidity(refreshTokenValidityTime)
                .tokenValidity(tokenValidityTime)
                .refreshToken(token.getRefreshToken())
                .token(token.getToken())
                .info("OK")
                .build();
    }
}
