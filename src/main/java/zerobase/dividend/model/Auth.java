package zerobase.dividend.model;

import lombok.Data;
import zerobase.dividend.persist.entity.MemberEntity;

import java.util.List;

public class Auth {

    @Data
    public static class SignIn {
        private String username;
        private String password;
    }

    @Data
    public static class SignUp {
        private String username;
        private String password;
        private List<String> roles;
        /*
        사용자가 정하는게 아니라 내부 로직으로 처리
        일반 회원가입 경로에서 가입하면 일반 권한만
        관리자 회원가입 경로에서 가입하면 관리자 권한도 부여
         */

        public MemberEntity toEntity() {
            return MemberEntity.builder()
                                .username(this.username)
                                .password(this.password)
                                .roles(this.roles)
                                .build();
        }
    }

}
