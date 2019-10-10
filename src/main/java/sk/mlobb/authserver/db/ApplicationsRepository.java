package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.Application;

@Repository
public interface ApplicationsRepository extends JpaRepository<Application, Long> {

    Application findByName(String name);
    Application findByUid(String uid);
}
