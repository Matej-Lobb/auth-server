package sk.mlobb.authserver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableResourceServer
@EnableTransactionManagement
@EntityScan(basePackages = {"sk.mlobb"})
@EnableJpaRepositories(basePackages = {"sk.mlobb"})
@SpringBootApplication(scanBasePackages = "sk.mlobb")
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
