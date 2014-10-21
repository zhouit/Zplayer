package org.zplayer.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.channels.Channel;

import javax.xml.stream.XMLStreamWriter;

public class IOUtils{

  public static void closeQuietly(Writer writer){
    try{
      if(writer != null) writer.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public static void closeQuietly(XMLStreamWriter writer){
    try{
      if(writer != null) writer.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void closeQuietly(Reader reader){
    try{
      if(reader != null) reader.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void closeQuietly(InputStream in){
    try{
      if(in != null) in.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void closeQuietly(OutputStream out){
    try{
      if(out != null) out.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void closeQuietly(Channel channel){
    try{
      if(channel != null) channel.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void closeQuietly(Socket socket){
    try{
      if(socket != null) socket.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void closeQuietly(ServerSocket server){
    try{
      if(server != null) server.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static String decodeUrl(String str, String charset){
    String result = null;
    try{
      result = URLDecoder.decode(str, charset);
    }catch(Exception e){
      e.printStackTrace();
    }

    return result;
  }

  public static File getFileByUrl(String url){
    File result = null;
    try{
      result = new File(new URI(url));
    }catch(URISyntaxException e){
      e.printStackTrace();
    }

    return result;
  }

}