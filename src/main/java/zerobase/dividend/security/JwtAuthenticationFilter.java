package zerobase.dividend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 토큰은 HTTP 요청 헤더에 포함됨, 헤더는 여러 정보를 가지고 있음
    // 어떤 헤더(key) 에 토큰정보(val) 가 있나?
    public static final String TOKEN_HEADER = "Authorization";

    // 인증 타입을 나타내기 위해 사용, JWT 토큰을 사용하는 경우 Bearer 사용
    public static final String TOKEN_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = this.resolveTokenFromRequest(request);

        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
            // 유효성 검증된 토큰일 경우 인증객체로 변환
            Authentication auth = this.tokenProvider.getAuthentication(token);

            // Authentication 객체를 현재 실행 중인 스레드의 보안 컨텍스트에 설정
            // 이로써 Spring Security 는 인증된 사용자의 정보와 권한을 유지하고, 보안 검사, 인가 처리 등을 수행할 수 있다
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 어떤 사용자가 어떤 경로에 접근했는지 로그 남기기
            log.info(String.format("[%s] -> %s", this.tokenProvider.getUsername(token), request.getRequestURI()));
        }

        // 다음 단계로 요청과 응답 객체를 전달
        filterChain.doFilter(request, response);
        // 만약 토큰이 유효하지 않다면, 보안 컨텍스트에 설정 안하고 그냥 실행
    }

    // request 헤더에서 토큰을 꺼내는 메소드
    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());   // prefix 제외한 토큰 값
        }

        return null;   // 토큰이 없거나 정상적인 형태가 아닐 경우
    }

}

/*
사용자가 API 를 호출한다고 해서, 바로 컨트롤러로 요청이 들어가는건 아니다
API 호출 -> Filter -> Servlet -> Interceptor -> AOP Layer -> Controller
응답이 나갈때도 마찬가지, 위의 단계를 거꾸로 거치면서 나간다

필터에서 요청이 들어올때마다, 요청에 포함된 토큰이 유효한지 확인할거
 */
