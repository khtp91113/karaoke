package com.example.karaoke;

public class LyricRow {

    /**
     * 歌詞要播放的時間
     */
    public String startTimeString;

    /**
     *  String to int 歌詞播放的時間 (ms)
     */
    public long startTime;
    public long endTime;

    /**
     * 歌詞內容
     */
    public String content;


    public LyricRow(){}


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "LyricRow{" +
                "startTimeString='" + startTimeString + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", content='" + content + '\'' +
                '}';
    }

    /**
     * 排序的时候，根据歌词的时间来排序
     */
    public int compareTo(LyricRow another) {
        return (int)(this.startTime - another.startTime);
    }
}
