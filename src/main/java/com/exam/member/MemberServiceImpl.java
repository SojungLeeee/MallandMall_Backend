package com.exam.member;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class MemberServiceImpl implements MemberService{

    //repository 생성자주입
    MemberRepository memberRepository;
    public MemberServiceImpl(MemberRepository memberRepository) {
        super();
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void save(MemberDTO dto) {
        // MemberDTO --> Member 로 변환하는 작업 필요
        Member member = Member.builder()
                .userid(dto.getUserid())
                .passwd(dto.getPasswd())
                .username(dto.getUsername())
                .post(dto.getPost())
                .addr1(dto.getAddr1())
                .addr2(dto.getAddr2())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();

        memberRepository.save(member);
    }

    @Override
    public MemberDTO findById(String userid) {
        Member member = memberRepository.findById(userid).orElse(null);
        if (member == null) return null;

        return convertToDTO(member);
    }

    //  로그인 기능
    @Override
    public MemberDTO findByUserid(String userid) {
        Member member = memberRepository.findByUseridAndPasswd(userid, ""); // 더미 비밀번호
        if (member == null) return null;

        return convertToDTO(member);
    }

    //  Entity → DTO 변환 메서드
    private MemberDTO convertToDTO(Member member) {
        return MemberDTO.builder()
                .userid(member.getUserid())
                .passwd(member.getPasswd()) // 비밀번호도 함께 반환
                .username(member.getUsername())
                .post(member.getPost())
                .addr1(member.getAddr1())
                .addr2(member.getAddr2())
                .phoneNumber(member.getPhoneNumber())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }
}