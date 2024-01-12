package com.jeontongju.authentication.controller;

import com.jeontongju.authentication.dto.request.*;
import com.jeontongju.authentication.dto.response.JwtTokenResponse;
import com.jeontongju.authentication.dto.response.MailAuthCodeResponseDto;
import com.jeontongju.authentication.dto.response.MemberInfoForAdminManagingResponseDto;
import com.jeontongju.authentication.dto.response.SiteSituationForAdminManagingResponseDto;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.service.MemberService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import io.github.bitbox.bitbox.dto.ResponseFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberRestController {

  private final MemberService memberService;

  @PostMapping("/email/auth")
  public ResponseEntity<ResponseFormat<MailAuthCodeResponseDto>> sendEmailAuthForFind(
      @Valid @RequestBody EmailInfoForAuthRequestDto authRequestDto)
      throws MessagingException, UnsupportedEncodingException {

    MailAuthCodeResponseDto mailAuthCodeResponseDto =
        memberService.sendEmailAuthForFind(authRequestDto);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<MailAuthCodeResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("비밀번호 찾기시, 이메일 인증 번호 생성 성공")
                .data(mailAuthCodeResponseDto)
                .build());
  }

  @PostMapping("/sign-up/email/auth")
  public ResponseEntity<ResponseFormat<MailAuthCodeResponseDto>> sendEmailAuthForSignUp(
      @Valid @RequestBody EmailInfoForAuthRequestDto emailInfoDto)
      throws MessagingException, UnsupportedEncodingException {

    MailAuthCodeResponseDto mailAuthCodeResponseDto =
        memberService.sendEmailAuthForSignUp(emailInfoDto);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<MailAuthCodeResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("회원 가입 시, 중복 회원 및 이메일 인증 번호 생성 성공")
                .data(mailAuthCodeResponseDto)
                .build());
  }

  @PostMapping("/consumers/sign-up")
  public ResponseEntity<ResponseFormat<Void>> signupForConsumer(
      @Valid @RequestBody ConsumerInfoForSignUpRequestDto signupRequestDto)
      throws JSONException, IOException {

    memberService.signupForConsumer(signupRequestDto);

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("소비자 일반 회원가입 성공")
                .build());
  }

  @PostMapping("/sellers/sign-up")
  public ResponseEntity<ResponseFormat<Void>> signupForSeller(
      @Valid @RequestBody SellerInfoForSignUpRequestDto signUpRequestDto)
      throws JSONException, IOException {

    memberService.signupForSeller(signUpRequestDto);

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("셀러 회원가입 성공")
                .build());
  }

  @PatchMapping("/consumers/adult-certification")
  public ResponseEntity<ResponseFormat<Void>> authentication19AfterSnsSignIn(
      @RequestHeader Long memberId,
      @RequestBody ImpUidForAdultCertificationRequestDto adultCertificationRequestDto)
      throws JSONException, IOException {

    memberService.authentication19AfterSnsSignIn(memberId, adultCertificationRequestDto);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("성인인증 성공")
                .build());
  }

  @PutMapping("/access-token")
  public ResponseEntity<ResponseFormat<String>> issueAccessTokenByRefreshToken(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {

    Cookie[] cookies = request.getCookies();

    String refreshToken = null;
    if (cookies == null) {
      log.info("쿠키가 없습니다.");
    } else {
      boolean isExist = false;
      for (Cookie cookie : cookies) {
        if ("refreshToken".equals(cookie.getName())) {
          String value = cookie.getValue();
          refreshToken = value;
          isExist = true;
          break;
        }
      }
      if (!isExist) {
        log.info("해당 refresh token이 쿠키에 존재하지 않습니다.");
      }
    }
    log.info("쿠키 확인 완료");

    log.info("MemberController's issueAccessTokenByRefreshToken executes..");
    JwtTokenResponse jwtTokenResponse = memberService.renewAccessTokenByRefreshToken(refreshToken);

    log.info("MemberController's issueAccessTokenByRefreshToken Successful executed!");
    Cookie cookie = new Cookie("refreshToken", jwtTokenResponse.getRefreshToken());
    response.addCookie(cookie);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<String>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("Access-Token 재발급 성공")
                .data(jwtTokenResponse.getAccessToken())
                .build());
  }

  @PostMapping("/password/auth")
  public ResponseEntity<ResponseFormat<Void>> confirmOriginPassword(
      @RequestHeader Long memberId,
      @Valid @RequestBody PasswordForCheckRequestDto checkRequestDto) {

    memberService.confirmOriginPassword(memberId, checkRequestDto);

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("비밀번호 수정 - 현재 비밀번호 확인 성공")
                .build());
  }

  @PatchMapping("/password")
  public ResponseEntity<ResponseFormat<Void>> modifyPassword(
      @Valid @RequestBody PasswordForChangeRequestDto changeRequestDto) {

    memberService.modifyPassword(changeRequestDto);

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("비밀번호 찾기 시, 새 비밀번호로 변경 성공")
                .build());
  }

  @PatchMapping("/password/change")
  public ResponseEntity<ResponseFormat<Void>> modifyPasswordForSimpleChange(
      @RequestHeader Long memberId,
      @Valid @RequestBody PasswordForSimpleChangeRequestDto simpleChangeRequestDto) {

    memberService.modifyPasswordForSimpleChange(memberId, simpleChangeRequestDto);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("비밀번호 변경 시, 새 비밀번호로 변경 성공")
                .build());
  }

  @DeleteMapping("/consumers")
  public ResponseEntity<ResponseFormat<Void>> withdraw(@RequestHeader Long memberId) {

    memberService.withdraw(memberId);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("회원 탈퇴 성공")
                .build());
  }

  @GetMapping("/admins/site-situation")
  public ResponseEntity<ResponseFormat<SiteSituationForAdminManagingResponseDto>> getSiteSituation(
      @RequestHeader MemberRoleEnum memberRole) {

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<SiteSituationForAdminManagingResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("관리자, 대시보드 헤더 현황 조회 성공")
                .data(memberService.getSiteSituation(memberRole))
                .build());
  }

  @GetMapping("/admins/members/result")
  public ResponseEntity<ResponseFormat<MemberInfoForAdminManagingResponseDto>> getMembersResult(
      @RequestHeader MemberRoleEnum memberRole) {

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<MemberInfoForAdminManagingResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("관리자, 회원 연령 분포 및 지난 일주일간 가입수 조회 성공")
                .data(memberService.getMembersResult(memberRole))
                .build());
  }
}
