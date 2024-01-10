package com.jeontongju.authentication.repository;

import com.jeontongju.authentication.domain.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {

  Optional<Member> findByUsername(String username);

  Optional<Member> findByMemberId(Long memberId);

  Optional<Member> findByUsernameAndMemberRoleEnum(String email, MemberRoleEnum memberRole);

  List<Member> findByMemberRoleEnumAndCreatedAtAfter(
      MemberRoleEnum memberRole, LocalDateTime currentDate);

  List<Member> findByIsDeletedAndCreatedAtAfter(boolean isDeleted, LocalDateTime currentDate);
}
