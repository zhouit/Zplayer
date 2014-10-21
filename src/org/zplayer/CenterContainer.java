package org.zplayer;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.zplayer.search.MusicSearcher;
import org.zplayer.utils.ResourceManager;
import org.zplayer.utils.StringUtils;

public class CenterContainer implements EventHandler<MouseEvent>{
  private VBox view;
  private StackPane musicView;

  private TitledPane collect;
  Label playList, netCollect;

  public CenterContainer(){
    view = new VBox();

    HBox tabs = new HBox();
    playList = new Label("   播放列表");
    playList.setId("play_list");
    playList.setPrefSize(282 / 3 - 10, 32.5);
    playList.setTextFill(Color.WHITESMOKE);
    playList.setAlignment(Pos.CENTER);
    playList.setOnMouseClicked(this);
    tabs.getChildren().add(playList);

    Label leftBtn = new Label();
    leftBtn.setId("tab_left_btn");
    leftBtn.setPrefSize(72 / 4, 32.5);
    tabs.getChildren().add(leftBtn);

    netCollect = new Label("网络收藏", ResourceManager.getViewOfClasspath("favorite.png"));
    netCollect.setId("net_collect");
    netCollect.setContentDisplay(ContentDisplay.RIGHT);
    netCollect.setPrefSize(276 / 3 + 10, 32.5);
    netCollect.setTextFill(Color.WHITESMOKE);
    netCollect.setAlignment(Pos.CENTER);
    netCollect.setOnMouseClicked(this);
    tabs.getChildren().add(netCollect);

    Label broadcast = new Label("音乐电台");
    broadcast.setTextFill(Color.WHITESMOKE);
    broadcast.setAlignment(Pos.CENTER);
    broadcast.setId("broadcast");
    broadcast.setPrefSize(282 / 3, 32.5);
    tabs.getChildren().add(broadcast);

    initMusicView();

    view.getChildren().addAll(tabs, musicView);
  }

  public Node getView(){
    return view;
  }

  private void initMusicView(){
    final PlayAccordion playAccordion = new PlayAccordion();
    ViewsContext.bindComponent(ViewsContext.PLAY_ACCORDION, playAccordion);
    musicView = new StackPane();
    musicView.setStyle("-fx-background-color:red");
    musicView.getChildren().add(playAccordion.getAccordion());

    final TextField search = new TextField();
    search.setId("search");
    search.setPrefHeight(30);
    search.setPromptText("输入中文查找");
    search.setVisible(false);
    search.setOnKeyReleased(new EventHandler<KeyEvent>(){
      public void handle(KeyEvent event){
        if(event.getCode() == KeyCode.ENTER){
          if(StringUtils.trimWhitespace(search.getText()).equals("")){
            playAccordion.backView();
            return;
          }

          playAccordion.filterView(MusicSearcher.getInstance().search(search.getText().trim()));
        }
      }
    });
    musicView.getChildren().add(search);
    StackPane.setAlignment(search, Pos.BOTTOM_CENTER);
    ViewsContext.bindComponent(ViewsContext.SEARCH_INPUT, search);
  }

  @Override
  public void handle(MouseEvent event){
    Node source = (Node) event.getSource();
    if(source == playList){
      if("play_list".equals(playList.getId())) return;

      playList.setId("play_list");
      netCollect.setId("net_collect");
      view.getChildren().set(1, musicView);
    }else if(source == netCollect){
      if(!"play_list".equals(playList.getId())) return;

      initCollect();
      playList.setId("play_list_lfocus");
      netCollect.setId("net_collect_focus");
      view.getChildren().set(1, collect);
    }
  }

  void initCollect(){
    if(collect != null) return;

    collect = new TitledPane("  默认收藏", ResourceManager.getViewOfClasspath("collect_empty.png"));
    collect.setPrefHeight(445);
    collect.setAnimated(false);
    collect.setId("center_line");
    collect.setGraphic(UIBuilder.buildGroupBtn());
    collect.setContentDisplay(ContentDisplay.RIGHT);
    collect.setGraphicTextGap(170);
  }

}
