package org.zplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Accordion;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;

import org.zplayer.center.DelayMusicTips;
import org.zplayer.center.ListContainer;
import org.zplayer.center.MenuListener;
import org.zplayer.center.MusicContainer;
import org.zplayer.resp.AppConfig;
import org.zplayer.resp.MusicInfo;
import org.zplayer.utils.ResourceManager;
import org.zplayer.utils.StringUtils;

public class PlayAccordion implements EventHandler<WindowEvent>{
  public static ObjectProperty<MusicInfo> playIndex = new SimpleObjectProperty<MusicInfo>(
      new MusicInfo(null, null, null));

  List<MusicContainer> listmc;
  private Accordion accordion;
  private EventHandler<ActionEvent> group_menu_handler;
  private ContextMenu cm;
  private MenuListener menuListener;

  public PlayAccordion(){
    initDB();
    initAccordion();
  }

  public Accordion getAccordion(){
    return accordion;
  }

  MusicContainer findViewByGroup(String group){
    for(MusicContainer mc : listmc){
      if(mc.getMusicGroup().equals(group)/* &&mc.getListContainer()!=null */){
        return mc;
      }
    }

    return null;
  }

  void filterView(Map<String, List<MusicInfo>> maps){
    for(MusicContainer mc : listmc){
      String key = mc.getMusicGroup();
      ListContainer lc = findViewByGroup(key).getListContainer();
      if(lc != null) lc.filterData(maps.get(key));
    }
  }

  void backView(){
    for(MusicContainer mc : listmc){
      ListContainer lc = mc.getListContainer();
      if(lc != null){
        lc.backData();
      }
    }
  }

  // 获取所有分组
  public List<String> getGroups(){
    List<String> result = new ArrayList<String>(listmc.size());
    for(MusicContainer mc : listmc){
      if(mc.getListContainer() != null){
        result.add(mc.getMusicGroup());
      }
    }

    return result;
  }

  public void playNextMusic(){
    MusicInfo result = null, last = playIndex.get();
    List<MusicInfo> list = findViewByGroup(last.group).getContentView().getItems();
    if(MusicInfo.myindex == list.size() - 1){
      List<String> groups = getGroups();
      int temp = groups.indexOf(last.group);
      result = findViewByGroup(groups.get(temp == groups.size() - 1 ? 0 : temp + 1))
          .getContentView().getItems().get(0);

      MusicInfo.myindex = 0;
    }else{
      result = findViewByGroup(last.group).getContentView().getItems().get(MusicInfo.myindex + 1);
      MusicInfo.myindex++;
    }

    playMusic(last, result);
  }

  public void playMusic(){
    List<String> groups = getGroups();
    if(groups.isEmpty()) return;

    playMusic(playIndex.get(), findViewByGroup(groups.get(0)).getContentView().getItems().get(0));
    MusicInfo.myindex++;
  }

  public void playPrevMusic(){
    MusicInfo result = null, last = playIndex.get();
    if(MusicInfo.myindex == 0){
      List<String> groups = getGroups();
      int temp = groups.indexOf(last.group);
      String prevGroup = groups.get(temp == 0 ? groups.size() - 1 : temp - 1);
      List<MusicInfo> list = findViewByGroup(prevGroup).getContentView().getItems();
      result = list.get(list.size() - 1);
      MusicInfo.myindex = list.size() - 1;
    }else{
      result = findViewByGroup(last.group).getContentView().getItems().get(MusicInfo.myindex - 1);
      MusicInfo.myindex--;
    }

    playMusic(last, result);
  }

  @SuppressWarnings("unchecked")
  public Map<String, List<MusicInfo>> getMusicData(){
    Map<String, List<MusicInfo>> result = new HashMap<String, List<MusicInfo>>();
    for(MusicContainer mc : listmc){
      ListContainer lc = mc.getListContainer();
      result.put(mc.getMusicGroup(), lc == null ? (List<MusicInfo>) Collections.EMPTY_LIST : lc
          .getView().getItems());
    }

    return result;
  }

  public ContextMenu getContextMenu(){
    return this.cm;
  }

  private void playMusic(MusicInfo last, MusicInfo current){
    PlayAccordion.playIndex.set(current);
    if(last.group != null && !current.group.equals(last.group)){
      // findViewByGroup(last.group).getContentView().scrollTo(0);
      findViewByGroup(last.group).getListContainer().updateBigCell();
    }

    ListContainer lc = findViewByGroup(current.group).getListContainer();
    lc.updateBigCell();
  }

