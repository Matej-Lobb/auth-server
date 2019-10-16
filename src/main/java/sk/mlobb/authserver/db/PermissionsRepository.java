package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.permission.Permission;

@Repository
public interface PermissionsRepository extends JpaRepository<Permission, Integer> {
}
