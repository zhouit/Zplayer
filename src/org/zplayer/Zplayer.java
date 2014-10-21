package org.zplayer;

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

import org.zplayer.lyric.LyricContainer;
import org.zplayer.resp.MusicInfo;
import org.zplayer.utils.ResourceManager;

public class Zplayer extends Application{
  TopContainer top;

  public void start(Stage pstage) throws Exception{
    ViewsContext.initStage(pstage);

    pstage.initStyle(StageStyle.TRANSPARENT);
    pstage.setResizable(false);
    pstage.setTitle("ZPlayer");
    pstage.getIcons().add(ResourceManager.loadClasspathImage("icon.png"));

    Rectangle clip = new Rectangle(300, 635);
    clip.setArcHeight(10.0);
    clip.setArcWidth(10.0);
    VBox root = new VBox();
    root.setId("bg");
    root.setPrefSize(300, 635);
    root.getStylesheets().add(ResourceManager.getResourceUrl("/org/zplayer/index.css"));

    top = new TopContainer();
    root.getChildren().add(top.getView());
    CenterContainer cc = new CenterContainer();
    root.getChildren().add(cc.getView());
    BottomContainer bc = new BottomContainer();

    root.getChildren().add(bc.getView());

    TrayManager.getTrayManager().initTray();

    PlayAccordion.playIndex.addListener(new ChangeListener<MusicInfo>(){
      public void changed(ObservableValue<? extends MusicInfo> values, MusicInfo old, MusicInfo newv){
        playMusic(newv);
        top.getControls().setMusic(newv);
        PlayAccordion pa=(PlayAccordion)ViewsContext.getComponent(ViewsContext.PLAY_ACCORDION);
        pa.updateListenView(old, newv);
      }
    });

    Scene scene = new Scene(root);
    scene.setFill(Color.TRANSPARENT);
    pstage.setScene(scene);

    // 启用移动stage
    StageDragListener listener = new StageDragListener(ViewsContext.stage());
    listener.enableDrag(top.getView());
    pstage.show();
  }

  void playMusic(MusicInfo music){
    Controls con = (Controls) top.getControls();
    if(ViewsContext.player() != null){
      ViewsContext.player().stop();
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