package com.jeontongju.authentication.utils;

import com.jeontongju.authentication.dto.MailInfoDto;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

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

      final int CODE_LEN = 8;
      SecureRandom random = new SecureRandom();
      StringBuilder builder = new StringBuilder(CODE_LEN);

      final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      for (int i = 0; i < CODE_LEN; i++) {
        int randomIdx = random.nextInt(CHARACTERS.length());
        char randomChar = CHARACTERS.charAt(randomIdx);
        builder.append(randomChar);
      }

      return builder.toString();
  }

  // 이메일 폼 생성
  private static MailInfoDto createEmailForm(String to, String title, String text)
      throws MessagingException, UnsupportedEncodingException {

    String authNum = createValidCode();
    MimeMessage message = mailSender.createMimeMessage();
    message.addRecipients(RecipientType.TO, to);
    message.setSubject(title);
    message.setFrom(from);
    message.setText(text + "<br>" + authNum, "utf-8", "html");
    return MailInfoDto.builder().mimeMessage(message).validCode(authNum).build();
  }

  // 이메일 보내기
  public static MailInfoDto sendAuthEmail(String email, String title, String text)
      throws MessagingException, UnsupportedEncodingException {

    MailInfoDto mailInfo = createEmailForm(email, title, text);
    MimeMessage emailForm = mailInfo.getMimeMessage();
    mailSender.send(emailForm);
    return mailInfo;
  }
}
