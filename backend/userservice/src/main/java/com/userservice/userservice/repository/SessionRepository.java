package com.userservice.userservice.repository;
import com.userservice.userservice.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
public interface SessionRepository extends JpaRepository<UserSession,String> { long deleteByExpiresAtBefore(Instant now); long deleteByUserIdAndTokenHashNot(Long userId,String tokenHash); }
