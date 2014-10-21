package org.zplayer.utils;

import java.util.concurrent.TimeUnit;

public class Threads{

  public static void sleeps(long time){
    try{
      Thread.sleep(time);
    }catch(InterruptedException e){
      e.printStackTrace();
    }
  }

  public static void sleeps(long duration, TimeUnit unit){
    sleeps(unit.toMillis(duration));
  }

  public static void wait(Object target){
    try{
      target.wait();
    }catch(InterruptedException e){
      e.printStackTrace();
    }
  }

}
