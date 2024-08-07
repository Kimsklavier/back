package com.back.repository.jdbctemplate;

import com.back.domain.Item;
import com.back.repository.ItemRepository;
import com.back.repository.ItemSearchCond;
import com.back.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.*;

/**
 * JdbcTemplate
 */
@Slf4j
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("item")
                .usingGeneratedKeyColumns("id")
                .usingColumns("item_name", "price", "quantity");
    }

    @Override
    public Item save(Item item) {

        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        Number key = jdbcInsert.executeAndReturnKey(param);
        item.setId(key.longValue());
        log.info("데이터 저장 완료 item.id={}", item.getId());
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name= :itemName, price= :price, quantity= :quantity where id = :id";
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);
        template.update(sql, param);

    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name as itemName, price, quantity from item where id = :id";

        try {
            Map<String, Object> param = new HashMap<>();
            param.put("id", id);
            Item item = template.queryForObject(sql, param, itemRowMapper());
            // queryForObject() -> query() 로 얻은 resultSet을 new RowMapperResultSetExtractor<>(rowMapper, 1) -> extractData() 를 거쳐
            // Item 객체로 변환하여 반환.
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        String sql = "select id, item_name, price, quantity from item";

        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            //null, 길이가 0, 공백("" or " ")이면 false
            sql += " where";
        }

        boolean andFlag = false;
        List<Object> paramList = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', :itemName, '%')";
            paramList.add(itemName);
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            paramList.add(maxPrice);
        }

        log.info("sql={}", sql);
        return template.query(sql,param,  itemRowMapper());
    }
}
