package com.jeontongju.authentication.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Member Insert")
    void test1() {
        Member member = Member.builder()
            .username("zjadlspun1114@naver.com")
            .password(passwordEncoder.encode("12345"))
            .memberRoleEnum(MemberRoleEnum.ROLE_CONSUMER)
            .build();
        Member savedMember = memberRepository.save(member);
        assertThat(savedMember.getMemberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("username + memberRole은 UNIQUE하다")
    void test2() {
        Member member = Member.builder()
            .username("test1@naver.com")
            .password(passwordEncoder.encode("56789"))
            .memberRoleEnum(MemberRoleEnum.ROLE_CONSUMER)
            .build();
        memberRepository.save(member);

        assertThatThrownBy(() -> {
            Member member2 = Member.builder()
                .username("test1@naver.com")
                .password(passwordEncoder.encode("12345"))
                .memberRoleEnum(MemberRoleEnum.ROLE_CONSUMER)
                .build();
            memberRepository.save(member2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("username이 같아도 memberRole이 다르면 다른 회원이다")
    void test3() {
        Member member3 = Member.builder()
            .username("test2@naver.com")
            .password(passwordEncoder.encode("56789"))
            .memberRoleEnum(MemberRoleEnum.ROLE_CONSUMER)
            .build();
        memberRepository.save(member3);

        Member member4 = Member.builder()
            .username("test2@naver.com")
            .password(passwordEncoder.encode("56789"))
            .memberRoleEnum(MemberRoleEnum.ROLE_SELLER)
            .build();
        memberRepository.save(member4);

        assertThat(memberRepository.count()).isEqualTo(2);
    }
}
