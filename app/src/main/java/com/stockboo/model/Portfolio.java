package com.stockboo.model;

import com.j256.ormlite.field.DatabaseField;
import com.stockboo.model.StockList;

/**
 * Created by prsn0001 on 10/12/2015.
 */
public class Portfolio extends StockList {

    public Portfolio(){

    }
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
    private double change;

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    @DatabaseField
    private int currentPrice;
}
