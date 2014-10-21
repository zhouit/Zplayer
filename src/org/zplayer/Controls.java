package org.zplayer;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import org.zplayer.controls.Zlider;
import org.zplayer.controls.Zlider.DragingHandler;
import org.zplayer.lyric.LyricContainer;
import org.zplayer.utils.ImageUtils;
import org.zplayer.utils.ResourceManager;
import org.zplayer.utils.StringUtils;

public class Controls extends AbstractView implements EventHandler<MouseEvent>{
  static Image[] vols = ImageUtils.split(ResourceManager.loadClasspathImage("sound_state.png"), 4);
  private TranslateTransition tt;
  Label name, play_pause, total_time, current_time, prev, next;
  Zlider time, sound;

  public Controls(){
    super();

    tt = new TranslateTransition(Duration.seconds(5.0D), name);
    tt.setInterpolator(Interpolator.LINEAR);
  }

  protected void initView(){
    Group root = new Group();
    name = LabelBuilder.create().textFill(Color.WHITE).layoutX(7).layoutY(3).build();
    Rectangle clip=new Rectangle(180, 15);
    name.setClip(clip);
    root.getChildren().add(name);

    current_time = LabelBuilder.create().textFill(Color.WHITE).text("00:00").layoutX(200)
        .layoutY(3.0).build();
    root.getChildren().add(current_time);

    total_time = LabelBuilder.create().textFill(Color.WHITE).text("/00:00").layoutX(235)
        .layoutY(3.0).build();
    root.getChildren().add(total_time);

    time = new Zlider(275, 6, 1, ResourceManager.loadClasspathImage("time_thumb.png"));
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

    final ImageView volume = new ImageView(vols[2]);
    volume.setFitHeight(17);
    volume.setFitWidth(72 / 4);
    volume.setLayoutX(200);
    volume.setLayoutY(78);
    root.getChildren().add(volume);

    sound = new Zlider(61, 12, 0, ImageUtils.split(
        ResourceManager.loadClasspathImage("sound_thumb.png"), 4)[0]);
    sound.setId("sound");
    sound.setLayoutX(225);
    sound.setLayoutY(78);
    sound.setProgress(0.5D);
    sound.setDragingHandler(new DragingHandler(){
      @Override
      public void dragHandler(double progress){
        if(progress == 0){
          volume.setImage(vols[0]);
        }else if(progress <= 0.35D){
          volume.setImage(vols[1]);
        }else if(progress <= 0.65D){
          volume.setImage(vols[2]);
        }else{
          volume.setImage(vols[3]);
        }

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

    ViewsContext.player().currentTimeProperty().addListener(new InvalidationListener(){
      @Override
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
    });
    ViewsContext.player().setOnEndOfMedia(new Runnable(){
      @Override
      public void run(){
        PlayAccordion pa = (PlayAccordion) ViewsContext.getComponent(ViewsContext.PLAY_ACCORDION);
        pa.playNextMusic();
      }
    });
  }

  public void handle(MouseEvent event){
    event.consume();
    Label target = (Label) event.getSource();
    if(ViewsContext.player() == null){
      PlayAccordion pa = (PlayAccordion) ViewsContext.getComponent(ViewsContext.PLAY_ACCORDION);
      pa.playMusic();
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
      PlayAccordion pa = (PlayAccordion) ViewsContext.getComponent(ViewsContext.PLAY_ACCORDION);
      pa.playPrevMusic();
    }else if(target == next){
      PlayAccordion pa = (PlayAccordion) ViewsContext.getComponent(ViewsContext.PLAY_ACCORDION);
      pa.playNextMusic();
    }
  }

}
