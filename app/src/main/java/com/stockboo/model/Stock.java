package com.stockboo.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by narpr05 on 7/14/2015.
 */

@ParseClassName("Stock")
public class Stock extends ParseObject {
    public String getDisplayName() {
        return getString("displayName");
    }
    public void setDisplayName(String value) {
        put("displayName", value);
    }
}