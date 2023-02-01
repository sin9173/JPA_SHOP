package jpabook.jpashop.repository.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;


    //DTO 로 직접 바인딩
    public List<OrderSimpleQueryDto> findOrderDtos() {
        //SQL 작성시 지정된 컬럼만 가져옴
        return em.createQuery("select new jpabook.jpashop.repository.simplequery.OrderSimpleQueryDto(o.id, o.member.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d",
                OrderSimpleQueryDto.class).getResultList();
    }
}
