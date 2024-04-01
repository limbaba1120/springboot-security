package org.example.config.oauth.provider;

import java.util.Map;

public class GithubUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes; // oauth2User.getAttributes()

    public GithubUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Object idObject = attributes.get("id");
        if (idObject != null) {
            return String.valueOf(idObject); // Integer를 String으로 안전하게 변환
        } else {
            return null; // 또는 적절한 기본값이나 예외 처리
        }
    }

    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("login");
    }
}
