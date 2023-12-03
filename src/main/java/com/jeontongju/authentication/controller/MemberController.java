package com.jeontongju.authentication.controller;

import com.jeontongju.authentication.dto.ConsumerInfoForSignUpForRequestDto;
import com.jeontongju.authentication.dto.SuccessFormat;
import com.jeontongju.authentication.service.MemberService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @PostMapping("/consumers/sign-up")
  public ResponseEntity<SuccessFormat> signupForConsumer(
      @Valid @RequestBody ConsumerInfoForSignUpForRequestDto signupRequestDto) {

    memberService.signupForConsumer(signupRequestDto);

    return ResponseEntity.ok()
        .body(
            SuccessFormat.builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.name())
                .detail("사용자 일반 회원가입 성공")
                .build());
  }
}
