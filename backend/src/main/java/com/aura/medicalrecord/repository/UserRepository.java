package com.aura.medicalrecord.repository;

import com.aura.medicalrecord.domain.Role;
import com.aura.medicalrecord.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleOrderByNameAsc(Role role);
}
