package com.jeontongju.authentication.utils;

public interface CustomErrMessage {

  String NOT_FOUND_MEMBER = "해당 회원을 찾을 수 없습니다.";
  String DUPLICATED_AUTHENTICATION = "이미 인증된 사용자 입니다.";
  String EMAIL_ALREADY_IN_USE = "이메일 중복 오류, 계정 통합 여부 체크";
  String EXPIRED_REFRESH_TOKEN = "토큰이 만료되었습니다. 다시 로그인 해 주세요";
  String MALFORMED_REFRESH_TOKEN = "잘못된 토큰입니다. 다시 로그인 해 주세요";
}
