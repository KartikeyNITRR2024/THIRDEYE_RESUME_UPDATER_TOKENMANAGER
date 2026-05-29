package com.thirdeye30.resumehelper.tokenmanager.repos;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.thirdeye30.resumehelper.tokenmanager.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Updates only the user's name.
     */
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.name = :name WHERE u.id = :id")
    int updateName(@Param("id") UUID id, @Param("name") String name);

    /**
     * Updates only the user's token value.
     */
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.token = :token WHERE u.id = :id")
    int updateToken(@Param("id") UUID id, @Param("token") Long token);

    /**
     * Fetches only the token field to save memory and processing time.
     */
    @Query("SELECT u.token FROM User u WHERE u.id = :id")
    Optional<Long> findTokenById(@Param("id") UUID id);
    
    /**
     * Deletes all users where createTime is before the provided threshold.
     * Note: @Transactional is required for delete operations.
     */
    @Transactional
    void deleteByCreateTimeBefore(LocalDateTime threshold);
    
    /**
     * Increment the user's token count by a specific amount.
     */
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.token = u.token + :amount WHERE u.id = :id")
    int addToken(@Param("id") UUID id, @Param("amount") Long amount);

    /**
     * Decrement the user's token count by a specific amount.
     */
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.token = u.token - :amount WHERE u.id = :id")
    int subtractToken(@Param("id") UUID id, @Param("amount") Long amount);
}
