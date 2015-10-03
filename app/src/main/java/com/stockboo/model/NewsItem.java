package com.stockboo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by prasad on 27-09-2015.
 */
public class NewsItem implements Parcelable {

    @DatabaseField(generatedId = true, columnName = "_id")
    public int _Id;

    @DatabaseField
    private String title;

    @DatabaseField
    private String description;

    @DatabaseField
    private String link;

    @DatabaseField
    private String thumbnailLink;

    @DatabaseField
    private String pubDate;

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_Id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeString(thumbnailLink);
    }
}
