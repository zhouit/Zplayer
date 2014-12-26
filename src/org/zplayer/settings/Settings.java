package org.zplayer.settings;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineBuilder;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.zplayer.StageDragListener;
import org.zplayer.ViewsContext;
import org.zplayer.resp.AppConfig;
import org.zplayer.utils.ResourceManager;

public class Settings implements EventHandler<ActionEvent>{
  Stage stage;
  TextField lyricLocation;

  public Settings(){
    stage = new Stage();
    stage.initOwner(ViewsContext.stage());
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.initModality(Modality.APPLICATION_MODAL);
    Label title = LabelBuilder.create().translateY(5).text("选项设置").textFill(Color.WHITE).build();

    EventHandler<MouseEvent> aboutHandler = new EventHandler<MouseEvent>(){
      public void handle(MouseEvent event){
        event.consume();
        stage.close();
      }
    };

    Label close = LabelBuilder.create().onMouseClicked(aboutHandler).prefHeight(26).prefWidth(33)
        .id("WinClose").build();
    HBox head = HBoxBuilder.create().id("head").padding(new Insets(0, 0, 0, 5)).spacing(312)
        .children(title, close).build();

    Rectangle clip = new Rectangle(400, 330);
    clip.setArcHeight(10.0);
    clip.setArcWidth(10.0);
    VBox root = VBoxBuilder.create().prefWidth(400).prefHeight(330)
        .children(head, loadTab(), createBottom()).clip(clip).id("bg")
        .stylesheets(ResourceManager.getResourceUrl("/org/zplayer/settings/settings.css"))
        .build();

    StageDragListener sdl = new StageDragListener(stage);
    sdl.enableDrag(head);
    Scene scene = new Scene(root);
    scene.setFill(Color.TRANSPARENT);
    stage.setScene(scene);
  }

  public void show(){
    stage.show();
  }

  public TabPane loadTab(){
    TabPane tabs = new TabPane();
    tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    tabs.setPrefSize(350, 260);
    tabs.setSide(Side.TOP);
    Tab lyric = new Tab();
    lyric.setId("lyric");
    lyricLocation = new TextField(AppConfig.getConfig(AppConfig.LYRIC_DIR));
    lyricLocation.setEditable(false);
    lyricLocation.setPrefWidth(170);
    HBox lhbox = HBoxBuilder.create()
        .padding(new Insets(0, 0, 0, 10)).spacing(5)
        .alignment(Pos.CENTER_LEFT)
        .children(new Label("歌词文件保存路径:"), lyricLocation,
            ButtonBuilder.create().text("...").onAction(this).build()).build();
    lyric.setContent(VBoxBuilder.create().padding(new Insets(10, 0, 0, 20))
        .alignment(Pos.TOP_CENTER).spacing(20).children(createCaption("歌词设置"), lhbox).build());

    Tab mixer = new Tab();
    mixer.setId("mixer");
    Tab update = new Tab();
    update.setId("update");
    tabs.getTabs().addAll(lyric, mixer, update);

    return tabs;
  }

  Node createBottom(){
    HBox bottom = HBoxBuilder.create()
        .padding(new Insets(10, 0, 0, 220)).spacing(20)
        .children(
            ButtonBuilder.create().onAction(this).text("确定").prefHeight(27).prefWidth(68)
                .id("button").build(),
            ButtonBuilder.create().prefHeight(27).prefWidth(68).onAction(this).text("取消")
                .id("button").build()).build();

    return bottom;

  }

  Node createCaption(String header){
    return HBoxBuilder.create()
        .children(new Label(header),
              LineBuilder.create().startX(0)
                     .startY(0).endX(280).endY(0).translateY(6).translateX(12)
                     .build())
        .build();
  }

  public void handle(ActionEvent event){
    event.consume();
    Button target = (Button) event.getSource();
    if("确定".equals(target.getText())){
      if(!lyricLocation.getText().equals(AppConfig.getConfig(AppConfig.LYRIC_DIR))){
        AppConfig.updateConfig(AppConfig.LYRIC_DIR, lyricLocation.getText());
      }

      stage.close();
    }else if("取消".equals(target.getText())){

      stage.close();
    }else if("...".equals(target.getText())){
      DirectoryChooser chooser = new DirectoryChooser();
      chooser.setTitle("请选择歌词文件夹");
      File result = chooser.showDialog(stage);
      if(result != null){
        lyricLocation.setText(result.getAbsolutePath());
      }
    }

  }

}
