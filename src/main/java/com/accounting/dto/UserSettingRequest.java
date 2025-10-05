package com.accounting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserSettingRequest {
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("settingKey")
    private String settingKey;
    
    @JsonProperty("settingValue")
    private String settingValue;
}

@Data
class DefaultMealItemsRequest {
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("mealItems")
    private List<String> mealItems;
}
