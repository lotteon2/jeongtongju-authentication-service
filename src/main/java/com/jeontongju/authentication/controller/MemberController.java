package com.jeontongju.authentication.controller;

import com.jeontongju.authentication.dto.SuccessFormat;
import com.jeontongju.authentication.dto.request.ConsumerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.request.EmailInfoForAuthRequestDto;
import com.jeontongju.authentication.dto.request.SellerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.response.JwtTokenResponse;
import com.jeontongju.authentication.dto.response.MailAuthCodeResponseDto;
import com.jeontongju.authentication.service.MemberService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
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
public class MemberController {

  private final MemberService memberService;

  @PostMapping("/sign-up/email/auth")
  public ResponseEntity<SuccessFormat<MailAuthCodeResponseDto>> sendEmailAuthForSignUp(
      @Valid @RequestBody EmailInfoForAuthRequestDto emailInfoDto)
      throws MessagingException, UnsupportedEncodingException {

    MailAuthCodeResponseDto mailAuthCodeResponseDto =
        memberService.sendEmailAuthForSignUp(emailInfoDto);
    return ResponseEntity.ok()
        .body(
            SuccessFormat.<MailAuthCodeResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("회원 가입 시, 중복 회원 및 이메일 인증 번호 생성 성공")
                .data(mailAuthCodeResponseDto)
                .build());
  }

  @PostMapping("/consumers/sign-up")
  public ResponseEntity<SuccessFormat<Void>> signupForConsumer(
      @Valid @RequestBody ConsumerInfoForSignUpRequestDto signupRequestDto)
      throws JSONException, IOException {

    memberService.signupForConsumer(signupRequestDto);

    return ResponseEntity.ok()
        .body(
            SuccessFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("소비자 일반 회원가입 성공")
                .build());
  }

  @PostMapping("/sellers/sign-up")
  public ResponseEntity<SuccessFormat<Void>> signupForSeller(
      @Valid @RequestBody SellerInfoForSignUpRequestDto signUpRequestDto)
      throws JSONException, IOException {

    memberService.signupForSeller(signUpRequestDto);

    return ResponseEntity.ok()
        .body(
            SuccessFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("셀러 회원가입 성공")
                .build());
  }

  @GetMapping("/sign-in/oauth2/code/kakao")
  public ResponseEntity<SuccessFormat<Void>> signInForConsumerBySns(
      @RequestParam("code") String code) {

    memberService.signInForConsumerByKakao(code);
    return ResponseEntity.ok()
        .body(
            SuccessFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("소비자 소셜 로그인 성공 - KAKAO")
                .build());
  }

  @GetMapping("/sign-in/oauth2/code/google")
  public ResponseEntity<SuccessFormat<Void>> signInForConsumerByGoogle(
      @RequestParam("code") String code) {

    memberService.signInForConsumerByGoogle(code);
    return ResponseEntity.ok()
        .body(
            SuccessFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("소비자 소셜 로그인 성공 - GOOGLE")
                .build());
  }

  @PutMapping("/access-token")
  public ResponseEntity<SuccessFormat<String>> issueAccessTokenByRefreshToken(
      @CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {

    JwtTokenResponse jwtTokenResponse = memberService.renewAccessTokenByRefreshToken(refreshToken);

    Cookie cookie = new Cookie("refreshToken", jwtTokenResponse.getRefreshToken());
    response.addCookie(cookie);
    return ResponseEntity.ok()
        .body(
            SuccessFormat.<String>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("Access-Token 재발급 성공")
                .data(jwtTokenResponse.getAccessToken())
                .build());
  }
}
