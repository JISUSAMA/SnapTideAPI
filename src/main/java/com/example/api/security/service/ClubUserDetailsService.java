package com.example.api.security.service;

import com.example.api.entity.Members;
import com.example.api.repository.MembersRepository;
import com.example.api.security.dto.ClubMemberAuthDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor //DB접근방식으로 UserDetailsService(인증 관리 객체) 사용
public class ClubUserDetailsService implements UserDetailsService {
  private final MembersRepository membersRepository;

  @Override
  // DB에 있는 것 확인 된후,User를 상속받은 ClubMemberAuthDTO에 로그인정보를 담음=>세션
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("ClubMemberUser.........", username);
    Optional<Members> result = membersRepository.findByEmail(username);
    if (!result.isPresent()) throw new UsernameNotFoundException("Check Email or Social");
    Members members = result.get(); // DB로부터 검색한 엔티티
    // 엔티티를 세션으로 담기위해 만든 ClubMemberAuthDTO
    ClubMemberAuthDTO clubMemberAuthDTO = new ClubMemberAuthDTO(
        members.getEmail(), members.getPw(), members.getMid(),
        members.isFromSocial(),
        members.getRoleSet().stream().map(
            clubMemberRole -> new SimpleGrantedAuthority(
                "ROLE_" + clubMemberRole.name())).collect(Collectors.toList())
    );
    clubMemberAuthDTO.setName(members.getName());
    clubMemberAuthDTO.setFromSocial(members.isFromSocial());
    log.info("clubMemberAuthDTO >> ", clubMemberAuthDTO.getCno());
    return clubMemberAuthDTO;
  }
}
