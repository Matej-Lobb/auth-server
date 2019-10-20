package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.ApplicationRole;

@Repository
public interface ApplicationRolesRepository extends JpaRepository<ApplicationRole, Long> {

    void deleteByRoleId(Long roleId);
}
