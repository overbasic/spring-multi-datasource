package basic.multi.member;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/slave")
    @Transactional(readOnly = true)
    public List<Member> getMembersFromSlave() {
        return memberRepository.findAll();
    }

    @GetMapping("/master")
    @Transactional(readOnly = false)
    public List<Member> getMemberFromMaster() {
        return memberRepository.findAll();
    }

    @PostMapping("/slave")
    @Transactional(readOnly = true)
    public void createMemberFromSlave(String name) {
        memberRepository.save(new Member(name));
    }

    @PostMapping("/master")
    @Transactional(readOnly = false)
    public void createMemberFromMaster(String name) {
        memberRepository.save(new Member(name));
    }
}