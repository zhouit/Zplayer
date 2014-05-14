package org.zhc.zplayer;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransitionBuilder;
import javafx.animation.ScaleTransitionBuilder;
import javafx.animation.TimelineBuilder;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

import org.zhc.zplayer.lyric.LyricContainer;
import org.zhc.zplayer.search.MusicSearcher;
import org.zhc.zplayer.utils.ResourceManager;

/**
 * 系统托盘管理器
 * 
 * @author zhou
 *
 */
public class TrayManager implements ActionListener{
  private static TrayManager _INSTANCE = new TrayManager();
  TrayIcon trayIcon;

  private TrayManager(){
    Platform.setImplicitExit(!ResourceManager.isWindows());
  }

  public static TrayManager getTrayManager(){
    return _INSTANCE;
  }

  public void initTray(){
    if(!ResourceManager.isWindows()) return;

    // 任务栏图标菜单
    PopupMenu popup = new PopupMenu();
    MenuItem open = new MenuItem("打开/关闭");
    MenuItem quit = new MenuItem("退出");

    open.addActionListener(this);
    quit.addActionListener(this);

    popup.add(open);
    popup.add(quit);
    try{
      SystemTray tray = SystemTray.getSystemTray();
      trayIcon = new TrayIcon(ResourceManager.getTrayIcon(), "Zplayer", popup);
      trayIcon.addActionListener(this);
      trayIcon.setToolTip("Zplayer");
      tray.add(trayIcon);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void clear(){
    if(trayIcon == null){
      clearAndQuit();
    }else{
      ViewsContext.stage().hide();
    }
  }

  public void clearAndQuit(){
    if(AppConfig.dataChange){
      AppConfig.saveXmls(ViewsContext.getPlayAccordion().getMusicData());
      MusicSearcher.getInstance().cleanTrash();
    }

    AppConfig.saveSettings();

    MusicSearcher.getInstance().close();
    if(LyricContainer.lyric_init) LyricContainer.getLyricContainer().shutdown();
    DelayMusicTips.getMusicTips().dispose();

    Transition tran = ScaleTransitionBuilder.create().node(ViewsContext.stage().getScene().getRoot())
         .fromY(1.0).toY(0.0D).duration(Duration.seconds(0.3)).build();
    
    if(ViewsContext.player() != null && ViewsContext.player().getStatus() == Status.PLAYING){
      tran = ParallelTransitionBuilder.create()
          .children(tran,TimelineBuilder.create()
               .keyFrames(new KeyFrame(Duration.seconds(1.0), new KeyValue(ViewsContext.player().volumeProperty(), 0.0)))
               .build())
           .build();
    }

    tran.setOnFinished(new EventHandler<ActionEvent>(){
      public void handle(ActionEvent event){
        if(ViewsContext.player() != null) ViewsContext.player().stop();
        ViewsContext.stage().close();
        Platform.exit();
      }
    });

    tran.play();
  }

  public void actionPerformed(java.awt.event.ActionEvent e){
    Object source = e.getSource();
    if(source.getClass() == TrayIcon.class){
      Platform.runLater(new Runnable(){
        public void run(){
          if(!ViewsContext.stage().isShowing()) ViewsContext.stage().show();
        }
      });
      return;
    }

    java.awt.MenuItem item = (java.awt.MenuItem) source;
    if("退出".equals(item.getLabel())){
      SystemTray.getSystemTray().remove(trayIcon);
      clearAndQuit();
    }else if("打开/关闭".equals(item.getLabel())){
      Platform.runLater(new Runnable(){
        public void run(){
          if(!ViewsContext.stage().isShowing()){
            ViewsContext.stage().show();
          }else{
            ViewsContext.stage().hide();
          }
        }
      });
    }
  }

}
