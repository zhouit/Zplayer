package org.zplayer.resp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class LoadManager extends Thread{
  LoadOver callback;
  Map<String, List<MusicInfo>> maps;
  CountDownLatch cdlatch;

  public LoadManager(LoadOver over, Map<String, List<MusicInfo>> maps){
    this.callback = over;
    this.maps = maps;
    cdlatch = new CountDownLatch(getItems());
  }

  public void run(){
    for(List<MusicInfo> tempList : maps.values()){
      for(MusicInfo tempMusic : tempList){
        new Loader(tempMusic, cdlatch).parse();
      }
    }

    try{
      cdlatch.await();
      AppConfig.debug("load over!");
      if(callback != null) callback.loadOver();
    }catch(InterruptedException e){
      e.printStackTrace();
    }
  }

  int getItems(){
    int result = 0;
    for(List<MusicInfo> tempList : maps.values()){
      result += tempList.size();
    }

    return result;
  }

  public static interface LoadOver{
    public void loadOver();
  }

  private static class Loader implements Runnable{
    MediaPlayer player;
    MusicInfo info;
    CountDownLatch clatch;
    int tryCount = 0;

    public Loader(MusicInfo inf, CountDownLatch clatch){
      this.info = inf;
      this.player = new MediaPlayer(new Media(info.url));
      this.clatch = clatch;
    }

    void parse(){
      this.player.setOnReady(this);
      this.player.setOnError(new Runnable(){
        public void run(){
          System.err.println("Error = " + info.getFullname());
          if(tryCount >= 1){
            clatch.countDown();
            return;
          }

          tryCount++;
          parse();
        }
      });
    }

    public void run(){
      AppConfig.debug("do run = " + info.getFullname());
      info.time = player.getMedia().getDuration();
      if(tryCount == 0) clatch.countDown();
    }

  }

}
