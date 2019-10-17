package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.ApplicationUser;

@Repository
public interface ApplicationUsersRepository extends JpaRepository<ApplicationUser, Long> {

    void deleteByUserId(Long userId);
}
