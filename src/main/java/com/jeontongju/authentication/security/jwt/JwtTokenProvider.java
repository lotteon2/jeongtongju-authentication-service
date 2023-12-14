package com.jeontongju.authentication.security.jwt;

import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.security.MemberDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements InitializingBean {
  private final String secret;
  private final Long tokenValidityInMilliseconds;
  private final Long refreshTokenValidityInMilliseconds;
  private Key key;

  public JwtTokenProvider(Environment env) {
    this.secret = env.getProperty("jwt.secret");
    this.tokenValidityInMilliseconds =
        Long.parseLong(env.getProperty("jwt.token-validity-in-seconds")) * 1000;
    this.refreshTokenValidityInMilliseconds =
        Long.parseLong(env.getProperty("jwt.refresh-token-validity-in-seconds")) * 1000;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public String createToken(Authentication authentication) {

    MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
    String authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    long now = (new Date()).getTime();
    Date validity = new Date(now + this.tokenValidityInMilliseconds);

    return Jwts.builder()
        .setSubject(authentication.getName())
        .claim("memberId", memberDetails.getMember().getMemberId().toString())
        .claim("memberRole", authorities)
        .claim("username", memberDetails.getUsername())
        .signWith(key, SignatureAlgorithm.HS512)
        .setExpiration(validity)
        .compact();
  }

  public String recreateToken(Member member) {
    long now = (new Date()).getTime();
    Date validity = new Date(now + this.tokenValidityInMilliseconds);
    return Jwts.builder()
        .setSubject(member.getUsername())
        .claim("memberId", member.getMemberId())
        .claim("memberRole", member.getMemberRoleEnum().name())
        .claim("username", member.getUsername())
        .signWith(key, SignatureAlgorithm.HS512)
        .setExpiration(validity)
        .compact();
  }

  public String createRefreshToken(Long memberId) {

    long now = (new Date()).getTime();
    Date validity = new Date(now + this.refreshTokenValidityInMilliseconds);

    return Jwts.builder()
        .setSubject(memberId.toString())
        .claim("memberId", memberId.toString())
        .signWith(key, SignatureAlgorithm.HS512)
        .setExpiration(validity)
        .compact();
  }
}
