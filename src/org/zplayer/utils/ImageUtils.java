package org.zplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class ImageUtils{

  public static Image clip(Image source, int width, int height, int x, int y){
    PixelReader reader = source.getPixelReader();
    return new WritableImage(reader, x, y, width, height);
  }

  public static Image[] split(Image source, int cols){
    int width = (int) source.getWidth() / cols;
    Image[] result = new Image[cols];
    for(int i = 0; i < cols; i++){
      result[i] = ImageUtils.clip(source, width, (int) source.getHeight(), i * width, 0);
    }

    return result;
  }

}
