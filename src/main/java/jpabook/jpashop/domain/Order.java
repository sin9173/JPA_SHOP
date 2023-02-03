package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //protected 접근제어자의 기본생성자를 만들어줌
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    //EAGER : 즉시로딩
    // - DB 에서 연관된 데이터를 모두 가져옴
    // - 데이터를 리스트로 가져올 경우 각 리스트마다 연관데이터에 해당하는 SQL SELECT 단일 쿼리를 보냄 (성능적인 문제 발생)

    //LAZY : 지연로딩
    // - 객체를 사용할때 데이터를 가져옴

    //가급적 LAZY 를 사용 (양방향 연관관계가 있거나 불필요한 로딩이 많을 경우 문제가 발생)
    //ManyToOne 이나 OneToOne 의 경우 기본 fetch 전략이 EAGER 이므로 주의가 필요
    //LAZY 로 해둘 경우 new ByteBuddyInterceptor() 라는 프록시객체가 생성
    // - 프록시 객체에서는 실제객체의 레퍼런스를 보관
    // - 조회시 레퍼런스에 값이 없을 경우 영속성 컨텍스트에 요청
    // - 영속성 컨텍스트에서 객체를 생성해준 후 레퍼런스에 값을 넣어줌 (프록시 객체를 초기화)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //Cascade 가 없을 경우 OrderItem 을 저장 또는 삭제를 각각 해주어야 함
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;
    
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]
    
    
    /* 도메인 모델 패턴 */
    // - 엔티티가 비즈니스 로직을 가지고 객체지향의 특성을 활용

    //==연관관계 메소드==//
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //생성 메소드
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //주문취소
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }


    //전체 주문 가격 조회
    public int getTotalPrice() {
        return orderItems.stream().mapToInt(i -> i.getTotalPrice()).sum();
    }

}
