package com.example.yin.service.impl;

import com.example.yin.crawler.StockDto;
import com.example.yin.dao.AdminMapper;
import com.example.yin.dao.HkStockMapper;
import com.example.yin.dao.StockPriceRecordMapper;
import com.example.yin.service.AdminService;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.github.stuxuhai.jpinyin.ChineseHelper;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private HkStockMapper hkStockMapper;
    @Autowired
    private StockPriceRecordMapper stockPriceRecordMapper;

    @Override
    public boolean veritypasswd(String name, String password) {

        return adminMapper.verifyPassword(name, password) > 0 ? true : false;
    }
    @Override
    public void doFixTask(){
        for(int i = 0;i < 1 ;i++){
            new Thread(new FixPriceServiceImpl(hkStockMapper,stockPriceRecordMapper)).start();
            //new Thread(new FixCodeServiceImpl(hkStockMapper)).start();
        }
    }
    @Override
    public void doTask() {

        new Thread(new HKexCrawler("2022-07-12","2022-07-31",hkStockMapper)).start();

    }

    public void stockCrawler(String date, String url,String source) {

        WebClient client = new WebClient(BrowserVersion.CHROME);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setActiveXNative(false);
        client.waitForBackgroundJavaScript(600 * 1000);
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
            printResult(resultList);
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
            dto.setStockPercent(td.select(".col-shareholding-percent").select(".mobile-list-body").text());
            dto.setStockDate(date);
            dto.setSource(source);
            if(StringUtils.isNoneEmpty(name)){
                result.add(dto);
            }
        });
        return result;
    }

    private void saveStock(List<StockDto> resultList) {
        if(null != resultList && resultList.size() > 0){
            hkStockMapper.batchInsert(resultList);
        }
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
}
