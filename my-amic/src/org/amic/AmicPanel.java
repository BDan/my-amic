package org.amic;
import java.awt.*;

//import javax.swing.*; 
//import java.util.*;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.*;

public class AmicPanel extends Canvas implements Emulator.Display, HierarchyListener{
	static final long serialVersionUID = -1723922483885254455L;

	
	
	//Emulator aMIC = new Emulator();
	public int[] keyMatrix = new int[8];
	


	long msLast = 0;
	private BufferStrategy strategy;
	boolean overTime;
	//boolean smoothRender = true;
	boolean smoothRender = false;
	boolean wasScaled = false;
	boolean showDebug = false;
	
	int lastSize = 0;
	int debugFlags[] = new int[6];
	Tracer tracer = new Tracer(5);

	public AmicPanel() {
		super();
		setBounds(0, 0, 768, 768);
		addHierarchyListener(this);

	}
	
	public void setShowFPS(boolean val){
		showDebug = val;
	}


	public void paintComponent(Graphics comp) {
		//Graphics2D comp2D = (Graphics2D) comp;
		//paintGraphics(comp2D);
	}
	@Override
	public Tracer getTracer(){
		return tracer;
	}

	void paintGraphics(Graphics2D comp2D, Image img) {

		/*
		if (debugFlags[0]>0){
			smoothRender = false;
		} else{
			smoothRender = true;
		} 
		*/
		if (smoothRender){
			comp2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}

		int imgSize = (int) (Math.min(getSize().height, getSize().width));
		/*
		if (imgSize<lastSize){
			wasScaled = true;
		} else
			wasScaled = false;
		lastSize = imgSize;
		*/
		//long tmpTime = System.nanoTime();
		comp2D.drawImage(img, 0, 0, imgSize, imgSize, this);
		//msT2 = (System.nanoTime() - tmpTime)/100000;
		
		tracer.draw(comp2D, getSize());

		
//		if (showDebug){
//			comp2D.setColor(Color.RED);
//			comp2D.drawString(String.format("Simulate: %06d Draw: %06d",debugFlags[1],debugFlags[2]), 30, 30);
//		}
//			
//		if (debugFlags[0]>0){
//			if (debugFlags[0]==1)
//				comp2D.setColor(Color.GREEN);
//			else
//				comp2D.setColor(Color.RED);		
//			
//		} else{
//			comp2D.setColor(Color.WHITE);
//		}
//		comp2D.fillRect(0, getSize().height-10, 4, 4);

		msLast = System.nanoTime();
	}
	@Override
	public void updateScreen (Image img){
		//From an accelerated Java tutorial at http://www.cokeandcode.com/info/tut2d.html
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		paintGraphics(g, img);
		g.dispose();
		strategy.show();
		
	}
	
	public void setDebugFlag (int key, int value){
		if (key<0 || key>= debugFlags.length)
			return;
		debugFlags[key]=value;
		
	}
	
	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		//System.out.println("\Hierarchy Event: "+e.toString());
		if ((e.getChangeFlags()&HierarchyEvent.SHOWING_CHANGED) != 0){
			//A strategy object can't be created until the canvas is assigned to a visible container
			
			createBufferStrategy(2);
			strategy = getBufferStrategy();
			setIgnoreRepaint(true);
		}
		
		
	}

	@Override
	public void showTracer(boolean show) {
		// TODO Auto-generated method stub
		
	}


}
