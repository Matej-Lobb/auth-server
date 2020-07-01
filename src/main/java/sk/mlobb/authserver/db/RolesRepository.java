package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.RoleEntity;

@Repository
public interface RolesRepository extends JpaRepository<RoleEntity, Long> {

    RoleEntity findByRole(String role);
}
