package org.zplayer.resp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javafx.util.Duration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zplayer.resp.LoadManager.LoadOver;
import org.zplayer.search.MusicSearcher;
import org.zplayer.utils.IOUtils;
import org.zplayer.utils.IndentXMLStreamWriter;

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

  public static Map<String, List<MusicInfo>> loadXmls(){
    Map<String, List<MusicInfo>> maps = null;
    File file = getAppfile();
    if(!file.exists()) return maps;

    try{
      XMLInputFactory factory = XMLInputFactory.newFactory();
      XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(file), "UTF-8");
      int type = reader.getEventType();
      MusicInfo temp = null;
      String group = null;
      while(type != XMLStreamConstants.END_DOCUMENT){
        switch(type){
        case XMLStreamConstants.START_DOCUMENT:
          maps = new HashMap<String, List<MusicInfo>>();
          break;
        case XMLStreamConstants.START_ELEMENT:
          String name = reader.getName().toString();
          if("group".equals(name)){
            group = reader.getAttributeValue(0);
            maps.put(group, new ArrayList<MusicInfo>());
          }else if("music".equals(name)){
            temp = new MusicInfo();
            temp.group = group;
          }else if("url".equals(name)){
            temp.url = reader.getElementText();
          }else if("time".equals(name)){
            temp.time = Duration.valueOf(reader.getElementText());
          }

          break;
        case XMLStreamConstants.END_ELEMENT:
          if("music".equals(reader.getName().toString())){
            maps.get(group).add(temp);
          }

          break;
        }

        type = reader.next();
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    return maps;
  }

  public static Map<String, List<MusicInfo>> loadMusic(){
    Map<String, List<MusicInfo>> result = null;
    result = loadXmls();

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

  public static void saveXmls(Map<String, List<MusicInfo>> maps){
    File config = getAppfile();
    File parent = config.getParentFile();
    if(!parent.exists()) parent.mkdir();
    if(config.exists()) config.delete();

    XMLStreamWriter writer = null;
    try{
      XMLOutputFactory factory = XMLOutputFactory.newFactory();
      writer = new IndentXMLStreamWriter(factory.createXMLStreamWriter(
          new FileOutputStream(config), "UTF-8"));
      writer.writeStartDocument("UTF-8", "1.0");
      writer.writeEndDocument();
      writer.writeStartElement("player");
      for(Map.Entry<String, List<MusicInfo>> entry : maps.entrySet()){
        writer.writeStartElement("group");
        writer.writeAttribute("name", entry.getKey());
        for(MusicInfo music : entry.getValue()){
          writer.writeStartElement("music");
          writer.writeAttribute("id", music.hashCode() + "");

          writer.writeStartElement("url");
          writer.writeCharacters(music.url);
          writer.writeEndElement();

          if(music.time != Duration.ZERO){
            writer.writeStartElement("time");
            writer.writeCharacters(music.time.toSeconds() + "s");
            writer.writeEndElement();
          }
          writer.writeEndElement();
        }
        writer.writeEndElement();
      }
      writer.writeEndElement();
      writer.flush();
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      IOUtils.closeQuietly(writer);
    }

  }

  public static void updateXml(MusicInfo music){
    File file = getAppfile();
    if(!file.exists()) return;
    try{
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      FileInputStream fin = new FileInputStream(file);

      Document doc = builder.parse(fin);
      XPath xpath = XPathFactory.newInstance().newXPath();
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, " ");

      Element ele = (Element) xpath.evaluate("/player/group[@name='" + music.group
          + "']/music[@id='" + music.hashCode() + "']", doc, XPathConstants.NODE);
      ele.setAttribute("time", music.time.toSeconds() + "s");
      transformer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(file)));
      fin.close();
    }catch(Exception e){
      e.printStackTrace();
    }
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