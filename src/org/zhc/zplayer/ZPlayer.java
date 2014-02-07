package org.zhc.zplayer;

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransitionBuilder;
import javafx.animation.ScaleTransitionBuilder;
import javafx.animation.TimelineBuilder;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuBarBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.zhc.zplayer.lyric.LyricContainer;
import org.zhc.zplayer.search.MusicSearcher;
import org.zhc.zplayer.settings.Settings;
import org.zhc.zplayer.utils.ResourceManager;
import org.zhc.zplayer.utils.StringUtils;

public class ZPlayer extends Application{
  boolean lyricInstantiate = false; // 歌词容器是否实例化

  public static Stage stage;
  Stage about;
  StageDragListener listener;

  Label play_list, net_collect, broadcast;
  PlayAccordion playAccordion;
  VBox root;
  AbstractView controls;
  TitledPane collect;
  Settings settings;

  TextField search;
  StackPane musicView;

  public void start(Stage pstage) throws Exception{
    pstage.initStyle(StageStyle.TRANSPARENT);
    pstage.setResizable(false);
    stage = pstage;
    pstage.setTitle("ZPlayer");
    pstage.getIcons().add(ResourceManager.loadClasspathImage("icon.png"));

    Rectangle clip = new Rectangle(300, 635);
    clip.setArcHeight(10.0);
    clip.setArcWidth(10.0);
    root = VBoxBuilder.create().id("bg").prefHeight(635).prefWidth(300).clip(clip)
        .stylesheets(ResourceManager.getResourceUrl("/org/zhc/zplayer/index.css")).build();
    // 默认情况下，Fx运行时会在最后一个stage的close(或hide)后自动关闭，即自动调用Application.stop()
    // 除非通过Platform.setImplicitExit(false)取消这个默认行为。这样,即使所有Fx窗口关闭（或隐藏）,Fx运行时还在正常运行
    // 可以再次显示原来的窗口或打开新的窗口。
    Platform.setImplicitExit(!ResourceManager.isWindows());

    loadTop();
    loadCenter();
    loadBottom();
    loadTray();

    PlayAccordion.playIndex.addListener(new ChangeListener<MusicInfo>(){
      public void changed(ObservableValue<? extends MusicInfo> values, MusicInfo old, MusicInfo newv){
        playMusic(newv);
        controls.setMusic(newv);
        playAccordion.updateListenView(old, newv);
      }
    });

    Scene scene = new Scene(root);
    scene.setFill(Color.TRANSPARENT);
    pstage.setScene(scene);
    // 启用界面悬浮
    listener.enableHange();
    pstage.show();
  }

