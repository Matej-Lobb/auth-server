package sk.mlobb.authserver.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordEncoderConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        final SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextInt(32);
        return new BCryptPasswordEncoder(6, secureRandom);
    }
}
