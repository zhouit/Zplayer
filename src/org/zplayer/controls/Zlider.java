package org.zplayer.controls;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import org.zplayer.utils.Locals;

/**
 * 自定义滑动条,暂只支持横向
 * 
 * @author zhou
 *
 */
public class Zlider extends Region implements EventHandler<MouseEvent>{
  private Pane track;
  private Pane bar;
  private ImageView thumb;
  private double gap = 0;
  private double dragStart;
  private double width;
  private DragingHandler draging;
  private double progress = 0;
  /* 当前是否可拖动 */
  private boolean drag = true;

  public Zlider(double width, double height, double gap, Image image){
    getStyleClass().add("zlider");
    this.gap = gap;
    setPrefSize(width, image.getHeight());
    this.width = width;
    track = new Pane();
    track.getStyleClass().add("track");
    track.setPrefSize(width, height);
    bar = new Pane();
    bar.setPrefHeight(height - 3 * gap);
    bar.getStyleClass().add("bar");

    thumb = new ImageView(image);
    thumb.setStyle("-fx-cursor:hand;");
    thumb.setOnMouseDragged(this);
    thumb.setOnMousePressed(this);
    getChildren().addAll(track, bar, thumb);
  }

  /**
   * 获取当前滑动条值
   * 
   * @return
   */
  public double getValue(){
    return progress;
  }

  public void setDraggable(boolean drag){
    this.drag = drag;
  }

  public void setDragingHandler(DragingHandler handler){
    this.draging = handler;
  }

  /**
   * 设置当前进度(0-1之间)
   * 
   * @param progress
   */
  public void setProgress(double progress){
    this.progress = progress;
    bar.setPrefWidth(progress * (track.getPrefWidth() - 2 * gap));
    requestLayout();
  }

  @Override
  protected void layoutChildren(){
    layoutInArea(track, 0, thumb.getImage().getHeight() / 2 - track.getPrefHeight() / 2,
        track.getPrefWidth(), track.getPrefHeight(), getBaselineOffset(), HPos.LEFT, VPos.TOP);
    layoutInArea(bar, gap, thumb.getImage().getHeight() / 2 - bar.getPrefHeight() / 2 - gap,
        bar.getPrefWidth(), bar.getPrefHeight(), getBaselineOffset(), HPos.LEFT, VPos.TOP);

    double x = progress * track.getPrefWidth() - thumb.getImage().getWidth() / 2;
    x = Locals.balance(x, 0, track.getWidth() - thumb.getImage().getWidth());
    layoutInArea(thumb, x, 0, thumb.getImage().getWidth(), thumb.getImage().getHeight(),
        getBaselineOffset(), HPos.LEFT, VPos.TOP);
  }

  @Override
  public void handle(MouseEvent event){
    event.consume();
    if(!drag) return;

    if(event.getEventType() == MouseEvent.MOUSE_DRAGGED){
      double current = thumb.localToParent(event.getX(), event.getY()).getX();
      double value = Locals.balance(progress + (current - dragStart) / track.getPrefWidth(), 0.0,
          1.0);
      dragStart = Locals.balance(current, 0, width);
      setProgress(value);

      if(draging != null) draging.dragHandler(progress);
    }else if(event.getEventType() == MouseEvent.MOUSE_PRESSED){
      dragStart = thumb.localToParent(event.getX(), event.getY()).getX();
    }
  }

  public static interface DragingHandler{

    /**
     * 拖动回调
     * 
     * @param progress
     *          0-1之间
     */
    public void dragHandler(double progress);

  }

}