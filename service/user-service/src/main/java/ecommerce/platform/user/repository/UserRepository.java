package ecommerce.platform.user.repository;

import ecommerce.platform.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserName(String username);

    boolean existsByUserName(String username);

    void deleteByUserName(String username);
}