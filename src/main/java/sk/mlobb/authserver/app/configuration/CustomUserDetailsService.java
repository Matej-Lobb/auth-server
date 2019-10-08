package sk.mlobb.authserver.app.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Custom user details service.
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Autowired
    public CustomUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("Loading user: {} !", username);
        User user = usersRepository.findByEmailIgnoreCase(username);
        if (user == null) {
            user = usersRepository.findByUsernameIgnoreCase(username);
            if (user == null) {
                throw new UsernameNotFoundException("No user present with username: " + username);
            }
        }
        log.debug("User: {}", user);
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                constructAuthorities(user));
    }

    private List<GrantedAuthority> constructAuthorities(User user) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getRole()));
        }
        return grantedAuthorities;
    }
}
