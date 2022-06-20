package basic.multi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import basic.multi.member.Member;
import basic.multi.member.MemberRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionTest {

    private final Logger logger = LoggerFactory.getLogger(TransactionTest.class);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    private static final String Test_User_Name = "testUser";

    @BeforeEach
    @Transactional
    void before() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("JPA_Transaction_성공_테스트")
    void successJpaCommitTest() throws Exception {
        // given

        // when
        mockMvc.perform(post("/members/master")
                .param("name", Test_User_Name)
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn()
        ;

        Optional<Member> member = memberRepository.findMemberByName(Test_User_Name);

        // then
        assertThat(member.get()).isNotNull();
    }


    @Test
    @DisplayName("JPA_Transaction_롤백_테스트")
    void rollbackJpaTest() {
        // given

        // when
        try {
            mockMvc.perform(post("/members/slave")
                    .param("name", Test_User_Name)
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
            ;

        } catch (Exception e) {
            logger.error("generate runtime exception to verify transaction rollback");
        }

        Optional<Member> member = memberRepository.findMemberByName(Test_User_Name);

        // then
        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(member::get);

    }

}