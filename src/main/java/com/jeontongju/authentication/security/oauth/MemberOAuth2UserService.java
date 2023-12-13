package com.jeontongju.authentication.security.oauth;

import com.jeontongju.authentication.dto.request.ConsumerInfoForCreateBySnsRequestDto;
import com.jeontongju.authentication.dto.response.oauth.google.GoogleUserInfo;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoUserInfo;
import com.jeontongju.authentication.dto.response.oauth.OAuth2UserInfo;
import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.enums.SnsTypeEnum;
import com.jeontongju.authentication.exception.MemberNotFoundException;
import com.jeontongju.authentication.mapper.MemberMapper;
import com.jeontongju.authentication.mapper.SnsAccountMapper;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.repository.SnsAccountRepository;
import com.jeontongju.authentication.security.MemberDetails;
import com.jeontongju.authentication.service.feign.consumer.ConsumerClientService;
import com.jeontongju.authentication.utils.CustomErrMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

    OAuth2UserInfo oAuth2UserInfo = null;
    String oauthType = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
    String oauthId = oAuth2User.getName();

    if (oauthType.equals("KAKAO")) {
      oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes(), oauthId);
    } else if (oauthType.equals("GOOGLE")) {
      oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes(), oauthId);
    } else {
      throw new RuntimeException();
    }

    Member member = null;
    if (isNew(oauthType, oauthId)) {

      switch (oauthType) {
        case "KAKAO":
          String kakaoEmail = oAuth2UserInfo.getEmail();
          String kakaoId = oAuth2UserInfo.getProviderId();
          String kakaoProfileImageUrl = oAuth2UserInfo.getProfileImageUrl();

          member =
              registerMemberFromSns(kakaoEmail, SnsTypeEnum.KAKAO, kakaoId, kakaoProfileImageUrl);
          break;

        case "GOOGLE":
          String googleEmail = oAuth2UserInfo.getEmail();
          String googleId = oAuth2UserInfo.getProviderId();
          String googleProfileImageUrl = oAuth2UserInfo.getProfileImageUrl();

          member =
              registerMemberFromSns(
                  googleEmail, SnsTypeEnum.GOOGLE, googleId, googleProfileImageUrl);
      }
    } else { // 기존 회원
      member =
          memberRepository
              .findByUsername(oAuth2UserInfo.getEmail())
              .orElseThrow(() -> new MemberNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    }

    return new MemberDetails(member, oAuth2User.getAttributes());
  }

  @NotNull
  private Member registerMemberFromSns(
      String email, SnsTypeEnum snsTypeEnum, String snsUniqueId, String profileImageUrl) {

    Member savedMember =
        memberRepository.save(memberMapper.toEntity(email, "", MemberRoleEnum.ROLE_CONSUMER));

    snsAccountRepository.save(
        snsAccountMapper.toEntity(
            snsTypeEnum.name() + "_" + snsUniqueId, snsTypeEnum.name(), savedMember));

    consumerClientService.createConsumerForSignupBySns(
        ConsumerInfoForCreateBySnsRequestDto.toDto(
            savedMember.getMemberId(), savedMember.getUsername(), profileImageUrl));
    return savedMember;
  }

  private boolean isNew(String oauthType, String oauthId) {

    return snsAccountRepository.findBySnsUniqueId(oauthType + "_" + oauthId).isEmpty();
  }
}
