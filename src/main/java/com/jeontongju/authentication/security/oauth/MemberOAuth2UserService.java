package com.jeontongju.authentication.security.oauth;

import com.jeontongju.authentication.dto.request.ConsumerInfoForCreateBySnsRequestDto;
import com.jeontongju.authentication.dto.response.oauth.google.GoogleUserInfo;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoUserInfo;
import com.jeontongju.authentication.dto.response.oauth.OAuth2UserInfo;
import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.enums.SnsTypeEnum;
import com.jeontongju.authentication.mapper.MemberMapper;
import com.jeontongju.authentication.mapper.SnsAccountMapper;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.repository.SnsAccountRepository;
import com.jeontongju.authentication.security.MemberDetails;
import com.jeontongju.authentication.service.feign.consumer.ConsumerClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberOAuth2UserService extends DefaultOAuth2UserService {

  private final MemberMapper memberMapper;
  private final SnsAccountMapper snsAccountMapper;
  private final MemberRepository memberRepository;
  private final SnsAccountRepository snsAccountRepository;
  private final ConsumerClientService consumerClientService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);

    return processOAuth2User(userRequest, oAuth2User);
  }

  private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

    log.info("oAuth2User: " + oAuth2User);
    OAuth2UserInfo oAuth2UserInfo = null;
    String oauthType = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
    String oauthId = oAuth2User.getName();

    if (oauthType.equals("KAKAO")) {
      oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes(), oauthId);
    } else if (oauthType.equals("GOOGLE")) {
      oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
    } else {
      throw new RuntimeException();
    }

    Member member = null;
    if (isNew(oauthType, oauthId)) {

      switch (oauthType) {
        case "KAKAO":
          String email = oAuth2UserInfo.getEmail();
          String id = oAuth2UserInfo.getProviderId();
          String profileImageUrl = oAuth2UserInfo.getProfileImageUrl();

          member =
              memberRepository.save(memberMapper.toEntity(email, "", MemberRoleEnum.ROLE_CONSUMER));
          snsAccountRepository.save(
              snsAccountMapper.toEntity(
                  SnsTypeEnum.KAKAO.name() + "_" + id, SnsTypeEnum.KAKAO.name(), member));

          consumerClientService.createConsumerForSignupBySns(
              ConsumerInfoForCreateBySnsRequestDto.toDto(
                  member.getMemberId(), member.getUsername(), profileImageUrl));
          break;
      }
    } else {

    }

    return new MemberDetails(member, oAuth2User.getAttributes());
  }

  private boolean isNew(String oauthType, String oauthId) {

    return snsAccountRepository.findBySnsUniqueId(oauthType + "_" + oauthId).isEmpty();
  }
}
