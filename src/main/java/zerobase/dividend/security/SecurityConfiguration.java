package zerobase.dividend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)   // hasRole 을 아래처럼 안하고 컨트롤러에서 어노테이션으로 처리할거라 필요
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter authenticationFilter;

    /*
    1. RestAPI 형태로 서비스를 구현했기 때문에, 사용하지 않는 기능들은 disable 처리
    2. JWT 토큰 로그인 - 서버는 토큰을 통해 사용자를 인증, 서버에 상태정보 저장 x = STATELESS
       세션 기반 로그인 - 세션 상태를 유지하기 위해, 서버에 상태정보를 저장 = STATELESS 하지 않음
    3. 어떤 경로를 허용할거고, 어떤 권한을 필요로 할건지 정의
    4. 사용자 정의 필터 추가
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                        .antMatchers("/**/signup", "/**/signin").permitAll()   // 해당 경로는 무조건 허용
//                      .antMatchers("/admin/**").hasRole("ADMIN")   // 특정 경로에 제한을 걸고 싶을때
                .and()
                    .addFilterBefore(this.authenticationFilter, UsernamePasswordAuthenticationFilter.class);
                    // 우리가 만든 필터를 Spring Security 기본 인증 필터 이전에 추가해주는것
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        // 해당 경로로 API 호출시, 인증정보가 없어도 자유롭게 접근가능 -> 개발 편의성을 위한것
        web.ignoring()
                .antMatchers("/h2-console/**");   // ** 는 뒤에 어떤 경로든 포함한다는 의미

        // ignoring 에 "/**/signup", "/**/signin" 을 넣어줘도 되긴하는데,
        // 편의상 개발관련 경로랑 구분해준것
    }

    // spring boot 2.x 부터 선언해줘야 한다
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    /*
    Spring Security 에서 인증을 관리하는 AuthenticationManager 객체를 생성하는 메서드
    super.authenticationManagerBean() 은
    부모 클래스인 WebSecurityConfigurerAdapter 클래스의
    authenticationManagerBean() 메서드를 호출하여
    부모 클래스에서 정의된 AuthenticationManager 객체를 반환한다
     */

}
