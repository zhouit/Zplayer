package org.zplayer;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class ViewsContext{
  public static final int TOP = 1;
  public static final int PLAY_ACCORDION = 2;
  public static final int SEARCH_INPUT = 3;
  
  private static final Map<Integer, Object> components = new HashMap<>();
  private static Stage stage;
  public static MediaPlayer player;

  public static MediaPlayer player(){
    return player;
  }

  public static void setPlayer(MediaPlayer play){
    player = play;
  }
  
  public static void initStage(Stage stage){
    ViewsContext.stage=stage;
  }
  
  public static Stage stage(){
    return stage;
  }

  public static void bindComponent(int key, Object value){
    components.put(key, value);
  }

  public static Object getComponent(int key){
    return components.get(key);
  }

}
