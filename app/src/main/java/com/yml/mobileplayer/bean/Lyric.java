package com.yml.mobileplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 歌词信息
 */
public class Lyric implements Parcelable{

    /**
     * 歌词内容
     */
    private String content;

    /**
     * 时间戳
     */
    private long timePoint;

    /**
     * 休眠时间或者高亮显示时间
     */
    private long sleepTime;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "content='" + content + '\'' +
                ", timePoint=" + timePoint +
                ", sleepTime=" + sleepTime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeLong(this.timePoint);
        dest.writeLong(this.sleepTime);
    }

    public Lyric() {
    }

    protected Lyric(Parcel in) {
        this.content = in.readString();
        this.timePoint = in.readLong();
        this.sleepTime = in.readLong();
    }

    public static final Creator<Lyric> CREATOR = new Creator<Lyric>() {
        @Override
        public Lyric createFromParcel(Parcel source) {
            return new Lyric(source);
        }

        @Override
        public Lyric[] newArray(int size) {
            return new Lyric[size];
        }
    };
}
