package com.example.api.security.service;

import com.example.api.entity.Members;
import com.example.api.entity.MembersRole;
import com.example.api.repository.MembersRepository;
import com.example.api.security.dto.ClubMemberAuthDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ClubOAuth2userDetailsService extends DefaultOAuth2UserService {
  private final MembersRepository membersRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    log.info("============= userRequest: " + userRequest);
    // OAuth2UserService는 social로부터 정보를 받기 위한 객체 생성
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
        new DefaultOAuth2UserService();
    //delegate.loadUser()는 userRequest(소셜에서 온 유저정보)를 세션 객체(OAuth2User)로 변환
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    SocialType socialType = getSocialType(registrationId.trim().toString());
    String userNameAttributeName = userRequest.getClientRegistration()
        .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
    log.info("userNameAttributeName >> " + userNameAttributeName);
    Map<String, Object> attributes = oAuth2User.getAttributes();
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue());
    }
    String email = null;
    if (socialType.name().equals("GOOGLE"))
      email = oAuth2User.getAttribute("email");
    log.info("Email: " + email);
    Members members = saveSocialMember(email);
    ClubMemberAuthDTO clubMemberAuthDTO = new ClubMemberAuthDTO(
        members.getEmail(),
        members.getPw(),
        members.getMid(),
        true,
        members.getRoleSet().stream().map(
                role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .collect(Collectors.toList())
        , attributes
    );
    clubMemberAuthDTO.setFromSocial(members.isFromSocial());
    clubMemberAuthDTO.setName(members.getName());
    log.info("clubMemberAuthDTO: " + clubMemberAuthDTO);
    return clubMemberAuthDTO;
  }

  private Members saveSocialMember(String email) {
    Optional<Members> result = membersRepository.findByEmail(email);
    if (result.isPresent()) return result.get();

    // 소셜에서 넘어온 정보가 DB에 없을 때 저장하는 부분
    Members members = Members.builder()
        .email(email)
        .pw(passwordEncoder.encode("1"))
        .fromSocial(true)
        .build();
    members.addMemberRole(MembersRole.USER);
    membersRepository.save(members);
    return members;
  }

  private SocialType getSocialType(String registrationId) {
    if (SocialType.NAVER.name().equals(registrationId)) {
      return SocialType.NAVER;
    }
    if (SocialType.KAKAO.name().equals(registrationId)) {
      return SocialType.KAKAO;
    }
    return SocialType.GOOGLE;
  }

  enum SocialType {
    KAKAO, NAVER, GOOGLE
  }
}