  private void initDB(){
    this.listmc = new ArrayList<MusicContainer>();
    Map<String, List<MusicInfo>> maps = AppConfig.loadMusic();
    loadContextMenu(maps.keySet());
    for(Map.Entry<String, List<MusicInfo>> entry : maps.entrySet()){
      List<MusicInfo> data = entry.getValue();
      MusicContainer mc = null;
      if(data != null && !data.isEmpty()){
        ListContainer tempView = new ListContainer(entry.getKey(), data);
        tempView.getView().setContextMenu(cm);
        mc = new MusicContainer(this, tempView);
      }else{
        mc = new MusicContainer(this, "默认列表");
      }

      listmc.add(mc);
    }
  }

  private void initAccordion(){
    accordion = new Accordion();
    accordion.setId("center_line");
    accordion.setPrefHeight(445);
    accordion.prefWidth(300);
    for(MusicContainer mc : listmc){
      accordion.getPanes().add(mc.getView());
    }

    if(listmc.size() > 0) listmc.get(0).getView().setExpanded(true);

    TitledPane lastPlay = new TitledPane("  最近播放(2)", new Text("暂无最近播放记录"));
    lastPlay.setAnimated(false);
    lastPlay.setGraphic(UIBuilder.buildGroupBtn());
    lastPlay.setContentDisplay(ContentDisplay.RIGHT);
    lastPlay.setGraphicTextGap(160);
    accordion.getPanes().add(lastPlay);
  }

  void loadContextMenu(Set<String> groups){
    this.cm = new ContextMenu();
    cm.setOnShown(this);
    cm.setOnHidden(this);

    Menu move = new Menu("   移动到列表\t");
    EventHandler<ActionEvent> moveHandler = new ContextMenuHandler();

    for(String group : groups){
      MenuItem moveTo = new MenuItem("   " + group + "\t");
      moveTo.setOnAction(moveHandler);
      move.getItems().add(moveTo);
    }

    move.getItems().addAll(new SeparatorMenuItem(),
        new MenuItem("   新建列表  \t", ResourceManager.getViewOfClasspath("menu_add.png")));

    cm.getItems().addAll(
        new MenuItem("   播放\t"),
        move,
        new MenuItem("   收藏到列表", ResourceManager.getViewOfClasspath("favorite.png")),
        MenuItemBuilder.create().text("   移除歌曲\t").onAction(moveHandler).build(),
        new MenuItem("   删除歌曲\t", ResourceManager.getViewOfClasspath("menu_del.png")),
        new MenuItem("   歌曲去重工具\t"),
        new SeparatorMenuItem(),
        new MenuItem("   为歌曲匹配MV\t", ResourceManager.getViewOfClasspath("menu_mv.png")),
        new SeparatorMenuItem(),
        MenuBuilder.create().text("   重命名\t")
            .items(new MenuItem("   智能重命名\t"), new MenuItem("   手动重命名\t")).build(),
        MenuBuilder
            .create()
            .text("   播放模式\t")
            .items(new MenuItem("   单曲播放\t"),
                new MenuItem("   顺序播放\t", ResourceManager.getViewOfClasspath("menu_check.png")),
                new MenuItem("   单曲循环\t"), new MenuItem("   全部循环\t"), new MenuItem("   随机播放\t"),
                new SeparatorMenuItem(),
                new MenuItem("   自动切换列表\t", ResourceManager.getViewOfClasspath("menu_check.png")))
            .build(), MenuItemBuilder.create().text("   歌曲排序\t").onAction(moveHandler).build(),
        new MenuItem("   打开文件所在目录\t"), new MenuItem("   导出列表文件"),
        new MenuItem("   发送到移动设备\t", ResourceManager.getViewOfClasspath("menu_send.png")),
        new MenuItem("   歌曲信息\t"), new SeparatorMenuItem(), new MenuItem("   搜索相关歌曲\t"));
  }

