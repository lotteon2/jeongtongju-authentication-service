package com.jeontongju.authentication.repository;

import com.jeontongju.authentication.domain.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, String> {

  Optional<Member> findByUsername(String username);

  Optional<Member> findByMemberId(Long memberId);

  Optional<Member> findByUsernameAndMemberRoleEnum(String email, MemberRoleEnum memberRole);

  List<Member> findByMemberRoleEnumAndIsDeletedAndCreatedAtAfter(
      MemberRoleEnum memberRole, Boolean isDeleted, LocalDateTime currentDate);

  List<Member> findByIsDeletedAndCreatedAtAfter(boolean isDeleted, LocalDateTime currentDate);

  @Query(
      "SELECT FUNCTION('DATE', m.createdAt) AS registrationDay, COUNT(m) AS memberCount "
          + "FROM Member m "
          + "WHERE m.createdAt >= :aWeekAgo "
          + "AND m.memberRoleEnum = :memberRoleEnum "
          + "GROUP BY FUNCTION('DATE', m.createdAt) "
          + "ORDER BY FUNCTION('DATE', m.createdAt)")
  List<Object[]> getMemberCountsByCreatedAtAndMemberRoleEnumFromAWeekAgo(
      @Param("aWeekAgo") LocalDateTime aWeekAgo,
      @Param("memberRoleEnum") MemberRoleEnum memberRoleEnum);
}
