package com.jeontongju.authentication.security;

import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.utils.CustomErrMessage;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public MemberDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Member member =
        memberRepository
            .findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    return new MemberDetails(member);
  }
}
