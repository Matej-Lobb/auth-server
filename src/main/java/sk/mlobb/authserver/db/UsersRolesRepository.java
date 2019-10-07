package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.UserRoles;

@Repository
public interface UsersRolesRepository extends JpaRepository<UserRoles, Long> {
}
