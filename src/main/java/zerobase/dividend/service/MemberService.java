package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zerobase.dividend.exception.impl.AlreadyExistUserException;
import zerobase.dividend.model.Auth;
import zerobase.dividend.persist.MemberRepository;
import zerobase.dividend.persist.entity.MemberEntity;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {   // 스프링 시큐리티

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;   // 어떤 인코더를 쓸건지 AppConfig 에 정의해줘야

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
        // orElseThrow 해서 Optional 벗겨진 MemberEntity 형인데
        // MemberEntity 가 implements UserDetails 한거라 그냥 return 하면 된다
    }

    // 회원가입
    public MemberEntity register(Auth.SignUp member) {
        // 동일한 아이디가 존재하는지 확인
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        // 비밀번호는 암호화 해서 저장해야
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var result = this.memberRepository.save(member.toEntity());
        return result;
    }

    // 로그인 정보 검증
    public MemberEntity authenticate(Auth.SignIn member) {
        // ID 확인
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다"));

        // 패스워드 확인 - 유저가 입력한 패스워드를 인코딩해서 비교
        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        return user;
    }

}
