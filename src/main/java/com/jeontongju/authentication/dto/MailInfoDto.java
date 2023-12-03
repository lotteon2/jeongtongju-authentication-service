package com.jeontongju.authentication.dto;

import javax.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class MailInfoDto {
  MimeMessage mimeMessage;
  String validCode;
}
