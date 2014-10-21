package org.zplayer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuBarBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.zplayer.settings.Settings;
import org.zplayer.utils.ResourceManager;
import org.zplayer.utils.StringUtils;

public class TopContainer implements EventHandler<MouseEvent>{
  private AnchorPane view;

  private Stage about;
  private Settings settings;
  AbstractView controls;

  public TopContainer(){
    view = new AnchorPane();

    Label close = LabelBuilder.create().prefHeight(26).prefWidth(33).onMouseClicked(this)
        .id("WinClose").build();
    view.getChildren().add(close);
    AnchorPane.setRightAnchor(close, 0.0);
    AnchorPane.setTopAnchor(close, 0.0);

    Label zoom = LabelBuilder.create().prefHeight(26).prefWidth(33).onMouseClicked(this)
        .id("WinZoom").build();
    view.getChildren().add(zoom);
    AnchorPane.setRightAnchor(zoom, 32.0);
    AnchorPane.setTopAnchor(zoom, 0.0);

    MenuBar menubar = MenuBarBuilder.create().prefHeight(26).prefWidth(87).id("MenuBar").build();

    menubar.getMenus().add(UIBuilder.buildMenu(menuHandler()));
    view.getChildren().add(menubar);
    AnchorPane.setLeftAnchor(menubar, 5.0);
    AnchorPane.setTopAnchor(menubar, 0.0);

    controls = new Controls();
    Node cview = controls.getView();
    view.getChildren().add(cview);
    AnchorPane.setLeftAnchor(cview, 10.0);
    AnchorPane.setTopAnchor(cview, 32.0);
  }

  public Region getView(){
    return view;
  }

  public AbstractView getControls(){
    return controls;
  }

  public void handle(MouseEvent event){
    event.consume();
    Node label = (Node) event.getTarget();
    if(label.getId().equals("WinClose")){
      if(ResourceManager.isWindows()){
        ViewsContext.stage().hide();
      }else{
        TrayManager.getTrayManager().clearAndQuit();
      }
    }else if(label.getId().equals("WinZoom")){
      ViewsContext.stage().setIconified(true);
    }
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
    about.initOwner(ViewsContext.stage());
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

    Hyperlink http = new Hyperlink("www.zhouhaocheng.cn");
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
                .children(new Label("作者博客:"), http).build()).clip(clip).id("bg")
        .stylesheets(ResourceManager.getResourceUrl("/org/zhc/zplayer/about.css")).build();

    StageDragListener sdl = new StageDragListener(about);
    sdl.enableDrag(head);
    Scene scene = new Scene(aboutRoot);
    about.setScene(scene);
  }

}
