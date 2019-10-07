package sk.mlobb.authserver.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

@Order(1)
@Configuration
public class PasswordEncoderConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        final SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextInt(32);
        return new BCryptPasswordEncoder(6, secureRandom);
    }
}
