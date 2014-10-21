package org.zplayer.center;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.stage.Popup;
import javafx.stage.Screen;

import org.zplayer.ViewsContext;
import org.zplayer.resp.MusicInfo;
import org.zplayer.utils.Locals;
import org.zplayer.utils.ResourceManager;

public abstract class AbstractMusicTips{
  private boolean contextmenushow = false;
  // 是否实例化
  boolean instantiate = false;

  Popup popup;
  private Group content;
  private ImageView pbg;

  public AbstractMusicTips(){
  }

  protected void initTips(){
    popup = new Popup();
    popup.setAutoHide(true);
    popup.setAutoFix(true);
    popup.setHideOnEscape(false);

    initLayout();
  }

  public void menuShow(){
    this.contextmenushow = true;
  }

  public void menuHide(){
    this.contextmenushow = false;
  }

  private void initLayout(){
    content = new Group();

    pbg = ImageViewBuilder.create().fitHeight(102).fitWidth(257).build();

    ImageView head = ImageViewBuilder.create().id("tips_image")
        .image(ResourceManager.loadClasspathImage("singer.png")).layoutX(10).layoutY(20).build();

    Label text = LabelBuilder.create().alignment(Pos.CENTER_LEFT).layoutX(75).layoutY(20).build();

    Label infomation = LabelBuilder.create().layoutX(75).layoutY(55).build();

    content.getChildren().addAll(pbg, head, text, infomation);
    popup.getContent().add(content);
  }

  protected boolean postCheck(MusicInfo info){
    return info == null || ViewsContext.stage().getY() < 6 || contextmenushow || popup.isShowing();
  }

  protected void attach(MusicInfo info, Node target){
    Label text = (Label) content.getChildren().get(2);
    text.setText(info.getFullname());

    Label information = (Label) content.getChildren().get(3);
    information.setText("大小:" + info.getFileSize() + "\t文件格式:" + info.getFormat()
        + "\n码率:000 Kbps  播放次数:10");

    Screen screen = Screen.getPrimary();
    double shiftTop = 32.0 - 15;
    if(Locals.getRightScreen(target) + 260 > screen.getBounds().getWidth()){
      pbg.setImage(ResourceManager.loadClasspathImage("tips_left.png"));
      popup.show(ViewsContext.stage(), Locals.getLeftScreen(target) - 255, Locals.getTopScreen(target)
          - shiftTop);
    }else{
      pbg.setImage(ResourceManager.loadClasspathImage("tips_right.png"));
      double right = Locals.getRightScreen(target);
      // 当ListView有Slider时
      if(target.getLayoutBounds().getWidth() == 284.0){
        right += 13.0;
      }

      // 这里把stage改为Node也是一样的,但歌曲分组(实际即为TitledPane)可能被删除
      // 而popup持有了MusicCell引用(getOwnerNode()),则其不能被垃圾回收
      popup.show(ViewsContext.stage(), right, Locals.getTopScreen(target) - shiftTop);
    }
  }

}