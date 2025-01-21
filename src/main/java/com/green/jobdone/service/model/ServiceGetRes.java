package com.green.jobdone.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ServiceGetRes {
    private long serviceId;
    private String reservedName;
    private String businessName;
    private String productName;
    private String userPhone;
    private String userName;
    private String address;
    private int price;
    private int completed;
    private String comment;
    private String addComment;
    private String startTime;
    private String endTime;
    private String mStartTime;
    private String mEndTime;

    private String businessNum;
    private String createdAt;
    private String serviceTypeName;

    private List<ServicePhone> phone;
    private List<ServiceOptionDto> options;
}
