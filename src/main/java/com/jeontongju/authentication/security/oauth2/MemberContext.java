package com.jeontongju.authentication.security.oauth2;

import com.jeontongju.authentication.entity.Member;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class MemberContext extends User implements OAuth2User {

  private final Long memberId;
  private final String email;
  //    private final String profileImageUrl;

  private Map<String, Object> attributes;
  private String userNameAttributeName;

  public MemberContext(Member member, Collection<? extends GrantedAuthority> authorities) {

    super(member.getUsername(), member.getPassword(), authorities);
    this.memberId = member.getMemberId();
    this.email = member.getUsername();
  }

  public MemberContext(
      Member member,
      Collection<? extends GrantedAuthority> authorities,
      Map<String, Object> attributes,
      String userNameAttributeName) {

    this(member, authorities);
    this.attributes = attributes;
    this.userNameAttributeName = userNameAttributeName;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.attributes;
  }

  @Override
  public String getName() {
    return null;
  }
}
