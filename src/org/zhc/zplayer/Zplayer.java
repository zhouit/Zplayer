package org.zhc.zplayer;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.zhc.zplayer.lyric.LyricContainer;
import org.zhc.zplayer.resp.MusicInfo;
import org.zhc.zplayer.utils.ResourceManager;

public class Zplayer extends Application{

  public void start(Stage pstage) throws Exception{
    ViewsContext.initStage(pstage);
    
    preshow();

    Rectangle clip = new Rectangle(300, 635);
    clip.setArcHeight(10.0);
    clip.setArcWidth(10.0);
    VBox root = new VBox();
    root.setId("bg");
    root.setPrefSize(635, 300);
    root.setClip(clip);
    root.getStylesheets().add(ResourceManager.getResourceUrl("/org/zhc/zplayer/index.css"));
        
    final TopContainer top = new TopContainer();
    ViewsContext.bindComponent(ViewsContext.TOP, top);
    CenterContainer cc = new CenterContainer();
    BottomContainer bc = new BottomContainer();

    root.getChildren().addAll(top.getView(), cc.getView(), bc.getView());

    TrayManager.getTrayManager().initTray();

    PlayAccordion.playIndex.addListener(new ChangeListener<MusicInfo>(){
      public void changed(ObservableValue<? extends MusicInfo> values, MusicInfo old, MusicInfo newv){
        playMusic(newv);
        top.getControls().setMusic(newv);
        PlayAccordion pa = (PlayAccordion) ViewsContext.getComponent(ViewsContext.PLAY_ACCORDION);
        pa.updateListenView(old, newv);
      }
    });

    Scene scene = new Scene(root);
    scene.setFill(Color.TRANSPARENT);
    pstage.setScene(scene);

    // 启用移动stage
    StageDragListener listener = new StageDragListener(ViewsContext.stage());
    listener.enableDrag(top.getView());
    // 启用界面悬浮
    listener.enableHange();
    pstage.show();
  }
  
  void preshow(){
    ViewsContext.stage().initStyle(StageStyle.TRANSPARENT);
    ViewsContext.stage().setResizable(false);
    ViewsContext.stage().setTitle("ZPlayer");
    ViewsContext.stage().getIcons().add(ResourceManager.loadClasspathImage("icon.png"));
  }

  void playMusic(MusicInfo music){
    TopContainer tc = (TopContainer) ViewsContext.getComponent(ViewsContext.TOP);
    Controls con = (Controls) tc.getControls();
    if(ViewsContext.player() != null){
      ViewsContext.player().stop();
      ViewsContext.player().currentTimeProperty().removeListener(con);
      ViewsContext.player().dispose();
      ViewsContext.setPlayer(null);
    }

    Media media = new Media(music.url);
    ViewsContext.setPlayer(new MediaPlayer(media));

    ViewsContext.player().setOnReady(new Runnable(){
      public void run(){
        LyricContainer.lyric_init = true;
        LyricContainer.getLyricContainer().showLyric();
        ViewsContext.player().play();
      }
    });

    ViewsContext.player().setVolume(con.getVolume());
    ViewsContext.player().setAutoPlay(false);
  }

  public static void main(String[] args){
    launch(args);
  }

}