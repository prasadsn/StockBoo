package com.stockboo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

/**
 * Created by prasad on 13-09-2015.
 */
public class StockList implements Parcelable {

    @DatabaseField(generatedId = true, columnName = "_id")
    public int _Id;

    @DatabaseField
    @SerializedName("SYMBOL")
    private String SYMBOL;

    @DatabaseField
    @SerializedName("ScriptName")
    private String ScriptName;

    @DatabaseField
    @SerializedName("Status")
    private String Status;

    @DatabaseField
    @SerializedName("ISINNO")
    private String ISINNO;

    @DatabaseField
    @SerializedName("Industry")
    private String Industry;

    @DatabaseField
    @SerializedName("Group")
    private String Group;

    @DatabaseField
    @SerializedName("ScriptID")
    private String ScriptID;

    public String getScriptName(){
        return ScriptName;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
