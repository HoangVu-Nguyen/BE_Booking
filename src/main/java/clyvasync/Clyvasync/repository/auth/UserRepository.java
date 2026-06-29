package clyvasync.Clyvasync.repository.auth;

import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.repository.projection.UserNameProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Optional<UserNameProjection> findProjectedById(Long id);
    @Query("SELECT u FROM User u " +
            "JOIN UserRole ur ON u.id = ur.userId " +
            "JOIN Role r ON ur.roleId = r.id " +
            "WHERE r.name = :roleName " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "u.fullName LIKE %:keyword% OR u.email LIKE %:keyword% OR u.phoneNumber LIKE %:keyword%)")
    Page<User> findByRole(@Param("roleName") String roleName,
                          @Param("keyword") String keyword,
                          Pageable pageable);
}