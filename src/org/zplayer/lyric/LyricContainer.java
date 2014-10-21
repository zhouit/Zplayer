package org.zplayer.lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.zplayer.PlayAccordion;
import org.zplayer.utils.FontUtils;
import org.zplayer.utils.IOUtils;

public abstract class LyricContainer implements Runnable{
  public static final int refreshInterval = 150;
  public static boolean lyric_init = false; // 歌词容器是否实例化

  // static final Color LEFT_COLOR = Color.rgb(158, 254, 28);
  // static final Color RIGHT_COLOR = Color.rgb(70, 175, 0);
  static final Color LEFT_COLOR = Color.rgb(253, 251, 10);
  static final Color RIGHT_COLOR = Color.rgb(240, 240, 240);

  // static final Color TOP_COLOR = Color.rgb(238, 254, 218);
  // static final Color CENTER_COLOR = Color.rgb(153, 254, 17);
  // static final Color BOTTOM_COLOR = Color.rgb(232, 254, 3);

  protected List<Sentence> sentences;
  protected volatile boolean isPlay, shutdown;
  protected boolean hasLyric; // 是否有歌词,若没有直接显示歌曲名
  protected Object lock;
  protected Thread thread;
  protected int lastIndex = -1; // 上次显示的歌词行数

  LyricContainer(){
    this.shutdown = true;
    this.hasLyric = true;
    this.sentences = new ArrayList<Sentence>();
    this.lock = new Object();
  }

  private static final class LyricHolder{
    static LyricContainer INSTANCE = new SwingLyricContainer();
    // static LyricContainer INSTANCE=new JavafxLyricContainer();
  }

  public static LyricContainer getLyricContainer(){
    return LyricHolder.INSTANCE;
  }

  public abstract void showLyric();

  public abstract void run();

  protected void setLyric(File file){
    sentences.clear();

    if(!file.exists() || file.isDirectory() || !file.getName().toLowerCase().endsWith(".lrc")){
      System.out.println(file.getAbsolutePath() + "歌词未找到!");
      return;
    }

    hasLyric = true;
    Sentence first = new Sentence(0, PlayAccordion.playIndex.get().getFullname());
    sentences.add(0, first);
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
      String line = null;
      while((line = reader.readLine()) != null){
        if(line.length() < 1 || !Character.isDigit(line.charAt(1))) continue;

        String time = line.substring(1, 9);
        Sentence sentence = new Sentence(Sentence.parseTime(time), line.substring(10));
        sentences.get(sentences.size() - 1).setToTime(sentence.fromTime);
        sentences.add(sentence);
      }

      if(sentences.size() > 1) first.setToTime(sentences.get(1).fromTime);

      sentences.get(sentences.size() - 1).setToTime(
          (long) PlayAccordion.playIndex.get().time.toMillis());
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      IOUtils.closeQuietly(reader);
    }
  }

  protected int getCurrentSentence(long time){
    for(int i = 0; i < sentences.size(); i++){
      if(sentences.get(i).isInTime(time)){
        return i;
      }
    }

    return 0;
  }

  public void pause(){
    isPlay = false;
  }

  public void play(){
    isPlay = true;
    synchronized(lock){
      lock.notifyAll();
    }
  }

  public void shutdown(){
    shutdown = true;
    synchronized(lock){
      lock.notifyAll();
    }
  }

  static final class Sentence{
    long fromTime;
    long toTime;
    String content;
    int contenLength;

    public Sentence(){
    }

    public Sentence(long fromTime, String content){
      this.fromTime = fromTime;
      this.content = content;
    }

    public void setToTime(long toTime){
      this.toTime = toTime == 0 ? Integer.MAX_VALUE : toTime;
    }

    public boolean isInTime(long target){
      return toTime > target && fromTime <= target;
    }

    public long getTime(){
      return toTime - fromTime;
    }

    static long parseTime(String str){
      int minute = Integer.parseInt(str.substring(0, 2));
      int seconds = Integer.parseInt(str.substring(3, 5));
      int micseconds = Integer.parseInt(str.substring(6, 8));

      return minute * 60 * 1000 + seconds * 1000 + micseconds * 10L;
    }

    public int getContentLength(){
      if(contenLength == 0) contenLength = FontUtils.getPixLength(content,
          Font.font("KaiTi", FontWeight.BLACK, 40));
      return contenLength;
    }

    public String toString(){
      return "[" + fromTime + " , " + toTime + "]" + content;
    }

  }

}
