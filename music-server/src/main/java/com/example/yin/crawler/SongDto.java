package com.example.yin.crawler;

public class SongDto {
    private String songName;

    private String songUrl;
    private int cmNum;

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public int getCmNum() {
        return cmNum;
    }

    public void setCmNum(int cmNum) {
        this.cmNum = cmNum;
    }
}
