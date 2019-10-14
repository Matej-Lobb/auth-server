package sk.mlobb.authserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "uid", unique = true)
    private String uid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "default_role_id")
    private Role defaultUserRole;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "application_users", joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "application_roles", joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> applicationRoles;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "application_service_users", joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> serviceUsers;
}
