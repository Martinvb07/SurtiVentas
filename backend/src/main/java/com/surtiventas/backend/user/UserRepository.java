package com.surtiventas.backend.user;

import com.surtiventas.backend.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleAndActiveTrue(Role role);

    /** The store linked to a buyer account, resolved without lazy-loading. */
    @Query("select u.customer from User u where u.id = :userId")
    Optional<Customer> findCustomerByUserId(@Param("userId") Long userId);
}
