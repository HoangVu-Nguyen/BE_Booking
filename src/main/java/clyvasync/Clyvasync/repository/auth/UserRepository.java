package clyvasync.Clyvasync.repository.auth;

import clyvasync.Clyvasync.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.username FROM User u WHERE u.id = :userId")
    Optional<String> findUsernameById(@Param("userId") Long userId);
}