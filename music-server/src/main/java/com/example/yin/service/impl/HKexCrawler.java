package com.example.yin.service.impl;

import com.example.yin.crawler.StockDto;
import com.example.yin.dao.HkStockMapper;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.github.stuxuhai.jpinyin.ChineseHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


@Service
public class HKexCrawler implements Runnable{

    private static final Logger logger = LogManager.getLogger(HKexCrawler.class);

    private String startDate;
    private String endDate;
    private HkStockMapper hkStockMapper;

    public HKexCrawler(){

    }
    public HKexCrawler(String start, String end, HkStockMapper hkStockMapper) {
        this.startDate = start;
        this.endDate = end;
        this.hkStockMapper = hkStockMapper;
    }

    public void doTask() {
        logger.info(Thread.currentThread().getId()+"HKexCrawler start");

        String shurl = "https://www3.hkexnews.hk/sdw/search/mutualmarket_c.aspx?t=sh&t=sh";
        String szurl = "https://www3.hkexnews.hk/sdw/search/mutualmarket_c.aspx?t=sz&t=sz";
        //从2021-07-11 开始抓取数据，每天抓取一次
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        start.set(Calendar.YEAR, Integer.parseInt(StringUtils.split(startDate, "-")[0]));
        start.set(Calendar.MONTH, Integer.parseInt(StringUtils.split(startDate, "-")[1]) -1);
        start.set(Calendar.DATE, Integer.parseInt(StringUtils.split(startDate, "-")[2]));
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        end.set(Calendar.YEAR, Integer.parseInt(StringUtils.split(endDate, "-")[0]));
        end.set(Calendar.MONTH, Integer.parseInt(StringUtils.split(endDate, "-")[1]) - 1);
        end.set(Calendar.DATE, Integer.parseInt(StringUtils.split(endDate, "-")[2]));
        while(!start.after(end)){
            stockCrawler(DateFormatUtils.format(start.getTime(),"yyyy/MM/dd"), shurl,"SH");
            stockCrawler(DateFormatUtils.format(start.getTime(),"yyyy/MM/dd"), szurl,"SZ");
            start.add(Calendar.DATE,1);
        }
        System.out.println(Thread.currentThread().getId() + "HKexCrawler finished");
    }

    public void stockCrawler(String date, String url,String source) {
        logger.info("stockCrawler - start");
        WebClient client = new WebClient(BrowserVersion.CHROME);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setActiveXNative(false);
        client.waitForBackgroundJavaScript(1 * 1000);
        client.getOptions().setJavaScriptEnabled(false); //很重要，启用JS
        client.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX
        client.getOptions().setDoNotTrackEnabled(false);
        client.getOptions().setTimeout(600 * 1000);
        try {
            HtmlPage page = client.getPage(url);
            HtmlTextInput input = (HtmlTextInput) page.getElementById("txtShareholdingDate"); //
            input.setValueAttribute(date);
            //submit没有name，只有class和value属性，通过value属性定位元素
            HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("btnSearch");
            page = (HtmlPage) submit.click();//登录进入
            String content = page.asXml();
            Document doc = Jsoup.parse(content);
            List<StockDto> resultList = analysisData(doc,source);
            logger.info("stockCrawler - end");

            // printResult(resultList);
            saveStock(resultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.close();
    }

    private void printResult(List<StockDto> resultList) {
        for (StockDto dto : resultList) {
            System.out.println("日期：" + dto.getStockDate() + ",股票代码:" + dto.getHkCode() + ",股票名称:" + dto.getStockName()
                    + ",股票数量:" + dto.getStockNum() + ",股票占比:" + dto.getStockPercent());
        }
    }

    private List<StockDto> analysisData(Document doc,String source) {
        List<StockDto> result = new ArrayList<>();

        Elements trList = doc.select("#mutualmarket-result tr");
        String date = doc.select("#txtShareholdingDate").val();
        trList.forEach(td -> {
            StockDto dto = new StockDto();
            dto.setHkCode(td.select(".col-stock-code").select(".mobile-list-body").text());
            String name = ChineseHelper.convertToSimplifiedChinese(td.select(".col-stock-name").select(".mobile-list-body").text());
            dto.setStockName(name);
            dto.setStockNum(td.select(".col-shareholding").select(".mobile-list-body").text().replaceAll(",",""));
            dto.setStockPercent(td.select(".col-shareholding-percent").select(".mobile-list-body").text().replaceAll("%",""));
            dto.setStockDate(date);
            dto.setSource(source);
            if(StringUtils.isNoneEmpty(name)){
                result.add(dto);
            }
        });
        System.out.println(Thread.currentThread().getId() +":" + source + ",date finished"+ date);

        return result;
    }

    private void saveStock(List<StockDto> resultList) {
        logger.info("saveStock - start");

        if(null != resultList && resultList.size() > 0){
            hkStockMapper.batchInsert(resultList);
        }
        logger.info("saveStock - end");

//        for (StockDto dto : resultList) {
//            try {
//                if (StringUtils.isNotEmpty(dto.getHkCode())) {
//                    hkStockMapper.insert(dto);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        //       }
    }

    @Override
    public void run() {
        doTask();
    }
}
