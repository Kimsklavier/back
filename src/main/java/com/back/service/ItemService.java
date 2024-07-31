package com.back.service;

import com.back.domain.Item;
import com.back.repository.ItemSearchCond;
import com.back.repository.ItemUpdateDto;
import java.util.List;
import java.util.Optional;

public interface ItemService {

    Item save(Item item);

    void update(Long itemId, ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findItems(ItemSearchCond itemSearch);
}
