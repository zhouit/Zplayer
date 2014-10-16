package org.zhc.zplayer;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.zhc.zplayer.lyric.LyricContainer;
import org.zhc.zplayer.resp.MusicInfo;
import org.zhc.zplayer.search.MusicSearcher;
import org.zhc.zplayer.utils.ResourceManager;
import org.zhc.zplayer.utils.StringUtils;

public class Zplayer extends Application{
  TopContainer top;
  Label play_list, net_collect, broadcast;
  PlayAccordion playAccordion;
  VBox root;
  TitledPane collect;

  TextField search;
  StackPane musicView;

  public void start(Stage pstage) throws Exception{
    ViewsContext.initStage(pstage);

    pstage.initStyle(StageStyle.TRANSPARENT);
    pstage.setResizable(false);
    pstage.setTitle("ZPlayer");
    pstage.getIcons().add(ResourceManager.loadClasspathImage("icon.png"));

    Rectangle clip = new Rectangle(300, 635);
    clip.setArcHeight(10.0);
    clip.setArcWidth(10.0);
    root = VBoxBuilder.create().id("bg").prefHeight(635).prefWidth(300).clip(clip)
        .stylesheets(ResourceManager.getResourceUrl("/org/zhc/zplayer/index.css")).build();

    loadTop();
    loadCenter();
    loadBottom();
    TrayManager.getTrayManager().initTray();

    PlayAccordion.playIndex.addListener(new ChangeListener<MusicInfo>(){
      public void changed(ObservableValue<? extends MusicInfo> values, MusicInfo old, MusicInfo newv){
        playMusic(newv);
        top.getControls().setMusic(newv);
        playAccordion.updateListenView(old, newv);
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

  void loadTop(){
    top = new TopContainer();

    root.getChildren().add(top.getView());
  }

  void loadCenter(){
    VBox container = new VBox();

    HBox tabs = HBoxBuilder.create().padding(new Insets(0.0)).build();

    play_list = LabelBuilder.create().textFill(Color.WHITESMOKE).alignment(Pos.CENTER)
        .onMouseClicked(new EventHandler<MouseEvent>(){
          public void handle(MouseEvent event){
            if("play_list".equals(play_list.getId())){
              return;
            }

            play_list.setId("play_list");
            net_collect.setId("net_collect");
            VBox center = (VBox) root.getChildren().get(1);
            center.getChildren().remove(1);
            center.getChildren().add(musicView);
          }
        }).text("   播放列表").id("play_list").prefHeight(32.5).prefWidth(282 / 3 - 10).build();
    net_collect = LabelBuilder.create().textFill(Color.WHITESMOKE).alignment(Pos.CENTER)
        .text("网络收藏").id("net_collect").onMouseClicked(new EventHandler<MouseEvent>(){
          public void handle(MouseEvent event){
            if(!"play_list".equals(play_list.getId())){
              return;
            }

            initCollectIfNeeded();
            play_list.setId("play_list_lfocus");
            net_collect.setId("net_collect_focus");
            VBox center = (VBox) root.getChildren().get(1);
            center.getChildren().remove(1);
            center.getChildren().add(collect);
          }
        }).graphic(ResourceManager.getViewOfClasspath("favorite.png"))
        .contentDisplay(ContentDisplay.RIGHT).prefHeight(32.5).prefWidth(276 / 3 + 10).build();
    broadcast = LabelBuilder.create().textFill(Color.WHITESMOKE).alignment(Pos.CENTER).text("音乐电台")
        .id("broadcast").prefHeight(32.5).prefWidth(282 / 3).build();

    tabs.getChildren().addAll(play_list,
        LabelBuilder.create().id("tab_left_btn").prefHeight(32.5).prefWidth(72 / 4).build(),
        net_collect, broadcast);

    playAccordion = new PlayAccordion();
    ViewsContext.bindComponent(ViewsContext.PLAY_ACCORDION, playAccordion);
    musicView = StackPaneBuilder
        .create()
        .style("-fx-background-color:red")
        .children(
            playAccordion.getAccordion(),
            search = TextFieldBuilder.create().id("search").promptText("输入中文查找").visible(false)
                .onKeyPressed(new EventHandler<KeyEvent>(){
                  public void handle(KeyEvent event){
                    if(event.getCode() == KeyCode.ENTER){
                      if(StringUtils.trimWhitespace(search.getText()).equals("")){
                        playAccordion.backView();
                        return;
                      }

                      playAccordion.filterView(MusicSearcher.getInstance().search(
                          search.getText().trim()));
                    }
                  }
                }).prefHeight(30).build()).build();

    StackPane.setAlignment(search, Pos.BOTTOM_CENTER);
    // container.getChildren().addAll(tabs,playAccordion.getAccordion());
    container.getChildren().addAll(tabs, musicView);
    root.getChildren().add(container);
  }

  void loadBottom(){
    BottomContainer bc = new BottomContainer();

    root.getChildren().add(bc.getView());
  }

  void playMusic(MusicInfo music){
    Controls con = (Controls) top.getControls();
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

  void initCollectIfNeeded(){
    if(collect != null) return;

    collect = new TitledPane("  默认收藏", ResourceManager.getViewOfClasspath("collect_empty.png"));
    collect.setPrefHeight(445);
    collect.setAnimated(false);
    collect.setId("center_line");
    collect.setGraphic(UIBuilder.buildGroupBtn());
    collect.setContentDisplay(ContentDisplay.RIGHT);
    collect.setGraphicTextGap(170);
  }

  public static void main(String[] args){
    launch(args);
  }

}