package org.zplayer.center;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.zplayer.resp.MusicInfo;

import javafx.application.Platform;
import javafx.scene.Node;

/**
 * 延时显示tips
 * @author zhou
 *
 */
public final class DelayMusicTips extends AbstractMusicTips{
  private static DelayMusicTips tips = new DelayMusicTips();

  ScheduledFuture<?> sf;
  ScheduledExecutorService service;

  public DelayMusicTips(){
    super();
  }

  protected void initIfNeeded(){
    if(super.instantiate) return;

    super.initTips();
    service = Executors.newSingleThreadScheduledExecutor();
    instantiate = true;
  }

  public static DelayMusicTips getMusicTips(){
    return tips;
  }

  public void dispose(){
    if(!super.instantiate) return;

    if(sf != null) sf.cancel(true);
    service.shutdown();
  }

  void delayAttach(MusicInfo info, Node target){
    initIfNeeded();
    if(postCheck(info)) return;

    if(sf != null) sf.cancel(false);
    sf = service.schedule(new ShowTask(info, target), 200, TimeUnit.MILLISECONDS);
  }

  void delayDetach(){
    // 此处不可用 有可能delayAttach延时中 , popup还未到显示时间
    // if(!popup.isShowing()) return ;

    if(sf != null) sf.cancel(false);
    // sf=service.schedule(new HideTask(),150,TimeUnit.MILLISECONDS);
    popup.hide();
  }

  class ShowTask implements Runnable{
    MusicInfo musicInfo;
    Node node;

    public ShowTask(MusicInfo mi, Node node){
      this.musicInfo = mi;
      this.node = node;
    }

    public void run(){
      Platform.runLater(new Runnable(){
        public void run(){
          attach(musicInfo, node);
        }
      });
    }

  }

  // class HideTask implements Runnable{
  // public void run(){
  // Platform.runLater(new Runnable(){
  // public void run() {
  // popup.hide();
  // }
  // });
  // }
  // }

}