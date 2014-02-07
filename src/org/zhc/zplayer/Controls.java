package org.zhc.zplayer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import org.zhc.zplayer.lyric.LyricContainer;
import org.zhc.zplayer.utils.ResourceManager;
import org.zhc.zplayer.utils.StringUtils;

public class Controls extends AbstractView implements InvalidationListener,
              EventHandler<MouseEvent>,Runnable{
  Label name,play_pause,total_time,current_time,prev,next;
  Slider time_slider,vslider;
  Line timeBar;
	
  public Controls(){
	super();
   }
  
  protected void initView(){
	Group root=new Group();
	name=LabelBuilder.create()
	   .textFill(Color.WHITE)
	   .layoutX(7).layoutY(3).build();
	root.getChildren().add(name);
	
	current_time=LabelBuilder.create()
	  .textFill(Color.WHITE)
	  .text("00:00").layoutX(200).layoutY(3.0)
	  .build();
	root.getChildren().add(current_time);
	
	total_time=LabelBuilder.create()
	  .textFill(Color.WHITE)
	  .text("/00:00").layoutX(235).layoutY(3.0)
	  .build();
	root.getChildren().add(total_time);
	
	time_slider=SliderBuilder.create()
	  .orientation(Orientation.HORIZONTAL)
	  .prefWidth(275)
	  .id("time").max(1.0).min(0.0)
	  .layoutX(8).layoutY(20)
	  .build();
	time_slider.valueChangingProperty().addListener(new PositionListener());
	root.getChildren().add(time_slider);
	
	prev=LabelBuilder.create()
	  .prefHeight(45).prefWidth(168/4)
	  .id("prev").onMouseClicked(this)
	  .layoutX(81).layoutY(50)
	  .build();
	root.getChildren().add(prev);
	
	play_pause=LabelBuilder.create()
	  .prefHeight(45).prefWidth(336/8)
	  .id("play").onMouseClicked(this)
	  .layoutX(123).layoutY(50)
	  .build();
	root.getChildren().add(play_pause);
	
	next=LabelBuilder.create()
	  .prefHeight(45).prefWidth(168/4)
	  .id("next").onMouseClicked(this)
	  .layoutX(165).layoutY(50)
	  .build();
	root.getChildren().add(next);
	
	Button skin=ButtonBuilder.create()
		.id("skin_btn")
		.layoutX(69).layoutY(78)
		.build();
	root.getChildren().add(skin);
	
	Button lyrics=ButtonBuilder.create()
	   .id("lyric_btn")
	   .layoutX(5).layoutY(74)
	   .build();
	root.getChildren().add(lyrics);	
	
	Label volume=LabelBuilder.create()
	  .id("volume_btn")
	  .prefWidth(72/4).prefHeight(17)
	  .layoutX(205).layoutY(80)
	  .build();
	root.getChildren().add(volume);
	
	ImageView sound_bg=ImageViewBuilder.create()
		.image(ResourceManager.loadClasspathImage("vslider.png"))
		.layoutX(230).layoutY(83)
		.build();
	root.getChildren().add(sound_bg);
	
	vslider=SliderBuilder.create()
		.orientation(Orientation.HORIZONTAL)
		.prefWidth(61).id("vslider")
		.max(1.0).value(0.5)
		.layoutX(230).layoutY(80)
		.build();
	
    root.getChildren().add(vslider);
    
	super.view=root;
   }
  
  public double getVolume(){
	return vslider.getValue();  
   }
  
  protected void updateView(){
	play_pause.setId("pause");
	name.setText(music.getFullname());
	total_time.setText("/"+music.formatDuration());
	
    vslider.setValue(ViewsContext.player().getVolume());
	vslider.valueProperty().bindBidirectional(ViewsContext.player().volumeProperty());
    
	ViewsContext.player().currentTimeProperty().addListener(this);
	ViewsContext.player().setOnEndOfMedia(this);
   }
  
  
  //歌曲时间slider与同步
  public void invalidated(Observable observable){
	 Duration duration=ViewsContext.player().getCurrentTime();
	 current_time.setText(StringUtils.formatDuration(duration));
	 if(time_slider.isValueChanging()) return ;
	 
	 if(music.time==null||duration==null){
	    time_slider.setValue(0.0);
	 }else{
		double value=duration.toMillis()/music.time.toMillis();
	    time_slider.setValue(value);
	 }
   }
  
  public void run(){
	 ViewsContext.getPlayAccordion().playNextMusic();
  }
  
  public void handle(MouseEvent event){
	event.consume();
	Label target=(Label)event.getSource();
	if(ViewsContext.player()==null){
	  ViewsContext.getPlayAccordion().playMusic();
	  play_pause.setId("pause");
	  return ;
	 }
	
	if(target==play_pause){
	  if(ViewsContext.player().getStatus()==Status.PLAYING){
		  ViewsContext.player().pause();
		 LyricContainer.getLyricContainer().pause();
         play_pause.setId("play");
	  }else if(ViewsContext.player().getStatus()==Status.PAUSED||
			 ViewsContext.player().getStatus()==Status.STOPPED){
		 ViewsContext.player().play();
		 LyricContainer.getLyricContainer().play();
		 play_pause.setId("pause");
	   }
	}else if(target==prev){
       ViewsContext.getPlayAccordion().playPrevMusic();
	}else if(target==next){
	   ViewsContext.getPlayAccordion().playNextMusic();
	}
  }
  
  private class PositionListener implements ChangeListener<Boolean>{
  
    public void changed(ObservableValue<? extends Boolean> values, Boolean old, Boolean newv){
      if(ViewsContext.player()==null) return ;
      
	  if(old&&!newv){
	    double svalue=time_slider.getValue();
	    Duration seekTo=music.time.multiply(svalue);
	    if(ViewsContext.player().getStatus()==Status.STOPPED){
	    	ViewsContext.player().pause();
	     }
	  
	    ViewsContext.player().seek(seekTo);
	   }
     }
  }

}
