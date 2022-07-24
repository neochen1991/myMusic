package com.example.yin.dao;

import com.example.yin.crawler.StockDto;
import com.example.yin.crawler.StockQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HkStockMapper {
    //int deleteByPrimaryKey(Integer id);

    int insert(StockDto record);

    int batchInsert(List<StockDto> list);

    List<StockDto> selectListByPage(StockQuery query);

    List<StockDto> selectByCode(StockDto dto);

    int updateCode(StockDto dto);

    List<StockDto> selectRelListByPage(StockQuery query);
    List<StockDto> selectNullRelListByPage(StockQuery query);
}
