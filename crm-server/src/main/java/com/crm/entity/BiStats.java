package com.crm.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BiStats {
    
    private Long customerCount;
    
    private Long todayNewCustomer;
    
    private Long monthNewCustomer;
    
    private Long businessCount;
    
    private BigDecimal contractAmount;
    
    private BigDecimal monthContractAmount;
    
    private Integer pendingContract;
    
    private Integer approvedContract;
    
    private Integer rejectedContract;
    
    private List<?> customerLevelStats;
    
    private List<?> monthlyTrend;
    
    private List<?> teamRanking;
    
    private List<?> funnelData;
}
