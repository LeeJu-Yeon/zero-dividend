package zerobase.dividend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zerobase.dividend.service.MemberService;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;   // 1시간 = 1000ms(1초) * 60초 * 60분
    private static final String KEY_ROLES = "roles";

    private final MemberService memberService;

    @Value("{spring.jwt.secret}")
    private String secretKey;

    /**
     * 토큰 생성(발급)
     * @param username
     * @param roles
     * @return
     */
    public String generateToken(String username, List<String> roles) {
        /*
        토큰에 포함되어야 하는 정보
        Claim (사용자 이름, 사용자 권한)
        토큰 생성일시, 토큰 만료일시
        암호화 알고리즘 종류, Secret Key
         */
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(KEY_ROLES, roles);   // claim 에는 key - val 타입으로 저장해야

        var now = new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        // 토큰 생성
        return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(expiredDate)
                    .signWith(SignatureAlgorithm.HS512, this.secretKey)
                    .compact();   // 이걸로 빌드 끝
    }

    // 토큰 파싱해서 Claim 정보 가져오기
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
            // 만료시간이 경과한 토큰을 파싱하려고 하면 예외가 발생한다
        }
    }

    // 토큰 파싱 -> Claim -> username 가져오기
    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();
    }

    // 토큰 기간 유효한지 확인
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;   // 토큰이 빈값일때

        var claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
        // 비교대상시간.before(현재시간) => 비교 대상 시간이 현재시간 이전이면 true
    }

    // JWT 토큰 -> 사용자 정보, 사용자 권한 포함한 Authentication 인증객체로 변환
    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));

        // 스프링 시큐리티 기능 Authentication 인증객체 반환
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

}

/*
JWT Jason Web Token 구조 -> . 으로 구분
Header - 토큰의 타입(Ex. JWT), 어떤 암호화 알고리즘이 적용됐는지(Ex. HS512)
Payload - 인증 및 권한 관련 클레임(claim) 정보가 포함됨(Ex. 사용자 이름, 사용자 권한, 토큰 생성일시, 토큰 만료일시)
Signature - 토큰이 유효한지 판별(Secret Key 필요)
 */
