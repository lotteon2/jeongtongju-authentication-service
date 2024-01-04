package com.jeontongju.authentication.repository;

import com.jeontongju.authentication.domain.SnsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnsAccountRepository extends JpaRepository<SnsAccount, String> {

  Optional<SnsAccount> findBySnsUniqueId(String snsUniqueId);
}
