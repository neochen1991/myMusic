package com.example.yin.crawler;

import java.util.List;

public class SingerDto {
    private String name;
    private String singerUrl;

    List<SongDto> songList;
    public SingerDto(String name, String singerUrl) {
        this.name = name;
        this.singerUrl = singerUrl;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSingerUrl() {
        return singerUrl;
    }

    public void setSingerUrl(String singerUrl) {
        this.singerUrl = singerUrl;
    }

    public List<SongDto> getSongList() {
        return songList;
    }

    public void setSongList(List<SongDto> songList) {
        this.songList = songList;
    }
}
