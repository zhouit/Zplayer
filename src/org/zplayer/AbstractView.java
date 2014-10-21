package org.zplayer;

import org.zplayer.resp.MusicInfo;

import javafx.scene.Node;

public abstract class AbstractView{
  MusicInfo music;
  Node view;

  public AbstractView(){
    this(null);
  }

  public AbstractView(MusicInfo info){
    this.music = info;

    initView();
  }

  public void setMusic(MusicInfo info){
    this.music = info;

    updateView();
  }

  protected abstract void initView();

  protected abstract void updateView();

  public Node getView(){
    return view;
  }

}
