package sk.mlobb.authserver.model.rest.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    private Long id;
    private String name;
    private String uid;
    private Role defaultUserRole;
    private Set<User> users;
    private Set<Role> applicationRoles;
    private Set<User> serviceUsers;
}
