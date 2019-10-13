package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.User;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {

    User findByEmailIgnoreCase(String email);
    User findByUsernameIgnoreCase(String username);
}