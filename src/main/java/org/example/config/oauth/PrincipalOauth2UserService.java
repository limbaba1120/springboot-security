package org.example.config.oauth;

import lombok.RequiredArgsConstructor;
import org.example.config.auth.PrincipalDetails;
import org.example.config.oauth.provider.*;
import org.example.domain.User;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

//구글로 부터 받은 userRequest 데이터에 대한 후처리되는 함수
//이미 코드는 받았고, AccessToken 을 돌려줌  (client_id, name ... 정보 얻음)
//우리는 userRequest에 있는 사용자의 getAttribute (사용자 정보, 구글 정보, name, given_name, email, picture) 받는다
// username = google_sub(1213124124...)
// password = "암호화(겟인데어)"
// email = kwlim1120ss@gmail.com
// role = ROLE_USER
// getAttributes:{sub=1141119264051869..., name=임건우, given_name=건우, family_name=임, picture=https://lh3.googleusercontent.com/a/ACg8ocJWfX0imgL1OvQQGDSV2j8LCy4dN1OigGwj9JB58bgL85A=s96-c, email=kwlim1120ss@gmail.com, email_verified=true, locale=ko}
// userRequest:ClientRegistration{registrationId='github', clientId='5c168df99aa8aa867...', clientSecret='49fd201a7c442fb35fe98ccd92205936d18....', clientAuthenticationMethod=org.springframework.security.oauth2.core.ClientAuthenticationMethod@4fcef9d3, authorizationGrantType=org.springframework.security.oauth2.core.AuthorizationGrantType@5da5e9f3, redirectUri='{baseUrl}/{action}/oauth2/code/{registrationId}', scopes=[read:user], providerDetails=org.springframework.security.oauth2.client.registration.ClientRegistration$ProviderDetails@30897e45, clientName='GitHub'}
// getAccessToken:org.springframework.security.oauth2.core.OAuth2AccessToken@6ff2a898
// getClientId:5c168df99aa8aa867...
@RequiredArgsConstructor
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 함수 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("userRequest:" + userRequest.getClientRegistration()); //registrationId로 어떤 OAuth로 로그인 했는지 가능
        System.out.println("getAccessToken:" + userRequest.getAccessToken().getTokenValue());
        System.out.println("getClientId:" + userRequest.getClientRegistration().getClientId());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 구글 로그인 버튼 클릭 -> 구글 로그인 창 -> 로그인 완료 -> code를 리턴 받음(OAuth-Client라이브러리)
        // -> AccessToken 요청, 여기까지가 userRequest 정보
        // userRequest정보로 이제 회원 프로필 받아야함, 그때 사용되는함수 loadUser함수 호출 -> 구글로부터 회원 프로필 받아준다
        System.out.println("getAttributes:" + oAuth2User.getAttributes());

        //회원가입 강제로 진행
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            System.out.println("페이스북 로그인 요청");
            oAuth2UserInfo = new FacebookUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("github")) {
            System.out.println("깃허브 로그인 요청");
            oAuth2UserInfo = new GithubUserInfo(oAuth2User.getAttributes());
        }else {
            System.out.println("구글 페이스북만 지원");
        }

        String provider = oAuth2UserInfo.getProvider(); // google
        String providerId = oAuth2UserInfo.getProviderId(); //google의 ID
        String username = provider + "_" + providerId; //google_sub
        String password = passwordEncoder.encode("겟인데어");
        String email = oAuth2UserInfo.getEmail();
        String role = "ROLE_USER";

        User userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            System.out.println("로그인 최초입니다");
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
        } else {
            System.out.println("로그인을 이미 한적이 있다. 당신은 자동회원가입이 되어있다.");
        }

        return new PrincipalDetails(userEntity, oAuth2User.getAttributes()); //PrincipalDetails가 return 되면서 authentication 객체에 저장
    }
}
