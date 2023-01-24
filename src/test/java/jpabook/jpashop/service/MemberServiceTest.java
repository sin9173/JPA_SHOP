package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    //@Rollback(value = false) //영속성 컨텍스트에 있는 쿼리들을 commit
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
        //em.flush();
        Assertions.assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test
    //@Test(expected = IllegalStateException.class) //JUnit4 에서 Exception 처리
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);


        //JUnit5에서 Exception 처리
        Assertions.assertEquals(
                Assertions.assertThrows(IllegalStateException.class, () -> {
            memberService.join(member2);
        }).getMessage(), "이미 존재하는 회원입니다.");

        //then
        //Assertions.fail("예외가 발생해야 한다.");
    }

}