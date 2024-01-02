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

  private final MemberRepository memberRepository;

  @Override
  public MemberDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    String[] usernameBits = username.split("_", 2);
    String email = usernameBits[0];
    String role = usernameBits[1];

    MemberRoleEnum memberRoleEnum;
    if(role.equals("ROLE_CONSUMER")) {
        memberRoleEnum = MemberRoleEnum.ROLE_CONSUMER;
    } else if(role.equals("ROLE_SELLER")) {
        memberRoleEnum = MemberRoleEnum.ROLE_SELLER;
    } else {
        memberRoleEnum = MemberRoleEnum.ROLE_ADMIN;
    }

    log.info("MemberDetailsService's loadUserByUsername executes");
    Member member =
        memberRepository
            .findByUsernameAndMemberRoleEnum(email, memberRoleEnum)
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
}
