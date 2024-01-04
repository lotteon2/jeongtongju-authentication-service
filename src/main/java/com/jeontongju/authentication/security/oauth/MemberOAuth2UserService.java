package com.jeontongju.authentication.security.oauth;

import com.jeontongju.authentication.dto.response.oauth.OAuth2UserInfo;
import com.jeontongju.authentication.dto.response.oauth.google.GoogleUserInfo;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoUserInfo;
import com.jeontongju.authentication.dto.temp.ConsumerInfoForCreateBySnsRequestDto;
import com.jeontongju.authentication.domain.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.enums.SnsTypeEnum;
import com.jeontongju.authentication.exception.AlreadyWithdrawalMemberException;
import com.jeontongju.authentication.exception.MemberNotFoundException;
import com.jeontongju.authentication.mapper.MemberMapper;
import com.jeontongju.authentication.mapper.SnsAccountMapper;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.repository.SnsAccountRepository;
import com.jeontongju.authentication.security.MemberDetails;
import com.jeontongju.authentication.service.feign.consumer.ConsumerClientService;
import com.jeontongju.authentication.utils.CustomErrMessage;
import java.util.Optional;
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

    OAuth2UserInfo oAuth2UserInfo;
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
    if (isNew(oauthType, oauthId, oAuth2UserInfo)) { // 새로운 회원

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
              .findByUsernameAndMemberRoleEnum(
                  oAuth2UserInfo.getEmail(), MemberRoleEnum.ROLE_CONSUMER)
              .orElseThrow(() -> new MemberNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));

      if (member.getIsDeleted()) {
        throw new AlreadyWithdrawalMemberException(CustomErrMessage.DISABLED_MEMBER);
      }
    }

    return new MemberDetails(member, oAuth2User.getAttributes());
  }

  private boolean isNew(String oauthType, String oauthId, OAuth2UserInfo oAuth2UserInfo) {

    // SnsAccount 테이블에 기존 내역이 있으면 false
    boolean isNew = snsAccountRepository.findBySnsUniqueId(oauthType + "_" + oauthId).isEmpty();

    // 해당 이메일로 일반 회원 가입한 소비자가 있으면 false
    Optional<Member> om =
        memberRepository.findByUsernameAndMemberRoleEnum(
            oAuth2UserInfo.getEmail(), MemberRoleEnum.ROLE_CONSUMER);
    if (om.isPresent()) {
      // 기존 회원 중 최초 소셜 로그인일 때
      if (isNew) {
        SnsTypeEnum snsTypeEnum =
            oauthType.equals("KAKAO") ? SnsTypeEnum.KAKAO : SnsTypeEnum.GOOGLE;

        snsAccountRepository.save(
            snsAccountMapper.toEntity(
                snsTypeEnum.name() + "_" + oAuth2UserInfo.getProviderId(),
                snsTypeEnum.name(),
                om.get()));
      }
      isNew = false;
    }

    return isNew;
  }

  @Transactional
  public Member registerMemberFromSns(
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
}
