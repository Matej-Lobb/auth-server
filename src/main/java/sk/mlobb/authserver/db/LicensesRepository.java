package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.License;
import sk.mlobb.authserver.model.User;

@Repository
public interface LicensesRepository extends JpaRepository<License, Long> {

    License findByUser(User user);
    void deleteByUser(User user);
}