  void loadTop(){
    AnchorPane top = new AnchorPane();

    EventHandler<MouseEvent> windowHandler = new EventHandler<MouseEvent>(){
      public void handle(MouseEvent event){
        event.consume();
        Node label = (Node) event.getTarget();
        if(label.getId().equals("WinClose")){
          if(ResourceManager.isWindows()){
            stage.hide();
          }else{
            clearAndQuit();
          }
        }else if(label.getId().equals("WinZoom")){
          stage.setIconified(true);
        }
      }
    };

    Label close = LabelBuilder.create().prefHeight(26).prefWidth(33).onMouseClicked(windowHandler)
        .id("WinClose").build();
    top.getChildren().add(close);
    AnchorPane.setRightAnchor(close, 0.0);
    AnchorPane.setTopAnchor(close, 0.0);

    Label zoom = LabelBuilder.create().prefHeight(26).prefWidth(33).onMouseClicked(windowHandler)
        .id("WinZoom").build();
    top.getChildren().add(zoom);
    AnchorPane.setRightAnchor(zoom, 32.0);
    AnchorPane.setTopAnchor(zoom, 0.0);

    MenuBar menubar = MenuBarBuilder.create().prefHeight(26).prefWidth(87).id("MenuBar").build();

    menubar.getMenus().add(UIBuilder.buildMenu(menuHandler()));
    top.getChildren().add(menubar);
    AnchorPane.setLeftAnchor(menubar, 5.0);
    AnchorPane.setTopAnchor(menubar, 0.0);

    controls = new Controls();
    Node cview = controls.getView();
    top.getChildren().add(cview);
    AnchorPane.setLeftAnchor(cview, 10.0);
    AnchorPane.setTopAnchor(cview, 32.0);

    // 启用移动stage
    listener = new StageDragListener(stage);
    listener.enableDrag(top);

    root.getChildren().add(top);
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
    Label playAdd = LabelBuilder.create().id("play_add").prefHeight(24).prefWidth(264 / 4).build();

    Label playDel = LabelBuilder.create().id("play_del").prefHeight(24).prefWidth(264 / 4).build();

    Label playModel = LabelBuilder.create().id("play_model").prefHeight(24).prefWidth(264 / 4)
        .build();

    Label playSearch = LabelBuilder.create().id("play_search").prefHeight(24).prefWidth(264 / 4)
        .onMouseClicked(new EventHandler<MouseEvent>(){
          public void handle(MouseEvent event){
            event.consume();
            if(search.isVisible()){
              search.setVisible(false);
              search.setText("");
              playAccordion.backView();
            }else{
              search.setVisible(true);
              MusicSearcher.getInstance().prepare();
            }
            // MusicContainer mc=playAccordion.listmc.get(0);
            // playAccordion.getAccordion().getPanes().remove(mc.getView());
            // playAccordion.listmc.remove(mc);
            // mc.clear();
          }
        }).build();
    // fade=FadeTransitionBuilder.create()
    // .fromValue(0.0).toValue(1.0)
    // .node(playSearch).duration(Duration.seconds(1.0))
    // .build();

    HBox bottom = HBoxBuilder.create().padding(new Insets(2, 20, 0, 20))
        .children(playAdd, playDel, playModel, playSearch).build();

    root.getChildren().add(bottom);
  }

