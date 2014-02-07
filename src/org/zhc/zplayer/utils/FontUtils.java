package org.zhc.zplayer.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

@SuppressWarnings("serial")
public class FontUtils {

  public static int getPixLength(String target,int fontsize){
	 Font font = new Font("楷体", Font.BOLD, fontsize);
	 FontMetrics metrics = new FontMetrics(font) {};
	 Rectangle2D bounds = metrics.getStringBounds(target, null);
	 int widthInPixels = (int) bounds.getWidth();
	 
	 return widthInPixels;
   }
  
}
