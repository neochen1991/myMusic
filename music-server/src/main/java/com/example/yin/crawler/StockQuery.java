package com.example.yin.crawler;

import lombok.Data;

import java.util.Date;
@Data
public class StockQuery {

    private int start;
    private int pageSize;
    private int id;
    private String hkCode;
    private String stockName;
    private String code;
    private String stockNum;
    private String stockPercent;
    private String stockDate;
    private String source;
    private Date createTime;
    private Date updateTime;

}
