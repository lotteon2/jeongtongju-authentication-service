package com.jeontongju.authentication.kafka;

import com.jeontongju.authentication.service.MemberService;
import io.github.bitbox.bitbox.util.KafkaTopicNameInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberKafkaListener {

  private final MemberService memberService;

  @KafkaListener(topics = KafkaTopicNameInfo.DELETE_SELLER_AUTHENTICATION)
  public void deleteSeller(Long sellerId) {

    try {
      memberService.deleteSeller(sellerId);
    } catch (Exception e) {
      log.error("During Deleting Seller: Error Kafka Logic={}", e.getMessage());
    }
  }
}
