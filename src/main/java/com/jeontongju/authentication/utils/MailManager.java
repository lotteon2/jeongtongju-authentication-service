package com.jeontongju.authentication.utils;

import com.jeontongju.authentication.dto.MailInfoDto;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import org.springframework.mail.javamail.JavaMailSender;

public interface MailManager {

  Integer VALID_CODE_LENGTH = 8;

  // 이메일 유효코드 생성
  static String createValidCode() {
    Random random = new Random();
    StringBuilder key = new StringBuilder();

    String authNum;
    for (int i = 0; i < VALID_CODE_LENGTH; i++) {
      int idx = random.nextInt(3);

      switch (idx) {
        case 0:
          key.append((char) (random.nextInt(26) + 97));
          break;
        case 1:
          key.append((char) (random.nextInt(26) + 65));
          break;
        case 2:
          key.append(random.nextInt(9));
          break;
      }
    }
    authNum = key.toString();
    return authNum;
  }

  // 이메일 폼 생성
  static MailInfoDto createEmailForm(JavaMailSender mailSender, String from, String to)
      throws MessagingException, UnsupportedEncodingException {

    String title = "전통주점 회원가입 유효코드 발송";
    String authNum = createValidCode();
    MimeMessage message = mailSender.createMimeMessage();
    message.addRecipients(RecipientType.TO, to);
    message.setSubject(title);
    message.setFrom(from);
    message.setText("회원가입 인증 유효코드입니다.<br>" + authNum, "utf-8", "html");
    return MailInfoDto.builder().mimeMessage(message).validCode(authNum).build();
  }

  // 이메일 보내기
  static MailInfoDto sendAuthEmail(JavaMailSender mailSender, String from, String email)
      throws MessagingException, UnsupportedEncodingException {
    MailInfoDto mailInfo = createEmailForm(mailSender, from, email);
    MimeMessage emailForm = mailInfo.getMimeMessage();
    mailSender.send(emailForm);
    return mailInfo;
  }
}
