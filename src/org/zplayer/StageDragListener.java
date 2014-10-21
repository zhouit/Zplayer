package org.zplayer;

import org.zplayer.utils.Threads;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public final class StageDragListener implements EventHandler<MouseEvent>{
	Point2D anchor,previous;
	Stage stage;
	double hange;
	
	public StageDragListener(Stage s){
	  this.stage=s;
	  
	  this.stage.setOnShown(new EventHandler<WindowEvent>(){
	     public void handle(WindowEvent we){
	       we.consume();
	       previous = new Point2D(stage.getX(), stage.getY());
	      }
	  });
	 }
	  
	public void handle(MouseEvent event){
	  event.consume();
	  Node window_label=(Node)event.getTarget();
	  if("WinZoom".equals(window_label.getId())||"WinClose".equals(window_label.getId())){
		return ;  
	   }
	  
	  if(event.getEventType()==MouseEvent.MOUSE_PRESSED){
		 anchor = new Point2D(event.getScreenX(), event.getScreenY());
	  }else if(event.getEventType()==MouseEvent.MOUSE_RELEASED){
		 previous= new Point2D(stage.getX(), stage.getY());
	  }else if(event.getEventType()==MouseEvent.MOUSE_DRAGGED){
		 double newX=previous.getX() + event.getScreenX() - anchor.getX();
		 Rectangle2D rect=Screen.getPrimary().getBounds();
		 if(rect.getWidth()-stage.getWidth()<newX){
			newX = rect.getWidth()-stage.getWidth();
		 }else if(newX<2.0){
			newX=2.0;
		 }
		 
		 stage.setX(newX);
		 double newY=previous.getY() + event.getScreenY() - anchor.getY();
		 if(newY<6.0){
		    newY=6.0;
		  }
		 
	     stage.setY(newY);
	  }else if(event.getEventType()==MouseEvent.MOUSE_ENTERED){
		if(stage.getY()!=hange) return ;
		
		new Thread(){
	      public void run(){
	       double frame=stage.getHeight()/15;
	       for(int i=1;i<=15;i++){
	    	  stage.setY(hange+i*frame);
	    	  Threads.sleeps(20);
	        }
	      }
		}.start();
	  }
	  
	  if(event.getEventType()==MouseEvent.MOUSE_RELEASED){
		if(stage.getY()!=6.0) return ;
		 
		 //因为Stage的x,y属性为只读不能用Timeline等类了
		 new Thread(){
		   public void run(){
		     double frame=(3.0-stage.getHeight())/15;
		     for(int i=1;i<=15;i++){
		       stage.setY(3.0+frame*i);
		       Threads.sleeps(20);
		      }
		       
		     hange=stage.getY();
		    }
		 }.start();
	  }
	  
	 }
	
   public void enableDrag(Node node){
	 node.setOnMousePressed(this);
	 node.setOnMouseReleased(this);
	 node.setOnMouseDragged(this);
	}
   
   public void enableHange(){
      this.stage.getScene().getRoot().setOnMouseEntered(this);
	  this.stage.getScene().getRoot().setOnMouseExited(this);
   }
   
   public void enableDrag(Scene scene){
	  scene.setOnMousePressed(this);
	  scene.setOnMouseReleased(this);
	  scene.setOnMouseDragged(this);
   }
   
  }