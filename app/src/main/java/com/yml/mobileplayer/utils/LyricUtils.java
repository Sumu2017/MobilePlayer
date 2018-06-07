package com.yml.mobileplayer.utils;

import com.yml.mobileplayer.bean.Lyric;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 解析歌词工具类
 */
public class LyricUtils {

    public static List<Lyric> readLyricFile(File file) {
        List<Lyric> lyrics = new ArrayList<>();
        if (file == null || !file.exists()) {
            return lyrics;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                lyrics.addAll(parseLine(line));
            }

            sortLyrics(lyrics);

            computeSleepTime(lyrics);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return lyrics;
    }

    /**
     * 解析单句歌词 [02:04.12][03:37.32][00:59.73]我在这里欢笑
     *
     * @param line
     * @return
     */
    public static List<Lyric> parseLine(String line){
        List<Lyric> lyrics = new ArrayList<>();
        String[] splitLyrics = line.split("\\]");
        String content=splitLyrics[splitLyrics.length-1];
        for (int i = 0; i < splitLyrics.length-1; i++) {
            String time=splitLyrics[i].substring(1,splitLyrics[i].length());
            long timePoint=strTime2LongTime(time);
            Lyric lyric=new Lyric();
            lyric.setContent(content);
            lyric.setTimePoint(timePoint);
            lyrics.add(lyric);
        }
        return lyrics;
    }

    /**
     * 对歌词按时间戳进行排序
     * @param lyrics
     */
    public static void sortLyrics(List<Lyric> lyrics){
          Collections.sort(lyrics, new Comparator<Lyric>() {
            @Override
            public int compare(Lyric lhs, Lyric rhs) {
                if(lhs.getTimePoint() < rhs.getTimePoint()){
                    return  -1;
                }else if(lhs.getTimePoint() > rhs.getTimePoint()){
                    return  1;
                }else{
                    return 0;
                }

            }
        });
    }

    /**
     * 计算每句高亮显示的时间
     * @param lyrics
     */
    public static void computeSleepTime(List<Lyric> lyrics){
        for(int i=0;i<lyrics.size();i++){
            Lyric oneLyric = lyrics.get(i);
            if(i+1 < lyrics.size()){
                Lyric twoLyric = lyrics.get(i+1);
                oneLyric.setSleepTime(twoLyric.getTimePoint()-oneLyric.getTimePoint());
            }
        }
    }

    /**
     * 把String类型是时间转换成long类型
     *
     * @param strTime 02:04.12
     * @return
     */
    public static long strTime2LongTime(String strTime) {
        long result = -1;
        try {

            //1.把02:04.12按照:切割成02和04.12
            String[] s1 = strTime.split(":");
            //2.把04.12按照.切割成04和12
            String[] s2 = s1[1].split("\\.");

            //1.分
            long min = Long.parseLong(s1[0].substring(1,2));

            //2.秒
            long second = Long.parseLong(s2[0]);

            //3.毫秒
            long mil = Long.parseLong(s2[1]);

            result = min * 60 * 1000 + second * 1000 + mil * 10;
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }

        return result;
    }

    /**
     * 判断文件编码
     * @param file 文件
     * @return 编码：GBK,UTF-8,UTF-16LE
     */
    public static String getCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }
}
