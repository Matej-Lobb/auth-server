package sk.mlobb.authserver.app.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.UserEntity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class DefaultAuthenticationProvider implements AuthenticationProvider {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAuthenticationProvider(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) {
        log.debug("Performing authentication ...");
        if (authentication.getName() == null || authentication.getCredentials() == null) {
            return null;
        }

        if (authentication.getName().isEmpty() || authentication.getCredentials().toString().isEmpty()) {
            return null;
        }

        boolean username = false;
        String identification = authentication.getName();
        UserEntity userEntity = usersRepository.findByEmailIgnoreCase(identification);
        if (userEntity == null) {
            userEntity = usersRepository.findByUsernameIgnoreCase(identification);
            username = true;
            validateObject(identification, userEntity == null, "No user present with identification: %s");
        }

        validateObject(identification, Boolean.FALSE.equals(userEntity.getActive()), "User %s is not active !");

        final String providedUserIdentification = authentication.getName();
        final Object providedUserPassword = authentication.getCredentials();

        String dbIdentification = getIdentification(username, userEntity);

        return validateCredentials(userEntity, providedUserIdentification, providedUserPassword, dbIdentification);
    }

    private Authentication validateCredentials(UserEntity userEntity, String providedUserIdentification, Object providedUserPassword, String dbIdentification) {
        if (providedUserIdentification.equalsIgnoreCase(dbIdentification)
                && passwordEncoder.matches(providedUserPassword.toString(), userEntity.getPassword())) {
            return new UsernamePasswordAuthenticationToken(userEntity.getUsername(), userEntity.getPassword(),
                    constructAuthorities(userEntity));
        } else {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
    }

    private String getIdentification(boolean username, UserEntity userEntity) {
        String dbIdentification;
        if (username) {
            dbIdentification = userEntity.getUsername();
        } else {
            dbIdentification = userEntity.getEmail();
        }
        return dbIdentification;
    }

    private void validateObject(String identification, boolean equals, String s) {
        if (equals) {
            throw new UsernameNotFoundException(String.format(s, identification));
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private List<GrantedAuthority> constructAuthorities(UserEntity userEntity) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (RoleEntity roleEntity : userEntity.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(roleEntity.getRole()));
        }
        return grantedAuthorities;
    }
}
