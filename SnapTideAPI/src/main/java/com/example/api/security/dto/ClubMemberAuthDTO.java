package com.example.api.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Log4j2
@Getter
@Setter
@ToString
public class ClubMemberAuthDTO extends User implements OAuth2User {
  private Long cno;
  private String email;
  private String name;
  private boolean fromSocial;
  private Map<String, Object> attr; //소셜로부터 받은 정보를 저장하는 속성

  public ClubMemberAuthDTO(String username, String password,
                           Long cno, boolean fromSocial,
                           Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
    this.cno = cno;
    this.email = username;  //★UserDetails에서 username은 email로 기준하기 때문.
    this.fromSocial = fromSocial;
  }
  // 소셜로부터 받을 때 별도의 생성자
  public ClubMemberAuthDTO(String username, String password,
                           Long cno, boolean fromSocial,
                           Collection<? extends GrantedAuthority> authorities,
                           Map<String, Object> attr) {
    this(username, password, cno, fromSocial, authorities);
    this.attr = attr;
  }
  @Override
  public Map<String, Object> getAttributes() {
    return this.attr;
  }
}
