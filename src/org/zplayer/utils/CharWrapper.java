package org.zplayer.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class CharWrapper{

  public static enum CharType {
    CHINESE(2), ASCII(0), OTHER(1);

    int typeVlaue;

    private CharType(int type){
      this.typeVlaue = type;
    }

    public int getTypeValue(){
      return typeVlaue;
    }

  }

  char value;

  public CharWrapper(char c){
    this.value = c;
  }

  public CharType getType(){
    return getCharType(value);
  }

  public static CharType getCharType(char target){
    CharType result = null;
    if(target >= 0x4e00 && target <= 0x9fbf){
      result = CharType.CHINESE;
    }else if(target > 0 && target <= 128){
      result = CharType.ASCII;
    }else{
      result = CharType.OTHER;
    }

    return result;
  }

  public static String getPinyin(char target){
    if(getCharType(target) != CharType.CHINESE){
      throw new IllegalArgumentException(target + " 必须传入中文字符！");
    }

    // 创建汉语拼音处理类
    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    // 输出设置，大小写，音标方式
    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);

    String pinyin = null;
    try{
      pinyin = PinyinHelper.toHanyuPinyinStringArray(target, format)[0];
    }catch(BadHanyuPinyinOutputFormatCombination e){
      e.printStackTrace();
    }

    return pinyin;
  }

  public static int compare(char one, char two){
    CharType ot = getCharType(one);
    CharType tt = getCharType(two);
    if(ot != tt){
      return ot.getTypeValue() - tt.getTypeValue();
    }

    if(CharType.CHINESE != ot){
      return one - two;
    }

    return getPinyin(one).compareTo(getPinyin(two));
  }

  public char getValue(){
    return value;
  }

  // public static void main(String[] args){
  // System.out.println(CharWrapper.compare('A', '阿'));
  // }

}
