package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.ApplicationServiceUser;

@Repository
public interface ApplicationServiceUsersRepository extends JpaRepository<ApplicationServiceUser, Long> {
}
