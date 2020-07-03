package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.UserRoleEntity;

@Repository
public interface UserRolesRepository extends JpaRepository<UserRoleEntity, Long> {
}
