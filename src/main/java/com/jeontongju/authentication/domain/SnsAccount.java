package com.jeontongju.authentication.domain;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sns_account")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SnsAccount {

  @Id
  @Column(name = "sns_unique_id")
  private String snsUniqueId;

  @Column(name = "oauth_provider", nullable = false)
  private String oauthProvider;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  public static SnsAccount register(String snsUniqueId, String oauthProvider, Member member) {
    return SnsAccount.builder()
            .snsUniqueId(snsUniqueId)
            .oauthProvider(oauthProvider)
            .member(member)
            .build();
  }
}
