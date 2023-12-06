package com.jeontongju.authentication.dto;

import javax.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MailInfoDto {
  MimeMessage mimeMessage;
  String validCode;
}
