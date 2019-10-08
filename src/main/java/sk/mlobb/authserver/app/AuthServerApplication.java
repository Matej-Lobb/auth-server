package sk.mlobb.authserver.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import sk.mlobb.authserver.app.configuration.CustomUserDetailsService;
import sk.mlobb.authserver.db.RolesRepository;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.exception.ConflictException;
import sk.mlobb.authserver.service.UserService;

import java.time.LocalDate;

@EnableTransactionManagement
@EntityScan(basePackages = {"sk.mlobb"})
@EnableJpaRepositories(basePackages = {"sk.mlobb"})
@SpringBootApplication(scanBasePackages = "sk.mlobb")
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

    @Component
    static class DataLoader implements ApplicationRunner {

        @Autowired
        private UserService userService;

        @Autowired
        private RolesRepository rolesRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private void addTestUser() throws ConflictException {
            Role admin = rolesRepository.save(Role.builder()
                    .role("ADMIN")
                    .build());
            userService.createUser(User.builder()
                    .username("test")
                    .password(passwordEncoder.encode("test"))
                    .country("Slovakia")
                    .active(true)
                    .dateOfBirth(LocalDate.now())
                    .email("test@test.sk")
                    .firstName("test")
                    .lastName("test")
                    .keepUpdated(true)
                    .build(), admin);
        }

        @Override
        public void run(ApplicationArguments args) throws Exception {
            addTestUser();
        }
    }
}
