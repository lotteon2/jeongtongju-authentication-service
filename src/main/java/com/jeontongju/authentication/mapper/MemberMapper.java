package com.jeontongju.authentication.mapper;

import com.jeontongju.authentication.dto.request.SellerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.response.ImpAuthInfo;
import com.jeontongju.authentication.dto.temp.ConsumerInfoForAccountConsolidationDto;
import com.jeontongju.authentication.dto.temp.ConsumerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.temp.SellerInfoForCreateRequestDto;
import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
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
    return ConsumerInfoForCreateRequestDto.builder()
        .consumerId(consumerId)
        .email(email)
        .name(impAuthInfo.getName())
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
}
