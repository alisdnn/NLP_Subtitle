package com.company;

import com.company.srt.SRTParser;
import com.company.srt.Subtitle;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Main {

    public static final String EQUAL = "equal";
    public static final String ONE_TO_MANY = "one to many";
    public static final String MANY_TO_ONE = "many to one";

    public static void main(String[] args) throws FileNotFoundException {
        // write your code here

        ArrayList<Tuple<String, Tuple<String, String>>> list = new ArrayList<>();

        String firstFile = System.getProperty("user.dir") + "\\NLP_Subtitle\\files\\first.srt";
        String secondFile = System.getProperty("user.dir") + "\\NLP_Subtitle\\files\\second.srt";

        SRTParser srtParser = new SRTParser();
        Logger logger = Logger.getLogger(Main.class.getName());
        ArrayList<Subtitle> firstSubtitles = srtParser
                .getSubtitlesFromFile(firstFile, true, true);

        ArrayList<Subtitle> secondSubtitles = srtParser
                .getSubtitlesFromFile(secondFile, true, true);

        int count = 0;
        for (Subtitle firstSubtitle : firstSubtitles) {
            long firstSubtitleTimeIn = firstSubtitle.timeIn / 1000;
            long firstSubtitleTimeOut = firstSubtitle.timeOut / 1000;
            for (Subtitle secondSubtitle : secondSubtitles) {
                if (firstSubtitleTimeIn == secondSubtitle.timeIn / 1000 && firstSubtitleTimeOut == secondSubtitle.timeOut / 1000) {
                    // if TimeIn and TimeOut are equal for a line between both files
                    System.out.println(firstSubtitle.startTime + " ," + firstSubtitle.endTime + "------" + secondSubtitle.startTime + " ," + secondSubtitle.endTime);
                    System.out.println(firstSubtitle.text + "----------" + secondSubtitle.text);
                    list.add(new Tuple(EQUAL, new Tuple(firstSubtitle.text, secondSubtitle.text)));
                    count++;
                    break;
                } else if (firstSubtitleTimeIn == secondSubtitle.timeIn / 1000 && firstSubtitleTimeOut > secondSubtitle.timeOut / 1000) {
                    // if single line in 1th file is equal to multiple lines in 2nd file
                    Subtitle nextSubtitle = secondSubtitle.nextSubtitle;
                    StringBuilder out = new StringBuilder();
                    out.append(secondSubtitle.text);
                    out.append(nextSubtitle.text);
                    while (nextSubtitle.timeOut <= firstSubtitle.timeOut) {
                        out.append(nextSubtitle.text);
                        if (nextSubtitle.nextSubtitle != null) {
                            nextSubtitle = nextSubtitle.nextSubtitle;
                        } else {
                            break;
                        }
                    }
                    if (out.length() > 0) {
                        System.out.println(firstSubtitle.startTime + " ," + firstSubtitle.endTime + "------" + secondSubtitle.startTime + " ," + secondSubtitle.endTime);
                        System.out.println(firstSubtitle.text + "\n --------" + "\n" + out);
                        list.add(new Tuple(ONE_TO_MANY, new Tuple(firstSubtitle.text, out)));
                        count++;
                    }
                    break;
                } else if (firstSubtitleTimeIn == secondSubtitle.timeIn / 1000 && firstSubtitleTimeOut < secondSubtitle.timeOut / 1000) {

                    // if multiple lines in 1th file is equal to single line in 2nd file
                    Subtitle nextSubtitle = firstSubtitle.nextSubtitle;
                    StringBuilder out = new StringBuilder();
                    out.append(firstSubtitle.text);
                    out.append(nextSubtitle.text);
                    while (nextSubtitle.timeOut <= secondSubtitle.timeOut) {
                        out.append(nextSubtitle.text);
                        if (nextSubtitle.nextSubtitle != null) {
                            nextSubtitle = nextSubtitle.nextSubtitle;
                        } else {
                            break;
                        }
                    }
                    if (out.length() > 0) {
                        System.out.println(firstSubtitle.startTime + " ," + firstSubtitle.endTime + "------" + secondSubtitle.startTime + " ," + secondSubtitle.endTime);
                        System.out.println(secondSubtitle.text + "\n --------" + "\n" + out);
                        list.add(new Tuple(MANY_TO_ONE, new Tuple(firstSubtitle.text, out)));
                        count++;
                    }
                    break;
                }
            }
        }
        System.out.println("count = " + count);

        saveToFile(list);

    }

    private static void saveToFile(ArrayList<Tuple<String, Tuple<String, String>>> list) throws FileNotFoundException {
        OutputStream os = new FileOutputStream("C:\\Users\\Alisa\\Desktop\\MJ\\aaa.xlsx", true);
        try (os) {
            Workbook wb = new Workbook(os, "MyApplication", "1.0");
            Worksheet ws = wb.newWorksheet("Sheet 1");

            for (int i = 0; i < list.size(); i++) {
                ws.value(i, 0, String.valueOf(list.get(i).x));
                ws.value(i, 1, String.valueOf(list.get(i).y.x));
                ws.value(i, 2, String.valueOf(list.get(i).y.y));
            }
            wb.finish();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Tuple<X, Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}