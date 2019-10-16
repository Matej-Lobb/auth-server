package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.RolePermissions;

@Repository
public interface RolePermissionsRepository extends JpaRepository<RolePermissions, Integer> {

}
