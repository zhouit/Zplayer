package org.zplayer;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;

import org.zplayer.search.MusicSearcher;

public class BottomContainer implements EventHandler<MouseEvent>{
  private HBox view;

  public BottomContainer(){
    Label playAdd = LabelBuilder.create().id("play_add").prefHeight(24).prefWidth(264 / 4).build();

    Label playDel = LabelBuilder.create().id("play_del").prefHeight(24).prefWidth(264 / 4).build();

    Label playModel = LabelBuilder.create().id("play_model").prefHeight(24).prefWidth(264 / 4)
        .build();

    Label playSearch = LabelBuilder.create().id("play_search").prefHeight(24).prefWidth(264 / 4)
        .onMouseClicked(this).build();

    view = HBoxBuilder.create().padding(new Insets(2, 20, 0, 20))
        .children(playAdd, playDel, playModel, playSearch).build();
  }
  
  public Node getView(){
    return view;
  }

  public void handle(MouseEvent event){
    event.consume();
    TextField search = (TextField) ViewsContext.getComponent(ViewsContext.SEARCH_INPUT);
    if(search.isVisible()){
      search.setVisible(false);
      search.setText("");
      PlayAccordion pa=(PlayAccordion)ViewsContext.getComponent(ViewsContext.PLAY_ACCORDION);
      pa.backView();
    }else{
      search.setVisible(true);
      MusicSearcher.getInstance().prepare();
    }
    // MusicContainer mc=playAccordion.listmc.get(0);
    // playAccordion.getAccordion().getPanes().remove(mc.getView());
    // playAccordion.listmc.remove(mc);
    // mc.clear();
  }

}
