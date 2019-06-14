package com.example.karaoke;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析歌詞
 */

public class LyricBuilder {

    public List<LyricRow> getLrcRows(String rawLrc){
        if (rawLrc == null || rawLrc.length() == 0) return null;


        StringReader reader = new StringReader(rawLrc);
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        List<LyricRow> rows = new ArrayList<LyricRow>();
        try {
            do {
                line = br.readLine();

                if (line != null && line.length() > 0) {

                    List<LyricRow> lrcRows = new ArrayList<LyricRow>();

                    try {

                        //從"]"，拆成前後兩段
                        String content = line.substring(10);
                        String times = line.substring(0, 10).replace("[", "").replace("]", "");

                        LyricRow lrcRow = new LyricRow();
                        lrcRow.setContent(content);
                        long startTime = timeConvert(times);
                        lrcRow.setStartTime(startTime);
                        lrcRows.add(lrcRow);


                    } catch (Exception e) {

                    }

                    if (lrcRows != null && lrcRows.size() > 0) {
                        for (LyricRow row : lrcRows) {
                            rows.add(row);
                        }
                    }
                }
            } while (line != null);

            if (rows.size() > 0) {

                if (rows != null && rows.size() > 0) {
                    int size = rows.size();
                    for (int i = 0; i < size; i++) {
                        LyricRow lrcRow = rows.get(i);

                  if (i < size - 1) {
                            lrcRow.setEndTime(rows.get(i + 1).getStartTime());
                        } else {
                            lrcRow.setEndTime(lrcRow.getStartTime() + 10000);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
        }
        return rows;
    }


    /**
     * 將時間由String轉成Long Type
     */
    private static long timeConvert(String timeString) {

        timeString = timeString.replace('.', ':');
        String[] times = timeString.split(":");

        return Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000 + Integer.valueOf(times[2]);
    }
}
