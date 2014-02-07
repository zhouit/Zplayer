package org.zhc.zplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javafx.util.Duration;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.zhc.zplayer.LoadManager.LoadOver;
import org.zhc.zplayer.search.MusicSearcher;
import org.zhc.zplayer.utils.IOUtils;

@SuppressWarnings("unchecked")
public final class AppConfig{
  public static final String LYRIC_DIR = "lyricDir";

  // 应用歌曲数据是否改变
  public static boolean dataChange = false;
  private static boolean settingsChange = false;
  private static Properties configs;

  public static boolean isSettingsChange(){
    return settingsChange;
  }

  public static void saveSettings(){
    if(!settingsChange) return;

    String dir = System.getenv("APPDATA");
    File file = new File(dir, "ZPlayer/zplayer.properties");
    FileOutputStream fos = null;
    try{
      fos = new FileOutputStream(file);
      configs.store(fos, "GBK");
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      IOUtils.closeQuietly(fos);
    }
  }

  private static File getAppfile(){
    String dir = System.getenv("APPDATA");
    File file = new File(dir, "ZPlayer/config.xml");

    return file;
  }

  public static String getConfig(String key){
    return configs.getProperty(key);
  }

  public static void updateConfig(String key, String value){
    settingsChange = true;

    configs.put(key, value);
  }

  public static File getAppIndex(){
    String dir = System.getenv("APPDATA");
    File file = new File(dir, "ZPlayer/Index");
    if(!file.exists()) file.mkdirs();

    return file;
  }

  public static void loadConfigs(){
    if(configs != null) return;

    String dir = System.getenv("APPDATA");
    File file = new File(dir, "ZPlayer/zplayer.properties");
    configs = new Properties();

    if(!file.exists()) return;
    FileInputStream fis = null;
    try{
      fis = new FileInputStream(file);
      configs.load(fis);
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      IOUtils.closeQuietly(fis);
    }
  }

  private static Map<String, List<MusicInfo>> loadXml(){
    Map<String, List<MusicInfo>> maps = null;
    File file = getAppfile();
    if(file.exists()){
      Document doc = parseXml(file);
      Element root = doc.getRootElement();
      List<Element> groups = (List<Element>) root.elements();
      maps = new HashMap<String, List<MusicInfo>>();
      for(Element element : groups){
        String group_name = element.attributeValue("name");
        List<MusicInfo> list = new ArrayList<MusicInfo>();
        for(Object obj : element.elements()){
          Element tempEle = (Element) obj;
          MusicInfo music = parseInfo(tempEle);
          music.group = group_name;
          list.add(music);
        }

        maps.put(group_name, list);
      }
    }

    return maps;
  }

  public static Map<String, List<MusicInfo>> loadMusic(){
    Map<String, List<MusicInfo>> result = null;
    result = loadXml();

    if(result == null){
      result = new HashMap<String, List<MusicInfo>>();
      result.put("默认列表", null);
    }else{
      if(getAppIndex().listFiles().length == 0 && !result.isEmpty()){
        MusicSearcher.getInstance().prepare();
        MusicSearcher.getInstance().addMusics(getMusics(result));
      }
    }

    loadConfigs();
    return result;
  }

  public static Map<String, List<MusicInfo>> loadMusic(String dirName){
    return loadMusic(new File(dirName));
  }

  public static Map<String, List<MusicInfo>> loadMusic(File dir){
    File[] files = dir.listFiles(new FilenameFilter(){
      public boolean accept(File dir, String name){
        return name.endsWith(".mp3") || name.endsWith(".wav");
      }
    });

    return loadMusic(Arrays.asList(files), "默认列表", null);
  }

  public static Map<String, List<MusicInfo>> loadMusic(List<File> files, String group,
      LoadOver callback){
    Map<String, List<MusicInfo>> result = new HashMap<String, List<MusicInfo>>();

    List<MusicInfo> list = new ArrayList<MusicInfo>();
    for(File tempFile : files){
      MusicInfo tempMusic = new MusicInfo(tempFile.toURI().toString(), group);
      list.add(tempMusic);
    }

    result.put(group, list);

    new LoadManager(callback, result).start();

    return result;
  }

  static MusicInfo parseInfo(Element element){
    MusicInfo music = new MusicInfo(element.elementText("url"));
    String time = element.elementText("time");
    if(null != time && !"".equals(time.trim())){
      music.time = Duration.valueOf(time);
    }

    return music;
  }

  static void saveXml(Map<String, List<MusicInfo>> maps){
    File config = getAppfile();
    File parent = config.getParentFile();
    if(!parent.exists()) parent.mkdir();
    if(config.exists()) config.delete();

    Document doc = DocumentHelper.createDocument();
    Element root = DocumentHelper.createElement("player");
    doc.setRootElement(root);

    for(Map.Entry<String, List<MusicInfo>> entry : maps.entrySet()){
      Element group = DocumentHelper.createElement("group").addAttribute("name", entry.getKey());
      for(MusicInfo music : entry.getValue()){
        Element temp = DocumentHelper.createElement("music").addAttribute("id",
            music.hashCode() + "");
        temp.add(DocumentHelper.createElement("url").addText(music.url));
        if(music.time != Duration.ZERO){
          temp.add(DocumentHelper.createElement("time").addText(music.time.toSeconds() + "s"));
        }

        group.add(temp);
      }

      root.add(group);
    }

    try{
      saveDocument(doc, config);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void updateXml(MusicInfo music){
    File file = getAppfile();
    if(file.exists()){
      Document doc = parseXml(file);
      Element target = (Element) doc.selectSingleNode("/player/group[@name='" + music.group
          + "']/music[@id='" + music.hashCode() + "']");
      target.add(DocumentHelper.createElement("time").addText(music.time.toSeconds() + "s"));

      try{
        saveDocument(doc, file);
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }

  static Document parseXml(File file){
    Reader reader = null;
    try{
      reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
      return new SAXReader().read(reader);
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      IOUtils.closeQuietly(reader);
    }

    return null;
  }

  static void saveDocument(Document doc, File file) throws Exception{
    boolean newLine = false;
    String indent = "";
    if(!file.exists()){
      newLine = true;
      indent = " ";
    }

    OutputFormat format = new OutputFormat(indent, newLine);
    format.setEncoding("UTF-8");
    XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"),
        format);
    writer.write(doc);
    writer.flush(); // 注意：当XMLWriter构造方法用的是FileWriter时，必须调用此flush（）方法或close（）
  }

  public static void debug(Object obj){
    System.out.println(obj);
  }

  private static List<MusicInfo> getMusics(Map<String, List<MusicInfo>> maps){
    List<MusicInfo> list = new ArrayList<MusicInfo>();
    for(List<MusicInfo> tempList : maps.values()){
      list.addAll(tempList);
    }

    return list;
  }

}