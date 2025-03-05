package com.green.jobdone.business.model.get;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class BusinessGetRevenueResByAdmin {
    private String BusinessName;
    private String detailTypeName;
    private int totalRevenue;
    private int thisMonthRevenue;
}
