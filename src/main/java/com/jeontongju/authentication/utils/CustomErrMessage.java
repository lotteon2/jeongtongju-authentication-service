package com.jeontongju.authentication.utils;

public interface CustomErrMessage {

  String NOT_FOUND_MEMBER = "찾을 수 없는 회원";
  String DUPLICATED_AUTHENTICATION = "이미 인증된 사용자";
  String EMAIL_ALREADY_IN_USE = "이메일 중복 오류, 계정 통합 여부 체크";
  String EXPIRED_REFRESH_TOKEN = "만료된 토큰";
  String MALFORMED_REFRESH_TOKEN = "잘못된 토큰";
  String NOT_CORRESPOND_ORIGIN_PASSWORD = "기존 비밀번호 불일치";
}
