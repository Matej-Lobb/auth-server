package sk.mlobb.authserver.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.mlobb.authserver.model.Token;

@Repository
public interface TokensRepository extends JpaRepository<Token, Long> {

    Token findByToken(String token);
    Token findByUserId(long userId);
    Token findByRefreshToken(String refreshToken);
}
