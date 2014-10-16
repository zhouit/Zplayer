package org.zhc.zplayer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import org.zhc.zplayer.controls.Zlider;
import org.zhc.zplayer.controls.Zlider.DragingHandler;
import org.zhc.zplayer.lyric.LyricContainer;
import org.zhc.zplayer.utils.ImageUtils;
import org.zhc.zplayer.utils.ResourceManager;
import org.zhc.zplayer.utils.StringUtils;

public class Controls extends AbstractView implements InvalidationListener,
    EventHandler<MouseEvent>, Runnable{
  Label name, play_pause, total_time, current_time, prev, next;
  Zlider time, sound;
  Line timeBar;

  public Controls(){
    super();
  }

  protected void initView(){
    Group root = new Group();
    name = LabelBuilder.create().textFill(Color.WHITE).layoutX(7).layoutY(3).build();
    root.getChildren().add(name);

    current_time = LabelBuilder.create().textFill(Color.WHITE).text("00:00").layoutX(200)
        .layoutY(3.0).build();
    root.getChildren().add(current_time);

    total_time = LabelBuilder.create().textFill(Color.WHITE).text("/00:00").layoutX(235)
        .layoutY(3.0).build();
    root.getChildren().add(total_time);

    time = new Zlider(275, 6,1, ResourceManager.loadClasspathImage("time_thumb.png"));
    time.setId("time");
    time.setLayoutX(8);
    time.setLayoutY(20);
    time.setDragingHandler(new DragingHandler(){
      @Override
      public void dragHandler(double progress){
        if(ViewsContext.player() == null) return;

        Duration seekTo = music.time.multiply(progress);
        if(ViewsContext.player().getStatus() == Status.STOPPED){
          ViewsContext.player().pause();
        }

        ViewsContext.player().seek(seekTo);
      }
    });
    root.getChildren().add(time);

    prev = LabelBuilder.create().prefHeight(45).prefWidth(168 / 4).id("prev").onMouseClicked(this)
        .layoutX(81).layoutY(50).build();
    root.getChildren().add(prev);

    play_pause = LabelBuilder.create().prefHeight(45).prefWidth(336 / 8).id("play")
        .onMouseClicked(this).layoutX(123).layoutY(50).build();
    root.getChildren().add(play_pause);

    next = LabelBuilder.create().prefHeight(45).prefWidth(168 / 4).id("next").onMouseClicked(this)
        .layoutX(165).layoutY(50).build();
    root.getChildren().add(next);

    Button skin = ButtonBuilder.create().id("skin_btn").layoutX(69).layoutY(78).build();
    root.getChildren().add(skin);

    Button lyrics = ButtonBuilder.create().id("lyric_btn").layoutX(5).layoutY(74).build();
    root.getChildren().add(lyrics);

    Label volume = LabelBuilder.create().id("volume_btn").prefWidth(72 / 4).prefHeight(17)
        .layoutX(205).layoutY(78).build();
    root.getChildren().add(volume);

    sound = new Zlider(61, 12,0, ImageUtils.split(
        ResourceManager.loadClasspathImage("sound_thumb.png"), 4)[0]);
    sound.setId("sound");
    sound.setLayoutX(225);
    sound.setLayoutY(78);
    sound.setProgress(0.5D);
    sound.setDragingHandler(new DragingHandler(){
      @Override
      public void dragHandler(double progress){
        if(ViewsContext.player() == null) return;

        ViewsContext.player().setVolume(progress);
      }
    });
    root.getChildren().add(sound);

    super.view = root;
  }

  public double getVolume(){
    return sound.getValue();
  }

  protected void updateView(){
    play_pause.setId("pause");
    name.setText(music.getFullname());
    total_time.setText("/" + music.formatDuration());

    sound.setProgress(ViewsContext.player().getVolume());

    ViewsContext.player().currentTimeProperty().addListener(this);
    ViewsContext.player().setOnEndOfMedia(this);
  }

  // 歌曲时间slider与同步
  public void invalidated(Observable observable){
    Duration duration = ViewsContext.player().getCurrentTime();
    current_time.setText(StringUtils.formatDuration(duration));

    if(music.time == null || duration == null){
      time.setProgress(0.0);
    }else{
      double value = duration.toMillis() / music.time.toMillis();
      time.setProgress(value);
    }
  }

  public void run(){
    ViewsContext.getPlayAccordion().playNextMusic();
  }

  public void handle(MouseEvent event){
    event.consume();
    Label target = (Label) event.getSource();
    if(ViewsContext.player() == null){
      ViewsContext.getPlayAccordion().playMusic();
      play_pause.setId("pause");
      return;
    }

    if(target == play_pause){
      if(ViewsContext.player().getStatus() == Status.PLAYING){
        ViewsContext.player().pause();
        LyricContainer.getLyricContainer().pause();
        play_pause.setId("play");
      }else if(ViewsContext.player().getStatus() == Status.PAUSED
          || ViewsContext.player().getStatus() == Status.STOPPED){
        ViewsContext.player().play();
        LyricContainer.getLyricContainer().play();
        play_pause.setId("pause");
      }
    }else if(target == prev){
      ViewsContext.getPlayAccordion().playPrevMusic();
    }else if(target == next){
      ViewsContext.getPlayAccordion().playNextMusic();
    }
  }

}
