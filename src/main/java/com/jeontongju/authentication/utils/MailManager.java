package com.jeontongju.authentication.utils;

import com.jeontongju.authentication.dto.MailInfoDto;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import java.io.UnsupportedEncodingException;
import java.util.Random;

@Component
public class MailManager {

  private static String from;
  private static JavaMailSender mailSender;

  private static final Integer VALID_CODE_LENGTH = 8;

  public MailManager(Environment env, JavaMailSender mailSender) {
    from = env.getProperty("store.email.from");
    MailManager.mailSender = mailSender;
  }

  // 이메일 유효코드 생성
  private static String createValidCode() {
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
  private static MailInfoDto createEmailForm(String to)
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
  public static MailInfoDto sendAuthEmail(String email)
      throws MessagingException, UnsupportedEncodingException {
    MailInfoDto mailInfo = createEmailForm(email);
    MimeMessage emailForm = mailInfo.getMimeMessage();
    mailSender.send(emailForm);
    return mailInfo;
  }
}
