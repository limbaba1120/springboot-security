package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.config.oauth.PrincipalOauth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // IoC 빈(bean)을 등록
@EnableWebSecurity // 필터 체인 관리 시작 어노테이션
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true) // 특정 주소 접근 시 권한 및 인증을 위한 어노테이션 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final PrincipalOauth2UserService principalOauth2UserService;


    // 1.코드 받기(인증,사용자가 구글에 접속된 정상적인 사용자), 2.엑세스토큰(사용자 접근에 권한이 생김),
    // 3.사용자 프로필 정보를 가져오고, 4. 그 정보를 토대로 회원가입을 자동으로 진행시키기도 함
    // 4-2. (이메일, 전화번호, 이름, 아이디) 만약 쇼핑몰을 한다면 -> 추가적으로 집주소, 등급 이런것도 필요
    // 추가적인 구성이 필요하게 되면, 추가적인 회원창이 나와서 회원가입 해야함. 만약 추가적인 정보가 필요없으면
    // 구글이 제공하는 정보만 사용하면됨됨
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .requestMatchers("/user/**").authenticated() // 인증만 되면 들어갈 수 있는 주소
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/loginForm")
                .loginProcessingUrl("/login") // /login 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행
                .defaultSuccessUrl("/")
                .and()
                .oauth2Login()
                .loginPage("/loginForm")
                .userInfoEndpoint()
                .userService(principalOauth2UserService); // 구글 로그인이 완료된 뒤의 후처리 필요. Tip 구글 로그인이 완료가되면 코드를 안받음. (엑세스 토큰 + 사용자 프로필 정보를 받음)


        return http.build();
    }
}