  public EventHandler<ActionEvent> getGroupHandler(){
    if(group_menu_handler != null) return this.group_menu_handler;

    this.group_menu_handler = new EventHandler<ActionEvent>(){
      public void handle(ActionEvent event){
        MenuItem item = (MenuItem) event.getSource();
        String action = StringUtils.trimWhitespace(item.getText());
        if("新建列表".equals(action)){
          if(accordion.getPanes().size() > 10) return;

          AppConfig.dataChange = true;
          int index = accordion.getPanes().size() - 1;
          MusicContainer tempMc = new MusicContainer(PlayAccordion.this, "新建列表" + index);
          listmc.add(tempMc);
          accordion.getPanes().add(index, tempMc.getView());

          // 向ContextMenu添加新MenuItem
          Menu move = (Menu) cm.getItems().get(1);
          List<MenuItem> list = move.getItems();
          MenuItem newItem = new MenuItem("   新建列表" + index);
          newItem.setOnAction(list.get(0).getOnAction());
          list.add(list.size() - 2, newItem);
        }else if("重命名".equals(action)){
          String group = (String) item.getUserData();
          findViewByGroup(group).getGroupTitle().renameTitle();
        }else if("删除列表".equals(action)){
          String group = (String) item.getUserData();

          AppConfig.dataChange = true;
          MusicContainer mc = findViewByGroup(group);

          System.out.println("delete MusicGroup = " + mc.getMusicGroup());
          listmc.remove(mc);
          getAccordion().getPanes().remove(mc.getView());
          mc.clear();
          // new GroupRemover(mc).remove();

          Menu move = (Menu) cm.getItems().get(1);
          List<MenuItem> list = move.getItems();
          int i = 0;
          for(; i < list.size() - 2; i++){
            MenuItem tempItem = list.get(i);
            if(StringUtils.trimWhitespace(tempItem.getText()).equals(group)){
              break;
            }
          }

          list.remove(i);
        }
      }
    };

    return this.group_menu_handler;
  }

  // 更改Accordion歌曲分组的图标
  public void updateListenView(MusicInfo old, MusicInfo newv){
    if(newv.group.equals(old.group)) return;

    for(MusicContainer mc : listmc){
      String tempGroup = mc.getMusicGroup();
      TitledPane tp = mc.getView();
      if(tempGroup.equals(old.group)){
        tp.setId(null);
        tp.setExpanded(false);
      }

      if(tempGroup.equals(newv.group)){
        tp.setId("listen");
        tp.setExpanded(true);
      }
    }
  }

  public void handle(WindowEvent event){
    if(event.getEventType() == WindowEvent.WINDOW_SHOWN){
      DelayMusicTips.getMusicTips().menuShow();
    }else if(event.getEventType() == WindowEvent.WINDOW_HIDDEN){
      DelayMusicTips.getMusicTips().menuHide();
    }
  }

  public MenuListener getMenuListener(){
    if(menuListener != null) return menuListener;

    menuListener = new MenuListener(){
      public void changed(String old, String newv){
        changeMgName(old, newv);
      }
    };

    return menuListener;
  }

  private void changeMgName(String old, String newv){
    Menu move = (Menu) cm.getItems().get(1);
    // 这里可能是新建的列表 无ListView就无ContextMenu
    for(int i = 0; i < move.getItems().size() - 2; i++){
      MenuItem tempItem = move.getItems().get(i);
      if(StringUtils.trimWhitespace(tempItem.getText()).equals(old)){
        tempItem.setText("   " + newv);
        break;
      }
    }

    AppConfig.dataChange = true;
    System.out.println(old + " , " + newv);
    MusicContainer mc = findViewByGroup(newv);
    if(mc.getListContainer() != null){
      mc.getListContainer().setDataGroup(newv);
      mc.updateIndexs(old, newv);
    }
  }

  final class ContextMenuHandler implements EventHandler<ActionEvent>{
    public void handle(ActionEvent event){
      MenuItem item = (MenuItem) event.getSource();
      String targetText = StringUtils.trimWhitespace(item.getText());
      MusicContainer mc = getSelectContainer();
      String tempGroup = mc.getMusicGroup();

      // if(targetText.equals(tempGroup)&&item.getParentMenu()!=null) return ;
      if("歌曲排序".equals(targetText)){
        AppConfig.dataChange = true;

        mc.getListContainer().sortData();
        MusicInfo.myindex = mc.getListContainer().getView().getItems().indexOf(playIndex.get());
        return;
      }

      MusicInfo selectItem = mc.getContentView().getSelectionModel().getSelectedItem();
      if(selectItem == null){
        AppConfig.debug("未选中任何歌曲！");
        return;
      }

      int selectIndex = mc.getContentView().getItems().indexOf(selectItem);
      MusicInfo index = PlayAccordion.playIndex.get();
      if(tempGroup.equals(index.group)){
        if(selectIndex < MusicInfo.myindex){
          MusicInfo.myindex--;
        }else if(selectIndex == MusicInfo.myindex){
          // MusicInfo.myindex=findViewByGroup(targetText).getContentView().getItems().size();
          return;
        }
      }

      AppConfig.dataChange = true;
      // 当选中移动歌曲时
      if(item.getParentMenu() != null){
        mc.removeMusic(selectIndex);
        findViewByGroup(targetText).addMusic(selectItem);
      }else if("移除歌曲".equals(targetText)){ // 当选中移除歌曲时
        mc.removeMusic(selectIndex);
      }
    }

    MusicContainer getSelectContainer(){
      for(MusicContainer mc : listmc){
        if(mc.getView().isExpanded()){
          return mc;
        }
      }

      return null;
    }

  }

}