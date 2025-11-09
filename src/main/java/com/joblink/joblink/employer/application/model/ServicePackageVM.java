package com.joblink.joblink.employer.application.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ServicePackageVM {
    Integer id;
    String code;
    String name;

    String description;       

    Integer duration;          
    BigDecimal price;          
    BigDecimal originalPrice;  

    boolean flash;             
    boolean isNew;            

    String bannerUrl;        
    String helpUrl;           

    Integer quantity;         

    Scope scope;              
    String scopeDetailUrl;    

    @Value
    @Builder
    public static class Scope {
        String locations;     
        String levels;         
        String industries;    
    }
}
