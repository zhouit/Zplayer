package org.zhc.zplayer;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.MediaPlayer;

public class ViewsContext{
  public static final String PLAY_ACCORDION = "PlayAccordion";
  private static final Map<String, Object> components = new HashMap<String, Object>();
  public static MediaPlayer player;

  public static MediaPlayer player(){
    return player;
  }

  public static void setPlayer(MediaPlayer play){
    player = play;
  }

  public static void bindComponent(String key, Object value){
    components.put(key, value);
  }

  public static Object getComponent(String key){
    return components.get(key);
  }

  public static PlayAccordion getPlayAccordion(){
    return (PlayAccordion) components.get(ViewsContext.PLAY_ACCORDION);
  }

}
