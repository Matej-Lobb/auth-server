package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.License;

@Repository
public interface LicensesRepository extends JpaRepository<License, Long> {

    License findByLicense(String hash);
}
