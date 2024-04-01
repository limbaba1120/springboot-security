package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.auth.PrincipalDetails;
import org.example.domain.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequiredArgsConstructor
@Controller
public class IndexController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @ResponseBody
    @GetMapping("/test/login")
    public String testLogin(@AuthenticationPrincipal PrincipalDetails userDetails) {
        log.info("/test/login==============");

        System.out.println("userDetails:" + userDetails);
        System.out.println("userDetails:" + userDetails.getAuthorities());
        System.out.println("userDetails:" + userDetails.getUsername());
        return "세션 정보 확인하기";
    }
    // 일반 로그인을 할 때 UserDetails 로 시큐리티 세션 Authentication 안에 들어가고
    // 구글, 깃허브 로그인 하면 OAuth2User 타입이 Authentication 객체 안에 들어간다
    // 세션이 생기면서 로그인이됨
    // 필요할 때 꺼내써야하는데 불편함...
    // 일반적인 로그인을 하면 @AuthenticationPrincipal PrincipalDetails userDetails 이렇게 받아야함
    // 구글로 로그인하면 @AuthenticationPrincipal OAuth2User oauth 이렇게 받아야함
    // 일반적인 로그인과 구글 로그인 처리하는게 굉장히 복잡해짐
    // X 라는 클래스 만들어서 UserDetails 상속받고, Oauth2User 상속받고 부모로 받으면 해결가능 (이런 방식 ㄴ)
    // Oauth2User을 PrincipalDetails를 부모 타입으로 객체로 잡고 부르면 언제든지 가능
    @ResponseBody
    @GetMapping("/test/oauth/login")
    public String testOAuthLogin(@AuthenticationPrincipal OAuth2User oauth) {
        log.info("/test/oauth/login==============");

        System.out.println("oauth2User:" +oauth.getAttributes());

        return "OAuth 세션 정보 확인하기";
    }

    //localhost:8080
    @GetMapping({"", "/"})
    public String index() {
        return "index";
    }


    //일반 로그인이든 Oauth로그인이든 PrincipalDetails로 다 받을 수 있음
    @ResponseBody
    @GetMapping("/user")
    public String user(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        System.out.println("principalDetails:"+principalDetails.getUser());
        return "user";
    }

    @ResponseBody
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @ResponseBody
    @GetMapping("/manager")
    public String manager() {
        return "manager";
    }

    // 스프링 시큐리티 해당 주소를 낚아챔
    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "joinForm";
    }

    @PostMapping("/join")
    public String join(User user) {
        user.setRole("ROLE_USER");
        String rawPassword = user.getPassword();
        String encPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encPassword);
        userRepository.save(user);
        return "redirect:/loginForm";
    }

    @Secured("ROLE_ADMIN")
    @ResponseBody
    @GetMapping("/info")
    public String info() {
        return "개인정보";
    }
}
