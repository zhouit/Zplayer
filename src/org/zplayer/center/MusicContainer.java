package org.zplayer.center;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.zplayer.PlayAccordion;
import org.zplayer.ViewsContext;
import org.zplayer.resp.AppConfig;
import org.zplayer.resp.MusicInfo;
import org.zplayer.resp.LoadManager.LoadOver;
import org.zplayer.search.MusicSearcher;
import org.zplayer.utils.ResourceManager;
import org.zplayer.utils.StringUtils;

public class MusicContainer implements EventHandler<ActionEvent>, LoadOver{
  private TitledPane view;
  private ListContainer content;
  private PlayAccordion parent;

  public MusicContainer(PlayAccordion parent, ListContainer lc){
    this.content = lc;
    this.parent = parent;
    this.view = new TitledPane();
    this.view.setContent(content.getView());
    this.view.setAnimated(true);

    GroupTitle gt = new GroupTitle(content.getGroup(), content.getView().getItems().size());
    gt.setMenuListener(parent.getMenuListener());
    content.groupProperty().bind(gt.titleProperty());
    gt.setMenuBar(buildGroupMenu(lc.getGroup()));
    view.setGraphic(gt);
  }

  public MusicContainer(PlayAccordion parent, String name){
    this.parent = parent;
    view = new TitledPane();
    view.setContent(initAdd());
    view.setAnimated(true);

    GroupTitle gt = new GroupTitle(name, 0);
    gt.setMenuListener(parent.getMenuListener());
    gt.setMenuBar(buildGroupMenu(name));
    view.setGraphic(gt);
  }

  public ListContainer getListContainer(){
    return content;
  }

  public ListView<MusicInfo> getContentView(){
    ListView<MusicInfo> result = null;
    if(content != null) result = content.getView();

    return result;
  }

  public String getMusicGroup(){
    GroupTitle gt = (GroupTitle) view.getGraphic();
    return StringUtils.getGroupTitle(gt.getTitle());
  }

  public TitledPane getView(){
    return this.view;
  }

  public GroupTitle getGroupTitle(){
    return (GroupTitle) view.getGraphic();
  }

  private Node initAdd(){
    VBox addPane = new VBox();
    addPane.setPadding(new Insets(30, 0, 0, 80));
    Hyperlink add_file = new Hyperlink("添加歌曲文件");
    add_file.setGraphic(ResourceManager.getViewOfClasspath("add_file.png"));
    add_file.setOnAction(this);
    Hyperlink add_floder = new Hyperlink("添加歌曲文件夹");
    add_floder.setGraphic(ResourceManager.getViewOfClasspath("add_folder.png"));
    addPane.getChildren().addAll(add_file, add_floder);

    return addPane;
  }

  public void removeMusic(int index){
    MusicInfo info = content.getView().getItems().remove(index);
    getGroupTitle().setNumber(-1);
    if(content.getView().getItems().size() == 0){
      this.content.groupProperty().unbind();
      this.content.getView().setContextMenu(null);
      this.content = null;
      view.setContent(initAdd());
    }

    MusicSearcher.getInstance().prepare();
    MusicSearcher.getInstance().deleteMusic(info);
  }

  public void addMusic(MusicInfo info){
    if(content == null){
      this.content = new ListContainer(getMusicGroup(), new ArrayList<MusicInfo>());
      this.content.groupProperty().bind(getGroupTitle().titleProperty());
      this.content.getView().setContextMenu(parent.getContextMenu());
      view.setContent(this.content.getView());
    }

    info.group = getMusicGroup();
    this.content.getView().getItems().add(info);
    getGroupTitle().setNumber(1);

    MusicSearcher.getInstance().prepare();
    MusicSearcher.getInstance().addMusic(info);
  }

  public void updateIndexs(String oldGroup, String newGroup){
    if(getListContainer() == null) return;

    MusicSearcher.getInstance().prepare();
    MusicSearcher.getInstance().deleteMusicByGroup(oldGroup);
    MusicSearcher.getInstance().addMusics(getContentView().getItems());
  }

  public void clear(){
    if(content != null){
      content.groupProperty().unbind();
      content.getView().setContextMenu(null);

      MusicSearcher.getInstance().prepare();
      MusicSearcher.getInstance().deleteMusicByGroup(getMusicGroup());
    }

    this.view.setGraphic(null);
  }

  Menu buildGroupMenu(String group){
    MenuItem new_group = null, rename = null, delete_group = null;
    Menu menu = MenuBuilder.create()
        .items(new_group = new MenuItem("   新建列表\t", ResourceManager.getViewOfClasspath("menu_add.png")),
            new MenuItem("   收藏整个列表\t", ResourceManager.getViewOfClasspath("favorite.png")),
            delete_group = new MenuItem("   删除列表\t"), rename = new MenuItem("   重命名\t"),
            new MenuItem("   清空列表歌曲\t", ResourceManager.getViewOfClasspath("menu_clear.png")),
            new MenuItem("   导入列表文件\t"), new MenuItem("   导出列表文件\t")).build();

    new_group.setOnAction(parent.getGroupHandler());
    rename.setOnAction(parent.getGroupHandler());
    delete_group.setOnAction(parent.getGroupHandler());
    rename.setUserData(group);
    delete_group.setUserData(group);

    return menu;
  }

  public void handle(ActionEvent event){
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().add(new ExtensionFilter("歌曲文件", "*.mp3", "*.wav"));
    List<File> files = chooser.showOpenMultipleDialog(ViewsContext.stage());
    if(files != null && files.size() != 0){
      String group = getMusicGroup();
      List<MusicInfo> infos = AppConfig.loadMusic(files, group, this).get(group);
      this.content = new ListContainer(group, infos);
      content.getView().setContextMenu(parent.getContextMenu());
      view.setContent(content.getView());

      GroupTitle gt = getGroupTitle();
      gt.setNumber(infos.size());
      content.groupProperty().bind(gt.titleProperty());

      AppConfig.dataChange = true;
    }
  }

  public void loadOver(){
    MusicSearcher.getInstance().prepare();
    MusicSearcher.getInstance().addMusics(getContentView().getItems());

    Platform.runLater(new Runnable(){
      public void run(){
        // 此处在ListView中更新时间信息
        getListContainer().updateBigCell();
      }
    });
  }

}
