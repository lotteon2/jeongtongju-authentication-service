package com.jeontongju.authentication.controller;

import com.jeontongju.authentication.dto.request.*;
import com.jeontongju.authentication.dto.response.JwtTokenResponse;
import com.jeontongju.authentication.dto.response.MailAuthCodeResponseDto;
import com.jeontongju.authentication.dto.temp.ResponseFormat;
import com.jeontongju.authentication.service.MemberService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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

  @GetMapping("/sign-in/oauth2/code/kakao")
  public ResponseEntity<ResponseFormat<Void>> signInForConsumerBySns(
      @RequestParam("code") String code) {

    memberService.signInForConsumerByKakao(code);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("소비자 소셜 로그인 성공 - KAKAO")
                .build());
  }

  @GetMapping("/sign-in/oauth2/code/google")
  public ResponseEntity<ResponseFormat<Void>> signInForConsumerByGoogle(
      @RequestParam("code") String code) {

    memberService.signInForConsumerByGoogle(code);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("소비자 소셜 로그인 성공 - GOOGLE")
                .build());
  }

  @PutMapping("/access-token")
  public ResponseEntity<ResponseFormat<String>> issueAccessTokenByRefreshToken(
      HttpServletRequest request, HttpServletResponse response) {

    Cookie[] cookies = request.getCookies();

    String refreshToken = null;
    if (cookies == null) {
      log.info("쿠키가 없습니다.");
    } else {
      for (Cookie cookie : cookies) {
        if ("refreshToken".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
        } else {
          log.info("refreshToken 이라는 이름의 쿠키가 없습니다");
        }
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
}