package com.stockboo.model;

import com.j256.ormlite.field.DatabaseField;
import com.stockboo.model.StockList;

/**
 * Created by prsn0001 on 10/12/2015.
 */
public class Portfolio extends StockList {

    public Portfolio(StockList list, int quantity, int price){
        super(list);
        this.quantity = quantity;
        this.price = price;
    }
    @DatabaseField
    private int quantity;

    @DatabaseField
    private int price;

    @DatabaseField
    private int currentPrice;
}
