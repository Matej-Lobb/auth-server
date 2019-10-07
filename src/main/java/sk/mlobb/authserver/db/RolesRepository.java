package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.Role;

@Repository
public interface RolesRepository extends JpaRepository<Role, Integer> {

    Role findByRole(String role);
}
