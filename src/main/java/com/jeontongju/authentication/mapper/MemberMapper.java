package com.jeontongju.authentication.mapper;

import com.jeontongju.authentication.domain.Member;
import com.jeontongju.authentication.dto.request.SellerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.response.ImpAuthInfo;
import com.jeontongju.authentication.dto.response.MemberInfoForAdminManagingResponseDto;
import com.jeontongju.authentication.dto.response.SiteSituationForAdminManagingResponseDto;
import com.jeontongju.authentication.dto.temp.ConsumerInfoForAccountConsolidationDto;
import com.jeontongju.authentication.dto.temp.SellerInfoForCreateRequestDto;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import io.github.bitbox.bitbox.dto.AgeDistributionForShowResponseDto;
import io.github.bitbox.bitbox.dto.ConsumerInfoForCreateRequestDto;
import io.github.bitbox.bitbox.dto.ImpAuthInfoForUpdateDto;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

  public Member toEntity(String email, String password, MemberRoleEnum role) {

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    return Member.builder()
        .username(email)
        .password(passwordEncoder.encode(password))
        .memberRoleEnum(role)
        .build();
  }

  public ConsumerInfoForCreateRequestDto toConsumerCreateDto(
      Long consumerId, String email, ImpAuthInfo impAuthInfo) {

    String birthday = impAuthInfo.getBirthday();
    LocalDate birthdate = LocalDate.parse(birthday);
    Period period = Period.between(birthdate, LocalDate.now());
    int age = period.getYears();

    return ConsumerInfoForCreateRequestDto.builder()
        .consumerId(consumerId)
        .email(email)
        .name(impAuthInfo.getName())
        .age(age)
        .phoneNumber(impAuthInfo.getPhone())
        .build();
  }

  public SellerInfoForCreateRequestDto toSellerCreateDto(
      Long memberId, SellerInfoForSignUpRequestDto signUpRequestDto, ImpAuthInfo impAuthInfo) {
    return SellerInfoForCreateRequestDto.builder()
        .memberId(memberId)
        .email(signUpRequestDto.getEmail())
        .storeName(signUpRequestDto.getStoreName())
        .storeDescription(signUpRequestDto.getStoreDescription())
        .storeImageUrl(signUpRequestDto.getStoreImageUrl())
        .storePhoneNumber(signUpRequestDto.getStorePhoneNumber())
        .businessmanName(impAuthInfo.getName())
        .businessmanPhoneNumber(impAuthInfo.getPhone())
        .build();
  }

  public ConsumerInfoForAccountConsolidationDto toAccountConsolidationDto(
      Long consumerId, ImpAuthInfo impAuthInfo) {

    return ConsumerInfoForAccountConsolidationDto.builder()
        .consumerId(consumerId)
        .name(impAuthInfo.getName())
        .phoneNumber(impAuthInfo.getPhone())
        .build();
  }

  public ImpAuthInfoForUpdateDto toImpAuthInfoDto(Long consumerId, ImpAuthInfo impAuthInfo) {

    return ImpAuthInfoForUpdateDto.builder()
        .consumerId(consumerId)
        .name(impAuthInfo.getName())
        .phoneNumber(impAuthInfo.getPhone())
        .build();
  }

  public SiteSituationForAdminManagingResponseDto toSiteSituationDto(
      Long waitingApprovalSellerCnts,
      int newSellerCnts,
      int newConsumerCnts,
      int deletedMemberCnts,
      Long waitingApprovalAuctionCnts) {

    return SiteSituationForAdminManagingResponseDto.builder()
        .waitingApprovalSellerCnts(waitingApprovalSellerCnts)
        .newSellerCnts(newSellerCnts)
        .newConsumerCnts(newConsumerCnts)
        .deletedMemberCnts(deletedMemberCnts)
        .waitingApprovalAuctionCnts(waitingApprovalAuctionCnts)
        .build();
  }

  public MemberInfoForAdminManagingResponseDto toMemberInfoForAdminDto(
      AgeDistributionForShowResponseDto ageDistributionForShowResponseDto,
      Map<LocalDate, Long> consumers,
      Map<LocalDate, Long> sellers) {

    return MemberInfoForAdminManagingResponseDto.builder()
        .teenage(ageDistributionForShowResponseDto.getTeenage())
        .twenty(ageDistributionForShowResponseDto.getTwenty())
        .thirty(ageDistributionForShowResponseDto.getThirty())
        .fortyOver(ageDistributionForShowResponseDto.getFortyOver())
        .consumers(consumers)
        .sellers(sellers)
        .build();
  }
}
