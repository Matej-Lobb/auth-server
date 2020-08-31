package sk.mlobb.authserver.app.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.PrivilegeEntity;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.UserEntity;
import sk.mlobb.authserver.model.authority.CustomGrantedAuthority;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private void validateObject(String identification, boolean equals, String message) {
        if (equals) {
            throw new UsernameNotFoundException(String.format(message, identification));
        }
    }

    private List<GrantedAuthority> constructAuthorities(UserEntity userEntity) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (RoleEntity roleEntity : userEntity.getRoles()) {
            grantedAuthorities.add(new CustomGrantedAuthority(roleEntity.getRole(),
                    mapPrivileges(roleEntity.getPrivileges())));
        }
        return grantedAuthorities;
    }

    private Set<String> mapPrivileges(Set<PrivilegeEntity> privileges) {
        Set<String> keyPrivileges = new HashSet<>();
        for (PrivilegeEntity privilegeEntity : privileges) {
            keyPrivileges.add(privilegeEntity.getKey());
        }
        return keyPrivileges;
    }
}