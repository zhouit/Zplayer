package org.zplayer.utils;

import javafx.util.Duration;

public class StringUtils{

  public static int getLength(String str){
    int result = 0;
    for(int i = 0; i < str.length(); i++){
      char c = str.charAt(i);
      if(c > 0 && c < 128){
        result += 1;
      }else{
        result += 2;
      }
    }

    return result;
  }

  public static String getGroupTitle(String str){
    int index = 0;
    for(int i = 0; i < str.length(); i++){
      if(str.charAt(i) == '('){
        index = i;
        break;
      }
    }

    return str.substring(0, index);
  }

  public static String trimWhitespace(String str){
    StringBuilder result = new StringBuilder();
    for(int i = 0; i < str.length(); i++){
      char c = str.charAt(i);
      if(c == '\n' || c == ' ' || c == '\t' || c == '\r'){
        continue;
      }

      result.append(c);
    }

    return result.toString();
  }

  public static String formatDuration(Duration time){
    double minutes = time.toMinutes();
    int minutesWhole = (int) Math.floor(minutes);
    int secondsWhole = (int) Math.round((minutes - minutesWhole) * 60);
    return String.format("%1$02d:%2$02d", minutesWhole, secondsWhole);
  }

  public static String toMinutes(Duration time){
    double minutes = time.toMinutes();
    int minutesWhole = (int) Math.floor(minutes);
    int secondsWhole = (int) Math.round((minutes - minutesWhole) * 60);
    return String.format("%1$02d.%2$02d", minutesWhole, secondsWhole) + "m";
  }

  public static int compare(String one, String two){
    int olen = one.length();
    int tlen = two.length();
    int lim = Math.min(olen, tlen);

    int k = 0;
    while(k < lim){
      char oc = one.charAt(k);
      char tc = two.charAt(k);

      int value = CharWrapper.compare(oc, tc);
      if(value != 0){
        return value;
      }

      k++;
    }

    return olen - tlen;
  }

}
