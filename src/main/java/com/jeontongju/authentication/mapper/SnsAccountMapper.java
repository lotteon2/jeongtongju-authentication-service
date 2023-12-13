package com.jeontongju.authentication.mapper;

import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.entity.SnsAccount;
import org.springframework.stereotype.Component;

@Component
public class SnsAccountMapper {

  public SnsAccount toEntity(String snsUniqueId, String oauthProvider, Member member) {
    return SnsAccount.builder()
        .snsUniqueId(snsUniqueId)
        .oauthProvider(oauthProvider)
        .member(member)
        .build();
  }
}
