package sk.mlobb.authserver.app.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
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
            if (user == null) {
                throw new UsernameNotFoundException(String.format("No user present with identification: %s",
                        identification));
            }
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new UsernameNotFoundException(String.format("User %s is not active !", identification));
        }

        final String providedUserIdentification = authentication.getName();
        final Object providedUserPassword = authentication.getCredentials();

        String dbIdentification;

        if (username) {
            dbIdentification = user.getUsername();
        } else {
            dbIdentification = user.getEmail();
        }

        if (providedUserIdentification.equalsIgnoreCase(dbIdentification)
                && passwordEncoder.matches(providedUserPassword.toString(), user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(),
                    constructAuthorities(user));
        }
        throw new UsernameNotFoundException("Invalid username or password.");
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
