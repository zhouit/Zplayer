package org.zplayer.lyric;

import java.io.File;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.zplayer.PlayAccordion;
import org.zplayer.StageDragListener;
import org.zplayer.ViewsContext;
import org.zplayer.resp.AppConfig;
import org.zplayer.utils.Threads;

public class JavafxLyricContainer extends LyricContainer{
  Stage lyricStage;
  Text lyrics;

  public JavafxLyricContainer(){
    super();

    lyricStage = new Stage(StageStyle.TRANSPARENT);
    lyricStage.setX(300);
    lyricStage.setY(600);
    lyricStage.initOwner(ViewsContext.stage());
    lyricStage.initModality(Modality.NONE);
    lyricStage.setResizable(false);

    lyrics = new Text("Zplayer 让我们做得更好!");
    lyrics.setFont(Font.font("KaiTi", FontWeight.BLACK, 40));
    lyrics.setFill(LEFT_COLOR);
    DropShadow shadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.BLACK, 0.0, 0.1, 1.0, 1.0);
    lyrics.setEffect(shadow);
    Scene scene = new Scene(HBoxBuilder.create().padding(new Insets(5)).children(lyrics).build(),
        750, 55);
    scene.setFill(Color.TRANSPARENT);
    lyricStage.setScene(scene);
    new StageDragListener(lyricStage).enableDrag(scene);
  }

  public void showLyric(){
    this.hasLyric = false;

    setLyric(new File(AppConfig.getConfig(AppConfig.LYRIC_DIR), PlayAccordion.playIndex.get()
        .getFullname() + ".lrc"));

    lyrics.setTranslateX(0);
    if(!shutdown) return;

    this.lyricStage.show();
    thread = new Thread(this);
    thread.start();
    this.isPlay = true;
    this.shutdown = false;
  }

  public void run(){
    while(!shutdown){
      Threads.sleeps(refreshInterval);

      if(!hasLyric){
        lyrics.setText(PlayAccordion.playIndex.get().getFullname());
        continue;
      }

      while(!isPlay && !shutdown){
        if(!isPlay){ // 当暂停时
          lyrics.setText("Zplayer 让我们做得更好!");
        }

        synchronized(lock){
          Threads.wait(lock);
        }
      }

      long currentTime = 0;
      if(ViewsContext.player() != null){
        currentTime = (long) ViewsContext.player().getCurrentTime().toMillis();
      }

      int index = getCurrentSentence(currentTime);
      if(index != lastIndex) lyrics.setTranslateX(0);
      lastIndex = index;

      Sentence st = sentences.get(index);
      double f = (double) ((double) (currentTime - st.fromTime) / (st.toTime - st.fromTime));
      if(f > 0.98) f = 0.98F;

      lyrics.setFill(new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, new Stop[]{
          new Stop(0.0, LEFT_COLOR), new Stop(f, LEFT_COLOR), new Stop(f + 0.01, RIGHT_COLOR),
          new Stop(1.0, RIGHT_COLOR) }));

      lyrics.setStyle("-fx-fill:linear-gradient(to bottom,rgb(238, 254, 218) 0%,"
          + "rgb(153, 254, 17) 50%,rgb(232, 254, 3) 100%);");

      double translateX = lyrics.getTranslateX();
      double visitLength = st.getContentLength() * f + translateX;
      if(visitLength / lyricStage.getWidth() >= 0.70D){
        lyrics.setTranslateX(translateX - st.getContentLength() * refreshInterval / st.getTime());
      }

      lyrics.setText(st.content);
    }
  }

}
