package com.jeontongju.authentication.repository;

import com.jeontongju.authentication.entity.SnsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnsAccountRepository extends JpaRepository<SnsAccount, String> {}
