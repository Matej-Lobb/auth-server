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
import org.springframework.transaction.annotation.Transactional;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;

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
    @Transactional
    public Authentication authenticate(final Authentication authentication) {
        if (authentication.getName() == null || authentication.getCredentials() == null) {
            return null;
        }

        if (authentication.getName().isEmpty() || authentication.getCredentials().toString().isEmpty()) {
            return null;
        }

        boolean username = false;
        String identification = authentication.getName();
        User user = usersRepository.findByEmailIgnoreCase(identification);
        if (user == null) {
            user = usersRepository.findByUsernameIgnoreCase(identification);
            username = true;
            validateObject(identification, user == null, "No user present with identification: %s");
        }

        validateObject(identification, Boolean.FALSE.equals(user.getActive()), "User %s is not active !");

        final String providedUserIdentification = authentication.getName();
        final Object providedUserPassword = authentication.getCredentials();

        String dbIdentification = getIdentification(username, user);

        return validateCredentials(user, providedUserIdentification, providedUserPassword, dbIdentification);
    }

    private Authentication validateCredentials(User user, String providedUserIdentification, Object providedUserPassword, String dbIdentification) {
        if (providedUserIdentification.equalsIgnoreCase(dbIdentification)
                && passwordEncoder.matches(providedUserPassword.toString(), user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(),
                    constructAuthorities(user));
        } else {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
    }

    private String getIdentification(boolean username, User user) {
        String dbIdentification;
        if (username) {
            dbIdentification = user.getUsername();
        } else {
            dbIdentification = user.getEmail();
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

    private List<GrantedAuthority> constructAuthorities(User user) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getRole()));
        }
        return grantedAuthorities;
    }
}
