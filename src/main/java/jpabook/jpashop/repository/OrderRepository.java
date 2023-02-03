package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

//    public List<Order> findAll(OrderSearch orderSearch) {
//        return em.createQuery("select o from Order o join o.member m where o.status = :status and m.name like :name", Order.class)
//                .setParameter("status", orderSearch.getOrderStatus())
//                .setParameter("name", orderSearch.getMemberName())
//                .setMaxResults(1000) //최대 1000건
//                .getResultList();
//    }

    //동적 JPQL 을 String 으로 직접 생성하는 방식 
    // 코드가 길어져 유지보수가 어려움
    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m";

        boolean isFirstCondition = true;

        //주문 상태 검색
        if(orderSearch.getOrderStatus() != null) {
            if(isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())) {
            if(isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }
    

    //JPA Criteria
    //JPA 에서 동적쿼리를 사용하도록 표준으로 제공
    //실제 쿼리가 연상되지 않아 유지보수가 어려움
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if(orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        QOrder order = QOrder.order;
        QMember member = QMember.member;

        JPAQueryFactory query = new JPAQueryFactory(em);

        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String memberName) {
        if(!StringUtils.hasText(memberName)) {
            return null;
        }
        return QMember.member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if(statusCond == null) {
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }


    //Fetch 조인 조회
    public List<Order> findAllByWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        //fetch 조인 : JPQL 에서 성능최적화를 위해 제공하는 기능
                        // - 연관된 엔티티 또는 컬렉션을 한번의 SQL 로 가져옴
                        // - LAZY 보다도 우선됨
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<Order> findAllWithItem() {
        return em.createQuery(
                //distinct 가 있을 경우 JPA 에서 같은 아이디값이 있을 경우 중복을 제거 해줌 (실제로는 여러개가 조회됨)
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" + //join 특성상 같은 주문데이터가 여러개가 생김
                        " join fetch oi.item i", Order.class
        ).getResultList();
    }

    public List<Order> findAllByWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o" +
                        //fetch 조인 : JPQL 에서 성능최적화를 위해 제공하는 기능
                        // - 연관된 엔티티 또는 컬렉션을 한번의 SQL 로 가져옴
                        // - LAZY 보다도 우선됨
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    // 1:N 을 fetch 조인시 firstResult/maxResults (페이징쿼리) 가 실제 쿼리에 반영이 되지 않음
    // - 데이터를 모두 가져온 후 메모리상에서 페이징을 수행함 (메모리 이슈가 발생될 수 있음)
    // - 데이터 Row 수가 변하지 않는 쿼리의 경우 fetch 조인 시 페이징이 가능
    // default_batch_fetch_size 설정으로 in 조건으로 별도로 가져오도록 할 수 있음
    
    // fetch 조인을 여러개 사용시 데이터 정합성이 깨질 수 있음

}
