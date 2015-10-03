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

    public StockList(){

    }

    public StockList(Parcel in) {
        _Id = in.readInt();
        SYMBOL = in.readString();
        ScriptName = in.readString();
        Status = in.readString();
        ISINNO = in.readString();
        Industry = in.readString();
        Group = in.readString();
        ScriptID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_Id);
        dest.writeString(SYMBOL);
        dest.writeString(ScriptName);
        dest.writeString(Status);
        dest.writeString(ISINNO);
        dest.writeString(Industry);
        dest.writeString(Group);
        dest.writeString(ScriptID);
    }
    public int get_Id() {
        return _Id;
    }

    public String getSYMBOL() {
        return SYMBOL;
    }

    public String getStatus() {
        return Status;
    }

    public String getISINNO() {
        return ISINNO;
    }

    public String getIndustry() {
        return Industry;
    }

    public String getGroup() {
        return Group;
    }

    public String getScriptID() {
        return ScriptID;
    }

    public static final Parcelable.Creator<StockList> CREATOR = new Parcelable.Creator<StockList>() {
        public StockList createFromParcel(Parcel source) {
            return new StockList(source);
        }

        public StockList[] newArray(int size) {
            return new StockList[size];
        }
    };
}
