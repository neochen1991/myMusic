package com.example.yin.service.impl;

import com.example.yin.crawler.StockDto;
import com.example.yin.crawler.StockQuery;
import com.example.yin.dao.HkStockMapper;
import com.example.yin.service.HkStockService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FixCodeServiceImpl implements HkStockService,Runnable {

    private static final Logger logger = LogManager.getLogger(HKexCrawler.class);
    private HkStockMapper hkStockMapper;

    public FixCodeServiceImpl(){

    }
    public FixCodeServiceImpl(HkStockMapper hkStockMapper) {
        this.hkStockMapper = hkStockMapper;
    }
    @Override
    public void dofixTask() {
        try {
            StockQuery query = new StockQuery();
            query.setStart(0);
            query.setPageSize(1000);
            List<StockDto> result = hkStockMapper.selectNullRelListByPage(query);
            while (result != null && result.size() > 0) {
                for (StockDto dto : result) {

                    if (dto.getHkCode().startsWith("70") || dto.getHkCode().startsWith("71")
                            || dto.getHkCode().startsWith("72") || dto.getHkCode().startsWith("73")) {
                        dto.setCode("00" + dto.getHkCode().substring(1));
                        dto.setSource("SZ");
                    }
                    if (dto.getHkCode().startsWith("77") ) {
                        dto.setCode("300" + dto.getHkCode().substring(2));
                        dto.setSource("SZ");
                    }
                    if (dto.getHkCode().startsWith("78") ) {
                        dto.setCode("301" + dto.getHkCode().substring(2));
                        dto.setSource("SZ");
                    }

                    if (dto.getHkCode().startsWith("90") || dto.getHkCode().startsWith("91")) {
                        dto.setCode("60" + dto.getHkCode().substring(1));
                        dto.setSource("SH");
                    }
                    if (dto.getHkCode().startsWith("93") || dto.getHkCode().startsWith("95")) {
                        dto.setCode("60" + dto.getHkCode().substring(1));
                        dto.setSource("SH");
                    }
                    if (dto.getHkCode().startsWith("30") ) {
                        dto.setCode("688" + dto.getHkCode().substring(2));
                        dto.setSource("SH");
                    }
                    int i = hkStockMapper.updateCode(dto);
                    logger.info(Thread.currentThread().getName()+"update id:" + dto.getId()+ ",code:" +dto.getCode() + ",result :" + i);
                }
                query.setStart(query.getStart() + query.getPageSize());
                result = hkStockMapper.selectRelListByPage(query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        dofixTask();
    }
}
