package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.UserEntity;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByEmailIgnoreCase(String email);
    UserEntity findByUsernameIgnoreCase(String username);
}