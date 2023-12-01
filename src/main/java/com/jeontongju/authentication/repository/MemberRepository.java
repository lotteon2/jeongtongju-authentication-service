package com.jeontongju.authentication.repository;

import com.jeontongju.authentication.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {

    public Optional<Member> findByUsername(String username);
}
