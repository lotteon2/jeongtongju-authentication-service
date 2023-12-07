package com.jeontongju.authentication.security.oauth2;

import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2MemberService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);

    String userNameAttributeName =
        userRequest
            .getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();

    Map<String, Object> attributes = oAuth2User.getAttributes();

    String oauthId = oAuth2User.getName();

    Member member = null;
    String oauthType = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

    if (!"KAKAO".equals(oauthType)) {
      throw new RuntimeException();
    }

    if (isNew(oauthType, oauthId)) {
      switch (oauthType) {
        case "KAKAO":
          Map attributesProperties = (Map) attributes.get("properties");
//          Map attributesKakaoAccount = (Map) attributes.get("kakao_account");

          String email = (String) attributesProperties.get("email");
          String username = String.format("KAKAO_%s", oauthId);

          member = Member.builder().username(email).password("").build();

          memberRepository.save(member);
      }
    } else {

    }

    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_CONSUMER"));

    return new MemberContext(member, authorities, attributes, userNameAttributeName);
  }

  private boolean isNew(String oauthType, String oauthId) {
    return memberRepository.findByUsername(String.format("%s_%s", oauthType, oauthId)).isEmpty();
  }
}
