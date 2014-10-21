package org.zplayer.center;

import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuBarBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import org.zplayer.utils.StringUtils;

public class GroupTitle extends AnchorPane implements EventHandler<KeyEvent>{
  MenuListener listener;

  TextField rename;
  Label name;
  MenuBar menu;
  int number;

  public GroupTitle(String group, int number){
    this.number = number;
    setPrefWidth(260);
    name = LabelBuilder.create().text(group + "(" + number + ")").build();

    menu = MenuBarBuilder.create().id("group_menu").prefWidth(104 / 4).prefHeight(23).build();

    rename = new TextField(group);
    rename.setVisible(false);

    getChildren().addAll(name, rename, menu);
    AnchorPane.setLeftAnchor(name, 5.0);
    AnchorPane.setTopAnchor(name, 5.0);

    AnchorPane.setRightAnchor(menu, 2.0);
    AnchorPane.setTopAnchor(menu, 2.0);

    AnchorPane.setLeftAnchor(rename, 5.0);
    AnchorPane.setTopAnchor(rename, 2.0);
  }

  public void setMenuListener(MenuListener lis){
    this.listener = lis;
  }

  public void renameTitle(){
    name.setVisible(false);
    rename.setVisible(true);
    rename.setText(StringUtils.getGroupTitle(name.getText()));
    rename.requestFocus();
    if(rename.getOnKeyPressed() != null) return;

    rename.setOnKeyPressed(this);
  }

  public void handle(KeyEvent event){
    if(event.getCode().equals(KeyCode.ENTER)){
      name.setVisible(true);
      rename.setVisible(false);
      if(!"".equals(rename.getText().trim())){
        name.setText(rename.getText() + "(" + number + ")");
        // 更新重命名MenuItem的userData
        MenuItem item = menu.getMenus().get(0).getItems().get(3);
        listener.changed((String) item.getUserData(), rename.getText());
        item.setUserData(rename.getText());
      }

      rename.setOnKeyPressed(null);
    }
  }

  public void setMenuBar(Menu item){
    this.menu.getMenus().add(item);
  }

  public Menu getMenu(){
    return menu.getMenus().get(0);
  }

  public String getTitle(){
    return name.getText();
  }

  public StringProperty titleProperty(){
    return rename.textProperty();
  }

  public void setNumber(int change){
    this.number = number + change;
    name.setText(rename.getText() + "(" + number + ")");
  }

}
