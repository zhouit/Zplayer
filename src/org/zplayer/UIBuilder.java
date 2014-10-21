package org.zplayer;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.zplayer.utils.ResourceManager;

public final class UIBuilder{

  public static Menu buildMenu(EventHandler<ActionEvent> menuHandler){
	MenuItem back=null,about=null,setting=null;
	Menu result=MenuBuilder.create().text("                  ")
		.items(new MenuItem("   登录",
			  ResourceManager.getViewOfClasspath("menu_login.png")),
			MenuBuilder.create().text("  添加本地歌曲")
				.items(new MenuItem("  添加歌曲"),new MenuItem("  添加歌曲文件夹"))
				.build(),new SeparatorMenuItem(),new MenuItem("  显示桌面歌词"),
			new MenuItem("  显示音乐库",
				ResourceManager.getViewOfClasspath("menu_check.png")),
			new MenuItem("  显示今日推荐"),new MenuItem("  显示音效插件"),
			new MenuItem("  显示均衡器"),new SeparatorMenuItem(),
			new MenuItem("  皮肤颜色",
				ResourceManager.getViewOfClasspath("menu_skin.png")),
			MenuBuilder.create().text("  迷你模式")
				.items(new MenuItem("  音乐魔方模式"),new MenuItem("  经典微型模式"))
			    .build(),new MenuItem("  界面总在最前\t\t"),new SeparatorMenuItem(),
			new MenuItem("  更多音乐工具"),
			MenuBuilder.create().text("  帮助与反馈")
			  .graphic(ResourceManager.getViewOfClasspath("menu_help.png"))
			  .items(back=new MenuItem("  意见反馈\t",
					ResourceManager.getViewOfClasspath("menu_back.png")),
			      about=new MenuItem("  关于Zplayer"))
			  .build(),
			setting=new MenuItem("  选项设置",
				ResourceManager.getViewOfClasspath("menu_set.png")),
			new MenuItem("  退出",
				ResourceManager.getViewOfClasspath("menu_exit.png")))
	   .build();
	
	back.setOnAction(menuHandler);
	about.setOnAction(menuHandler);
	setting.setOnAction(menuHandler);
	
	return result;
  }
  
  public static Node buildGroupBtn(){
	return LabelBuilder.create()
		.id("group_btn")
		.prefWidth(104/4).prefHeight(23)
		.build();
   }
  
  public static ImageView loadpView(String fileName,double width,double height){
	  Image result=ResourceManager.loadClasspathImage(fileName);
	  ImageView renVal=new ImageView(result);
	  renVal.setViewport(new Rectangle2D(0, 0, width,height));
	  return renVal;
  }
  
  static void mailToOrBrowse(boolean mail){
	if(!Desktop.isDesktopSupported()) return ;
	
	Desktop desktop=Desktop.getDesktop();
	try{
	  if(desktop.isSupported(Action.MAIL)&&mail){
		 desktop.mail(new URI("mailto:155811492@qq.com"));
	  }else if(desktop.isSupported(Action.BROWSE)){
		 desktop.browse(new URI("http://www.zhouhaocheng.cn/"));	
	   }
	}catch(IOException e){
	  e.printStackTrace();
	}catch(URISyntaxException e){
	  e.printStackTrace();
	}
   }
  
  public static void openDefaultBrowser(String http){
	 try{
	   String cmd = "rundll32 url.dll,FileProtocolHandler "+http;
	  Runtime.getRuntime().exec(cmd);
	}catch(IOException e){
	  e.printStackTrace();
	 }
   }
  
}