  void playMusic(MusicInfo music){
    Controls con = (Controls) controls;
    if(ViewsContext.player() != null){
      ViewsContext.player().stop();
      ViewsContext.player().currentTimeProperty().removeListener(con);
      ViewsContext.player().volumeProperty()
          .unbindBidirectional(con.vslider.valueProperty());
      ViewsContext.setPlayer(null);
    }

    Media media = new Media(music.url);
    ViewsContext.setPlayer(new MediaPlayer(media));

    ViewsContext.player().setOnReady(new Runnable(){
      public void run(){
        lyricInstantiate = true;
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

  void clearAndQuit(){
    // Transition tran=FadeTransitionBuilder.create()
    // .node(root).fromValue(1.0).toValue(0.0)
    // .duration(Duration.seconds(1.0))
    // .build();
    if(AppConfig.dataChange){
      AppConfig.saveXmls(playAccordion.getMusicData());
      MusicSearcher.getInstance().cleanTrash();
    }

    AppConfig.saveSettings();

    MusicSearcher.getInstance().close();
    if(lyricInstantiate) LyricContainer.getLyricContainer().shutdown();
    DelayMusicTips.getMusicTips().dispose();

    Transition tran = ScaleTransitionBuilder.create().node(root).fromY(1.0).toY(0.0D)
        .duration(Duration.seconds(0.3)).build();
    if(ViewsContext.player() != null
        && ViewsContext.player().getStatus() == Status.PLAYING){
      tran = ParallelTransitionBuilder
          .create()
          .children(
              tran,
              TimelineBuilder
                  .create()
                  .keyFrames(
                      new KeyFrame(Duration.seconds(1.0), new KeyValue(ViewsContext.player()
                          .volumeProperty(), 0.0))).build()).build();
    }

    tran.setOnFinished(new EventHandler<ActionEvent>(){
      public void handle(ActionEvent event){
        if(ViewsContext.player() != null) ViewsContext.player().stop();
        stage.close();
        Platform.exit();
      }
    });

    tran.play();
  }

  private EventHandler<ActionEvent> menuHandler(){
    return new EventHandler<ActionEvent>(){
      public void handle(ActionEvent event){
        MenuItem item = (MenuItem) event.getSource();
        String itemText = StringUtils.trimWhitespace(item.getText());
        if("意见反馈".equals(itemText)){
          UIBuilder.mailToOrBrowse(true);
        }else if("关于Zplayer".equals(itemText)){
          loadAbout();
          about.show();
        }else if("选项设置".equals(itemText)){
          if(settings == null) settings = new Settings();
          settings.show();
        }
      }
    };
  }

  void loadAbout(){
    if(about != null) return;

    about = new Stage();
    about.initOwner(ZPlayer.stage);
    about.initStyle(StageStyle.TRANSPARENT);
    about.initModality(Modality.APPLICATION_MODAL);
    Label title = LabelBuilder.create().translateY(5).text("关于Zplayer").textFill(Color.WHITE)
        .build();
    EventHandler<MouseEvent> aboutHandler = new EventHandler<MouseEvent>(){
      public void handle(MouseEvent event){
        Node label = (Node) event.getTarget();
        event.consume();
        if("WinClose".equals(label.getId())){
          about.close();
        }else{
          UIBuilder.mailToOrBrowse(false);
        }
      }
    };

    Label close = LabelBuilder.create().onMouseClicked(aboutHandler).prefHeight(26).prefWidth(33)
        .id("WinClose").build();
    HBox head = HBoxBuilder.create().id("head").padding(new Insets(0, 0, 0, 5)).spacing(243)
        .children(title, close).build();

    Text describe = new Text("  长江大学 软工一班 周浩成 版权所有\n\n" + "本程序所有图片资源均来自于酷狗播放器\n");
    describe.setFont(Font.font("KaiTi", FontWeight.BOLD, 16));

    Hyperlink http = new Hyperlink("www.javafx.com");
    http.setTranslateY(-1.0);
    http.setOnMouseClicked(aboutHandler);

    Rectangle clip = new Rectangle(350, 290);
    clip.setArcHeight(10.0);
    clip.setArcWidth(10.0);
    VBox aboutRoot = VBoxBuilder
        .create()
        .spacing(15.0)
        .alignment(Pos.TOP_CENTER)
        .prefHeight(290)
        .prefWidth(350)
        .children(
            head,
            describe,
            ResourceManager.getViewOfClasspath("javafx.jpg"),
            HBoxBuilder.create().padding(new Insets(0, 0, 0, 65))
                .children(new Label("官方网站:"), http).build()).clip(clip).id("bg")
        .stylesheets(ResourceManager.getResourceUrl("/org/zhc/zplayer/about.css")).build();

    StageDragListener sdl = new StageDragListener(about);
    sdl.enableDrag(head);
    Scene scene = new Scene(aboutRoot);
    about.setScene(scene);
  }

  public static void main(String[] args){
    if(ResourceManager.isPreInstance()) return;

    launch(args);
  }

  TrayIcon trayIcon;

  void loadTray(){
    if(!ResourceManager.isWindows()) return;

    // 任务栏图标菜单
    PopupMenu popupMenu = new PopupMenu();
    java.awt.MenuItem openItem = new java.awt.MenuItem("打开/关闭");
    java.awt.MenuItem quitItem = new java.awt.MenuItem("退出");

    ActionListener acl = new ActionListener(){
      public void actionPerformed(java.awt.event.ActionEvent e){
        Object source = e.getSource();
        if(source.getClass() == TrayIcon.class){
          Platform.runLater(new Runnable(){
            public void run(){
              if(!stage.isShowing()) stage.show();
            }
          });
          return;
        }

        java.awt.MenuItem item = (java.awt.MenuItem) source;
        if("退出".equals(item.getLabel())){
          SystemTray.getSystemTray().remove(trayIcon);
          clearAndQuit();
        }else if("打开/关闭".equals(item.getLabel())){
          Platform.runLater(new Runnable(){
            public void run(){
              if(!stage.isShowing()){
                stage.show();
              }else{
                stage.hide();
              }
            }
          });
        }
      }
    };

    openItem.addActionListener(acl);
    quitItem.addActionListener(acl);

    popupMenu.add(openItem);
    popupMenu.add(quitItem);
    try{
      SystemTray tray = SystemTray.getSystemTray();
      trayIcon = new TrayIcon(ResourceManager.getTrayIcon(), "Zplayer", popupMenu);
      trayIcon.addActionListener(acl);
      trayIcon.setToolTip("Zplayer");
      tray.add(trayIcon);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}