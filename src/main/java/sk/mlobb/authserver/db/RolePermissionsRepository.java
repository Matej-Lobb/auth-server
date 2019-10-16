package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.RolePermissions;

import java.util.List;

@Repository
public interface RolePermissionsRepository extends JpaRepository<RolePermissions, Long> {

    List<RolePermissions> findAllByRoleId(Long id);
}
