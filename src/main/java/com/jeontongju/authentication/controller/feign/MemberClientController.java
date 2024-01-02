package com.jeontongju.authentication.controller.feign;

import com.jeontongju.authentication.dto.temp.FeignFormat;
import com.jeontongju.authentication.dto.temp.MemberEmailForKeyDto;
import com.jeontongju.authentication.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberClientController {

  private final MemberService memberService;

  @PostMapping("/members/email")
  public FeignFormat<MemberEmailForKeyDto> getMemberEmailForKey(@RequestBody Long memberId) {
    MemberEmailForKeyDto memberEmailForKeyDto = memberService.getMemberEmailForKey(memberId);

    return FeignFormat.<MemberEmailForKeyDto>builder()
        .code(HttpStatus.OK.value())
        .data(memberEmailForKeyDto)
        .build();
  }
}
