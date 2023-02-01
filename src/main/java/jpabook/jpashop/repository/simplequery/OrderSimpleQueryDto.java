package jpabook.jpashop.repository.simplequery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    // 각 레코드마다 추가적으로 쿼리가 발생되므로 성능적인 문제가 발생
    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name; //LAZY 가 초기화됨 (select 쿼리 발생)
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address; //LAZY 가 초기화됨 (select 쿼리 발생)
    }
}
