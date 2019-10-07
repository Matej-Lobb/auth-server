package sk.mlobb.authserver.model.rest.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    private long refreshTokenValidity;
    private long tokenValidity;
    private String info;
}