package sk.mlobb.authserver.model.authority;

import org.springframework.security.core.GrantedAuthority;
import sk.mlobb.authserver.model.PrivilegeEntity;

import java.util.Set;

public class CustomGrantedAuthority implements GrantedAuthority {

    private final String role;
    private final Set<String> privileges;

    public CustomGrantedAuthority(String role, Set<String> privileges) {
        this.role = role;
        this.privileges = privileges;
    }

    @Override
    public String getAuthority() {
        return role;
    }

    public Set<String> getPrivileges() {
        return privileges;
    }
}
