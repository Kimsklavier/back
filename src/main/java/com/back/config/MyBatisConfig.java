package com.back.config;

import com.back.repository.ItemRepository;
import com.back.repository.jdbctemplate.JdbcTemplateItemRepositoryV3;
import com.back.repository.mybatis.ItemMapper;
import com.back.repository.mybatis.MyBatisItemRepository;
import com.back.service.ItemService;
import com.back.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class MyBatisConfig {
    private final ItemMapper itemMapper;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new MyBatisItemRepository(itemMapper);
    }
}
