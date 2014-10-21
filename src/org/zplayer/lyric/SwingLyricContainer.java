package org.zplayer.lyric;

import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
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

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.zplayer.PlayAccordion;
import org.zplayer.ViewsContext;
import org.zplayer.resp.AppConfig;
import org.zplayer.utils.Threads;

public class SwingLyricContainer extends LyricContainer{
  JDialog jDialog;
  Text lyrics;

  public SwingLyricContainer(){
    super();

    this.jDialog = new JDialog();
    lyrics = new Text("Zplayer 让我们做得更好!");
    lyrics.setFont(Font.font("KaiTi", FontWeight.BLACK, 40));
    lyrics.setFill(LEFT_COLOR);
    DropShadow shadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.BLACK, 0.0, 0.1, 1.0, 1.0);
    lyrics.setEffect(shadow);
    final Scene scene = new Scene(HBoxBuilder.create().padding(new Insets(5)).children(lyrics)
        .build(), 750, 55);
    scene.setFill(Color.TRANSPARENT);

    final JFXPanel jfxPanel = new JFXPanel();
    Platform.runLater(new Runnable(){
      public void run(){
        jfxPanel.setScene(scene);
      }
    });

    LyricDragListener ldl = new LyricDragListener(jfxPanel);
    jfxPanel.addMouseListener(ldl);
    jfxPanel.addMouseMotionListener(ldl);

    jDialog.setSize(750, 55);
    jDialog.add(jfxPanel);
    jDialog.setLocation(300, 600);
    jDialog.setUndecorated(true);
    jDialog.setAlwaysOnTop(true);
    jDialog.setBackground(new java.awt.Color(0, 0, 0, 0));
    jDialog.setForeground(jDialog.getBackground());
    jDialog.setVisible(true);
    jDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  public void showLyric(){
    this.hasLyric = false;

    setLyric(new File(AppConfig.getConfig(AppConfig.LYRIC_DIR), PlayAccordion.playIndex.get()
        .getFullname() + ".lrc"));

    lyrics.setTranslateX(0);
    if(!shutdown) return;

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
        System.out.println("do play");
        if(!isPlay){ // 当暂停时
          lyrics.setText("Zplayer 让我们做得更好!");
          System.out.println("do play pause");
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

      lyrics.setStyle("-fx-fill:linear-gradient(to bottom,rgb(238, 254, 218) 0%,"
          + "rgb(153, 254, 17) 50%,rgb(232, 254, 3) 100%);");

      lyrics.setFill(new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, new Stop[]{
          new Stop(0.0, LEFT_COLOR), new Stop(f, LEFT_COLOR), new Stop(f + 0.01, RIGHT_COLOR),
          new Stop(1.0, RIGHT_COLOR) }));

      double translateX = lyrics.getTranslateX();
      double visitLength = st.getContentLength() * f + translateX;
      if(visitLength / 750 >= 0.70D){
        lyrics.setTranslateX(translateX - st.getContentLength() * refreshInterval / st.getTime());
      }

      lyrics.setText(st.content);
    }

    jDialog.dispose();
  }

  @Override
  public void shutdown(){
    // 当未播放过歌曲时
    if(PlayAccordion.playIndex.get().toString() == null){
      jDialog.dispose();
    }else{
      super.shutdown();
    }
  }

  static class LyricDragListener extends MouseAdapter{
    JComponent target;
    Point start_drag;
    Point start_loc;

    public LyricDragListener(JComponent target){
      this.target = target;
    }

    public Window getFrame(Container target){
      if(target instanceof Window){
        return (Window) target;
      }

      return getFrame(target.getParent());
    }

    Point getScreenLocation(MouseEvent e){
      Point cursor = e.getPoint();
      Point target_location = this.target.getLocationOnScreen();
      return new Point((int) (target_location.getX() + cursor.getX()),
          (int) (target_location.getY() + cursor.getY()));
    }

    public void mousePressed(MouseEvent e){
      this.start_drag = this.getScreenLocation(e);
      this.start_loc = this.getFrame(this.target).getLocation();
    }

    public void mouseDragged(MouseEvent e){
      Point current = this.getScreenLocation(e);
      Point offset = new Point((int) current.getX() - (int) start_drag.getX(), (int) current.getY()
          - (int) start_drag.getY());
      Window frame = this.getFrame(target);
      Point new_location = new Point((int) (this.start_loc.getX() + offset.getX()),
          (int) (this.start_loc.getY() + offset.getY()));
      frame.setLocation(new_location);
    }
  }

}
