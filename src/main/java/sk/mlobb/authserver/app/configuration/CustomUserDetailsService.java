package sk.mlobb.authserver.app.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.UserEntity;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service("userDetailsService")
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String identification) {

        UserEntity userEntity = usersRepository.findByEmailIgnoreCase(identification);
        if (userEntity == null) {
            userEntity = usersRepository.findByUsernameIgnoreCase(identification);
        }
        validateObject(identification, userEntity == null, "No user present with identification: %s");

        return new org.springframework.security.core.userdetails.User(
                userEntity.getEmail(), userEntity.getPassword(), userEntity.getActive(), true, true,
                true, constructAuthorities(userEntity));
    }

    private void validateObject(String identification, boolean equals, String s) {
        if (equals) {
            throw new UsernameNotFoundException(String.format(s, identification));
        }
    }

    private List<GrantedAuthority> constructAuthorities(UserEntity userEntity) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (RoleEntity roleEntity : userEntity.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(roleEntity.getRole()));
        }
        return grantedAuthorities;
    }
}