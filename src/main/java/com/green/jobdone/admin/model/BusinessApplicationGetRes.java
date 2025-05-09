package com.green.jobdone.admin.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BusinessApplicationGetRes {
    private String applicationCreatedAt;
    private String paper;
    private String detailTypeName;
    private String userName;
    private String businessName;
    private int state;
    private long businessId;

}
