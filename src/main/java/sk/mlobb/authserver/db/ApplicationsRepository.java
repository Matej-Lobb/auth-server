package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.ApplicationEntity;

@Repository
public interface ApplicationsRepository extends JpaRepository<ApplicationEntity, Long> {

    ApplicationEntity findByName(String name);
    ApplicationEntity findByUid(String uid);
}
