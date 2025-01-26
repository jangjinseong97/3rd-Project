package com.green.jobdone.business.model.get;

import com.green.jobdone.common.PicUrlMaker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class BusinessGetRes {
    private String pic;
    private long businessId;
    private String businessName;
    private String title;
    private double scoreAvg;
    private int price;
    private int like;

    public BusinessGetRes(String pic, long businessId, String businessName, String title, double scoreAvg, int price, int like) {
        this.pic = PicUrlMaker.makePicUrl(businessId, pic);
        this.businessId = businessId;
        this.businessName = businessName;
        this.title = title;
        this.scoreAvg = scoreAvg;
        this.price = price;
        this.like = like;
    }
}
