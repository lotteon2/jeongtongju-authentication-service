package com.jeontongju.authentication.controller;

import com.jeontongju.authentication.dto.SuccessFormat;
import com.jeontongju.authentication.dto.request.ConsumerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.request.EmailInfoForAuthRequestDto;
import com.jeontongju.authentication.dto.request.SellerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.response.MailAuthCodeResponseDto;
import com.jeontongju.authentication.service.MemberService;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  public ResponseEntity<SuccessFormat<Object>> sendEmailAuthForSignUp(
      @Valid @RequestBody EmailInfoForAuthRequestDto emailInfoDto)
      throws MessagingException, UnsupportedEncodingException {

    MailAuthCodeResponseDto mailAuthCodeResponseDto =
        memberService.sendEmailAuthForSignUp(emailInfoDto);
    return ResponseEntity.ok()
        .body(
            SuccessFormat.builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("회원 가입 시, 중복 회원 및 이메일 인증 번호 생성 성공")
                .data(mailAuthCodeResponseDto)
                .build());
  }

  @PostMapping("/consumers/sign-up")
  public ResponseEntity<SuccessFormat<Object>> signupForConsumer(
      @Valid @RequestBody ConsumerInfoForSignUpRequestDto signupRequestDto) {

    memberService.signupForConsumer(signupRequestDto);

    return ResponseEntity.ok()
        .body(
            SuccessFormat.builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("사용자 일반 회원가입 성공")
                .build());
  }

  @PostMapping("/sellers/sign-up")
  public ResponseEntity<SuccessFormat<Object>> signupForSeller(
      @Valid @RequestBody SellerInfoForSignUpRequestDto signUpRequestDto) {

    memberService.signupForSeller(signUpRequestDto);

    return ResponseEntity.ok()
        .body(
            SuccessFormat.builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("셀러 회원가입 성공")
                .build());
  }

  @GetMapping("/sign-in/oauth2/code/kakao")
  @ResponseBody
  public ResponseEntity<SuccessFormat<Object>> signInForConsumerBySns(
      @RequestParam("code") String code) {

    log.info("code: " + code);
    memberService.signInForConsumerByKakao(code);

    return ResponseEntity.ok()
        .body(
            SuccessFormat.builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("사용자 소셜 로그인 성공")
                .build());
  }
}
