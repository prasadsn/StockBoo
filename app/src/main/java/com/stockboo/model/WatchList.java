package com.stockboo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

/**
 * Created by prasad on 13-09-2015.
 */
public class WatchList implements Parcelable {

    @DatabaseField(generatedId = true, columnName = "_id")
    public int _Id;

    @DatabaseField
    private String SYMBOL;

    @DatabaseField
    private String ScriptName;

    @DatabaseField
    private String Status;

    @DatabaseField
    private String ISINNO;

    @DatabaseField
    private String Industry;

    @DatabaseField
    private String Group;

    @DatabaseField
    private String price;

    @DatabaseField
    private String ScriptID;

    @DatabaseField
    private String c;

    @DatabaseField
    private String c_fix;

    /*protected WatchList(Parcel in) {
        _Id = in.readInt();
        SYMBOL = in.readString();
        ScriptName = in.readString();
        Status = in.readString();
        ISINNO = in.readString();
        Industry = in.readString();
        Group = in.readString();
        price = in.readString();
        ScriptID = in.readString();
        c = in.readString();
        c_fix = in.readString();
    }*/

    public static final Creator<WatchList> CREATOR = new Creator<WatchList>() {
        @Override
        public WatchList createFromParcel(Parcel in) {
            return new WatchList(in);
        }

        @Override
        public WatchList[] newArray(int size) {
            return new WatchList[size];
        }
    };

    public String getScriptName(){
        return ScriptName;
    }
    @Override
    public int describeContents() {
        return 0;
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
        dest.writeString(price);
        dest.writeString(ScriptID);
        dest.writeString(c);
        dest.writeString(c_fix);
    }
}
