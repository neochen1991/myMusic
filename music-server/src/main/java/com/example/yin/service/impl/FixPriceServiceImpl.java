package com.example.yin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.yin.crawler.StockDto;
import com.example.yin.crawler.StockQuery;
import com.example.yin.dao.HkStockMapper;
import com.example.yin.dao.StockPriceRecordMapper;
import com.example.yin.domain.StockPriceRecord;
import com.example.yin.service.HkStockService;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FixPriceServiceImpl implements Runnable {

    private static final Logger logger = LogManager.getLogger(HKexCrawler.class);
    private HkStockMapper hkStockMapper;
    private StockPriceRecordMapper stockPriceRecordMapper;

    public FixPriceServiceImpl(){

    }
    public FixPriceServiceImpl(HkStockMapper hkStockMapper,StockPriceRecordMapper stockPriceRecordMapper) {
        this.hkStockMapper = hkStockMapper;
        this.stockPriceRecordMapper = stockPriceRecordMapper;
    }

    public void dofixTask() {
        try {
            String baseUrl = "https://q.stock.sohu.com/hisHq?code=cn_";
            StockQuery query = new StockQuery();
            query.setStart(0);
            query.setPageSize(1000);
            List<StockDto> result = hkStockMapper.selectRelListByPage(query);
            while (result != null && result.size() > 0) {
                for (StockDto dto : result) {
                    try {
                        String url = baseUrl + dto.getCode() + "&start=20210712&end=20220717";
                        CloseableHttpClient client = HttpClientBuilder.create().build();
                        HttpGet httpGet = new HttpGet(url);
                        CloseableHttpResponse response = client.execute(httpGet);
                        HttpEntity entity = response.getEntity();

                        String content = EntityUtils.toString(entity);
                        logger.info("url:" + url+ ",content:"+ content);
                        JSONArray json = (JSONArray) JSON.parse(content);
                        JSONArray array = ((JSONObject) json.get(0)).getJSONArray("hq");
                        array.forEach(e -> {
                            StockPriceRecord record = new StockPriceRecord();
                            record.setCode(dto.getCode());
                            record.setSource(dto.getSource());
                            record.setStockDate(((JSONArray) e).get(0).toString());
                            record.setOpenPrice(new BigDecimal(((JSONArray) e).get(1).toString()));
                            record.setClosingPrice(new BigDecimal(((JSONArray) e).get(2).toString()));
                            record.setPriceChange(new BigDecimal(((JSONArray) e).get(3).toString()));
                            record.setChangePercent(new BigDecimal(((JSONArray) e).get(4).toString()
                                    .replaceAll("%", "")).divide(new BigDecimal(100)));
                            record.setLowPrice(new BigDecimal(((JSONArray) e).get(5).toString()));
                            record.setHighPrice(new BigDecimal(((JSONArray) e).get(6).toString()));
                            record.setTranVol(new BigDecimal(((JSONArray) e).get(7).toString()));
                            record.setTranValue(new BigDecimal(((JSONArray) e).get(8).toString())
                                    .multiply(new BigDecimal(10000)));
                            record.setHuanShouLv(new BigDecimal(((JSONArray) e).get(9).toString()
                                    .replaceAll("%", "")).divide(new BigDecimal(100)));

                            stockPriceRecordMapper.insert(record);
                        });
                        logger.info(Thread.currentThread().getName());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                query.setStart(query.getStart() + query.getPageSize());
                result = hkStockMapper.selectRelListByPage(query);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        dofixTask();
    }
}
