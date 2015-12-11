package org.amic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;

/*
 * Class for tracing emulator's real-time performance
 */
public class Tracer {
	int [][] values;
	int [][] numValues;
	int active = 0;
	int row = 0;
	Color[]colorList=new Color[]{Color.RED,Color.GREEN, Color.BLUE,Color.MAGENTA,Color.CYAN,Color.ORANGE};
	Color reference = Color.YELLOW;
	Stroke stroke = new BasicStroke(4);
	boolean show=false;
	private int size;
	
	public Tracer(int size){
		this.size = size;
		values = new int[50][size];
		numValues = new int[2][size];
	}
	/**
	 * Note: first value (idx=0) is the reference length, the next ones are bar segments.
	 * if the total sum of bars from 1 to size is equal to the reference (0) then the 
	 * combined bar length will be equal to the reference bar
	 * @param idx
	 * @param value
	 */
	public void setValue(int idx, int value){
		values[row][idx]=value;
	}
	public void changePage(){
			active=(active+1)%1;
			row=(row+1)%values.length;
	}
	public void setNumValue(int idx, int value){
		numValues[active][idx]=value;
	}
	
	public void setShow(boolean show){
		this.show=show;
	}
	
	public void draw(Graphics2D graphics, Dimension dim){
		if (!show) return;
		//graphics.clearRect(0, 0, dim.width, dim.height);
		
		int base = (active+1)%2;
		int refLen=(int)(dim.width*75/100);
		graphics.setStroke(stroke);
		graphics.setColor(reference);
		int y = dim.height - 10;
		int x = 5;
		graphics.drawLine(x+0, y, x+refLen, y);
		y -=5;
		int x1 = x;
		graphics.setColor(Color.WHITE);
		graphics.drawString(String.format(" %4d, %d",numValues[base][1],numValues[base][0] ), refLen, dim.height-20);
		
		for (int ydx=0;ydx<values.length;ydx++){
			for (int idx=1;idx<size;idx++){
				
				int x2=x1+(int)((double)values[ydx][idx]/values[ydx][0]*refLen);
				if(x2>x1){
					graphics.setColor(colorList[(idx-1)%colorList.length]);
					graphics.drawLine(x1, y, x2, y);
				}
				x1=x2;
				
				
			}
			x1=x;
			y-=5;
		}

	}

}
