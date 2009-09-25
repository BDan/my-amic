/*
 *	Audio.java
 *
 *	Copyright 2007-2008 Jan Bobrowski <jb@wizard.ae.krakow.pl>
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	version 2 as published by the Free Software Foundation.
 */

//import java.util.Arrays;

import javax.sound.sampled.*;
import java.util.*;
class Audio
{

	/*
	byte buf[] = new byte[400];
	int bufp;
	long div;
	int idiv;
	int acct;
	int accv0, accv1, level;
	*/
	int mul;
	static final int FREQ = 22050;
	//static final int FREQ = 11000;
	int f_cpu = 0;
	int y_prev = 0;
	
	int auFreq=0;
	float filt = 0F;

	byte buf1[] = new byte[100];
	boolean first = true;
	long quot = 0;
	
	public int frame = 0;
	int pos = 0;
	int lastbit = 0;
	byte lastVal = 0;

	/*
	void open(int hz)
	{
		div = hz;
		acct = hz;
		idiv = (1<<30) / hz;
	}
	*/
	public void setCpuFreq (int cpuFreq){
		this.f_cpu = cpuFreq;
		Arrays.fill(buf1,(byte)0);
		//this.quot = cpuFreq-1;
		//acct = auFreq;
		//acct = 0;
		
	}
	//Sample rectangular signal, to test the audio if
	public void test_rect (){
		boolean bit = false;
		int idx = 0;
		int lim = 14;
	
		for (int i=0;i<50000;i++){
			
			byte val = bit?(byte)-100:(byte)100;
			buf1[pos++]= val;
			if (pos==buf1.length){
				SourceDataLine l = line;
				if(l!=null)
					l.write(buf1, 0, pos);
				pos = 0;
			}
			if (idx++==lim){
				bit = !bit;
				idx=0;
				if (bit)
					lim=12;
				else
					lim=120;
			}
			
		}
		
		
	}
	
	public void test2(){
		//int val =0;
		int len, bit;
		boolean choice = false;
		for (int i=0;i<50000;i++){
			if (choice){
				//len = 1509; //real
				len = 1509;
				bit = 0;
			} else{
				len = 1444; //real
				//len = 1509;
				bit = 1;
				
			}
			ping (len,bit);
			choice = !choice;
			
		}
	}
	int t_acc, acc;
	public void ping(long t, int bit){
		final int v_max = 100;
		int f_au = FREQ;
		int t_n = (int)t*f_au;
		final int alpha = 710; //740
		
		if (t_n<t_acc){
			//System.out.println("short");
			acc += t_n*bit;
			t_acc -= t_n;
		} else{
			acc += t_acc*bit;
			int x0 = acc * 2* v_max/f_cpu - v_max;
			t_n = t_n - t_acc;
			t_acc = f_cpu - t_n % f_cpu;
			acc = bit*(f_cpu - t_acc);
			int x = 0, y = 0;
			for (int i=0 ; i<=(t_n / f_cpu);i++){
				if (i==0){
					x = x0;
				} else{
					x = v_max*(2*bit-1); 
				}
			
			y = (alpha * x + (1000 - alpha) * y_prev)/1000;
			if (Math.abs(y)>v_max)
				y = (y>0 ?1:-1)*v_max;
			y_prev = y;
			buf1[pos++] =(byte)(y);
			
			if (pos==buf1.length){
				SourceDataLine l = line;
				if(l!=null)
					l.write(buf1, 0, pos);
				pos = 0;
			}
			}
		}
	}
	
	
	SourceDataLine line;

	Audio() {
		try {
			auFreq = FREQ;
			
			mul = FREQ;
			AudioFormat fmt
			  = new AudioFormat(FREQ, 8, 1, true, false);
			System.out.println(fmt);
			SourceDataLine l = (SourceDataLine)AudioSystem.getLine(
				new DataLine.Info(SourceDataLine.class, fmt)
			);
			//l.open(fmt, 4096);
			
			l.open(fmt, 4096);
			System.out.printf("Buffer size: %d\n",l.getBufferSize());
			l.start();
			line = l;
			
		} catch (Exception e) {
			
			System.out.println(e);
		}
	}



	synchronized void close() {
		SourceDataLine l = line;
		if(l!=null) {
			line = null;
			l.stop();
			l.close();
		}
	}
	



} 