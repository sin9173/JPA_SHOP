package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    // Merge (데이터 수정의 경우)
    // 1. 파라미터로 들어온 아이디값을 가지고 영속성컨텍스트와 DB 에서 데이터를 가져옴
    // 2. 기존 데이터에 파라미터로 들어온 모든 필드들을 변경 (값이 없을 경우 null 로 업데이트)
    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }


    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        //변경 감지 (영속상태에 있는 객체를 수정하면 트랜젝션 후 update 수행)
        Item findItem = itemRepository.findOne(itemId);
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stockQuantity);
    }

}
