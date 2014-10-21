package org.zplayer.utils;

import javafx.scene.text.Font;

import com.sun.javafx.tk.Toolkit;

public class FontUtils{

  public static int getPixLength(String target, Font font){
    float width = Toolkit.getToolkit().getFontLoader().computeStringWidth(target, font);

    return (int) width;
  }

}
