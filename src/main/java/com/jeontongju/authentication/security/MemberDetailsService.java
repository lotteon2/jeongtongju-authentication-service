package com.jeontongju.authentication.security;

import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.utils.CustomErrMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

  private MemberRoleEnum memberRole;
  private final MemberRepository memberRepository;

  @Override
  public MemberDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    log.info("MemberDetailsService's loadUserByUsername executes");
    Member member =
        memberRepository
            .findByUsernameAndMemberRoleEnum(username, memberRole)
            .orElseThrow(
                () ->
                    new AuthenticationException(CustomErrMessage.NOT_FOUND_MEMBER) {
                      @Override
                      public String getMessage() {
                        return super.getMessage();
                      }
                    });

    return new MemberDetails(member);
  }

  public void assignMemberRole(MemberRoleEnum memberRole) {
    this.memberRole = memberRole;
  }
}
