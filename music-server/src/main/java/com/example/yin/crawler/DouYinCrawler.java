package com.example.yin.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.select.Elements;

public class DouYinCrawler {

    public static void main(String[] args) {
        qqmusic();
    }

    public static void qqmusic() {

        WebClient client = new WebClient(BrowserVersion.CHROME);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setActiveXNative(false);
        client.waitForBackgroundJavaScript(600 * 1000);
        client.getOptions().setJavaScriptEnabled(true); //很重要，启用JS
        client.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX
        client.getOptions().setDoNotTrackEnabled(false);
        client.getOptions().setTimeout(600 * 1000);
        try {
            String host = "https://y.qq.com";
            String singerListUrl= host + "/n/ryqq/singer_list";

            List<SingerDto> singerList = getSingerList(client, singerListUrl);
            for(SingerDto dto : singerList){
                List<SongDto> songList = getSingerSongList(dto,client, host + dto.getSingerUrl());
                for(SongDto dto2:songList){
                    getSongDetail(client,dto2);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        client.close();
    }

    private static List<SingerDto> getSingerList(WebClient client, String singerListUrl) {
        List<SingerDto> singerList = new ArrayList<>();

        try {
            HtmlPage page = client.getPage(singerListUrl);
            String content = page.asXml();
            Document doc = Jsoup.parse(content);
            Elements songList = doc.select(".singer_list__list");
            songList.select("li").forEach(e -> {
                String name = e.select(".singer_list__cover").attr("title");
                String href = e.select(".singer_list__cover").attr("href");
                SingerDto dto = new SingerDto(name,href);
                singerList.add(dto);
            });
            printSingerList(singerList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return singerList;
    }

    private static List<SongDto> getSingerSongList(SingerDto singerDto,WebClient client, String url) {
        List<SongDto> songList = new ArrayList<>();

        try {
            HtmlPage page = client.getPage(url);
            String content = page.asXml();
            Document doc = Jsoup.parse(content);
            Elements eList = doc.select(".songlist__list");
            eList.select("li").forEach(e -> {
                String name = e.select(".songlist__songname_txt").select("a").attr("title");
                String href = e.select(".songlist__songname_txt").select("a").attr("href");
                SongDto songDto = new SongDto();
                songDto.setSongName(name);
                songDto.setSongUrl(href);
                songList.add(songDto);

            });
            singerDto.setSongList(songList);
            printList(songList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songList;
    }
    private static SongDto getSongDetail(WebClient client, SongDto dto) {
        try {
            String host = "https://y.qq.com";

            HtmlPage page = client.getPage(host+dto.getSongUrl());
            client.waitForBackgroundJavaScript(10000);
            String content = page.asXml();
            Document doc = Jsoup.parse(content);
            Elements comment = doc.select("part__tit");

            print(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dto;
    }
    public static void print(SongDto dto) {
            System.out.print("歌曲：" + dto.getSongName() + "  地址：" + dto.getSongUrl() + "  评论数：" + dto.getCmNum());
            System.out.println();

    }

    public static void printList(List<SongDto> list) {
        for (SongDto dto : list) {
            System.out.print("歌曲：" + dto.getSongName() + "  地址：" + dto.getSongUrl());
            System.out.println();

        }
    }
    public static void printSingerList(List<SingerDto> list) {
        for (SingerDto dto : list) {
            System.out.print("歌手：" + dto.getName() + "  地址：" + dto.getSingerUrl());
            System.out.println();

        }
    }


    public static void download(Map<String, String> map) throws Exception {

        for (String name : map.keySet()) {
            String basePath = "f:/douyin/";
            String path = map.get(name);
            URL url = new URL(path);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            FileOutputStream output = new FileOutputStream(new File(basePath + name + ".mp3"));

            byte[] bytes = new byte[1024];

            int length = 0;
            while ((length = inputStream.read(bytes)) != -1) {
                output.write(bytes, 0, length);
            }
            System.out.println("end");
            output.close();
            inputStream.close();
        }
    }

}
