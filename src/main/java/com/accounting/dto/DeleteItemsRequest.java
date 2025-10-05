package com.accounting.dto;

import java.util.List;

public class DeleteItemsRequest {
    private List<String> itemNames;
    
    public DeleteItemsRequest() {}
    
    public DeleteItemsRequest(List<String> itemNames) {
        this.itemNames = itemNames;
    }
    
    public List<String> getItemNames() {
        return itemNames;
    }
    
    public void setItemNames(List<String> itemNames) {
        this.itemNames = itemNames;
    }
}
