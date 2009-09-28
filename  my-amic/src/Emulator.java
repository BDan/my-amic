/*
 * aMIC/PRAE emulator
 * Hardware emulator class
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class Emulator implements Z80.Env, Runnable {
	interface Display{
		void updateScreen (Image I);
		void setDebugFlag (int key, int val);
		//0 = overtime frame
		//1 = processing time [ms]
	}

	
//Constant values
	final int FPS = 50;
	final int timeFrame = 10000/FPS;
	private final int AD_ROM=0x4000;
	private final int cpu_freq = 2500000; //Hz
	long audioTime;
	
//Hardware modules	
	public int memo[] = new int[0x10000];
	Audio speaker;// = new Audio();
	public int audio_bit=0;
	boolean keyed=true;
	private int portC = 0; //output
	public int portB = 0; //input
	public int keyMatrix[] = new int[8]; //mapped on portA
	private static final int scrW = 256;
	private static final int scrH = 256;
	BufferedImage I = new BufferedImage(scrW, scrH, BufferedImage.TYPE_INT_RGB);
	
	public final Z80 cpu = new Z80(this);
	Display displayer = null;
	
	
	Thread runner;
	
	/*
	public amic_emu (long frame_len){
		this.frame_len = frame_len;
	}
	*/


	public void setTargetDisplay (Display displayer){
		this.displayer = displayer;
	}

	//Z80.Env methods	
	public int m1(int pc, int ir){


		return memo[pc];
	};
	public int mem(int addr){
		//System.out.printf("Mem read @[%02X] -> %02X\n", addr,memo[addr]);

		return memo[addr];
	};
	public void mem(int addr, int v){
		//System.out.printf("%02X -> [%02X]\n", addr,v);
		if (addr>=AD_ROM){
			memo[addr]=v;
		}
	};
	public int in(int port){
		//System.out.printf("%04X: IN  <-%02X\n",cpu.pc(), port&0xff);
		int retVal = 0xff;
		//cpu.time+=20;
		
		if ((port & 0xff) == 0x20){
			//System.out.printf ("!!\n");
			
			retVal = (~keyMatrix[portC&7])&0xFF;//~2&0xff;
			//if (retVal!=0xff)
		   //System.out.printf ("in: %02X\n",retVal);
				//keyed = false;
			
			
		} 
		if ((port & 0xff) == 0x21){
			//System.out.printf ("!!\n");
			
			retVal = (~portB)&0xFF;//~2&0xff;
			//if (retVal!=0xff)
		   //System.out.printf ("in: %02X\n",retVal);
				//keyed = false;
			
			
		} 
		//	retVal = 0xf7;
		return retVal;
	};
	
	public void out(int port, int v){
		
		if ((port & 0xff) ==0x22){
			
			int newAudioBit = (v&0x08)==0 ?0:1; 
			if (audio_bit != newAudioBit){
				speaker.ping(cpu.time-audioTime, audio_bit);
				
				audioTime = cpu.time;
				audio_bit = newAudioBit;
				
			}
			
			portC = v;
			
		}
		
	};
	
	public int mem16(int addr){
		int retVal = memo[addr] | memo[addr+1]<<8;
		//System.out.printf("Mem16 read @[%02X] -> %02X\n", retVal,memo[addr]);
		return retVal;
	};
	public void mem16(int addr, int v){
		if (addr>=AD_ROM){
		memo[addr] = v&0xff;
		memo[addr+1] = v>>>8;
		}
		//System.out.printf("16 bit write: %02X -> [%02X]\n", addr,v);
		
	};
	
	public int halt(int n, int ir){
		
		System.out.printf ("HALT received after %d ticks =%f sec\n",cpu.time,1.0*cpu.time/cpu_freq);
		cpu.time_limit=0;
		return n;
	};
	
	public void loadMem (int [] data, int offset){
		for (int i=0;i<data.length;i++){
			memo[i+offset]=data[i];
		}
		
	}
	public void initMem (){
		Arrays.fill(memo,0);
	}
	
	public void loadHex (String fName){
		try {
		    HexFileParser hfp = new HexFileParser(new File(fName));
		     hfp.parseFile(memo,0);
		} catch (Exception e) {
			System.out.println("Can't load ROM file: "+fName);
		    e.printStackTrace();
		    System.exit(0);
		}
	}
	
	void drawVram() {
		final int white = 0xe4e4e4, black = 0x000000;
		for (int addr = 0x4000; addr < 0x6000; addr++) {
			int val = memo[addr];
			int vAddr = addr - 0x4000;
			int row = vAddr / 32;
			int col = (vAddr % 32) * 8;
			int iColor = 0;
			for (int idx = 0; idx < 8; idx++) {
				if (((val << idx) & 0x80) > 0)
					iColor = white;
				else
					iColor = black;
				I.setRGB(col + idx, row, iColor);
			}
		}
	}
	
	@Override
	public void run() {

		Thread thisThread = Thread.currentThread();
		speaker = new Audio();
		speaker.setCpuFreq(cpu_freq);
		
		//speaker.frame = 20050/FPS;
		
		//long tAveraged=0;
		
		
		//int ctl = 0;
		//speaker.test_rect();
		//speaker.test2();
		
		int frameTicks = cpu_freq/FPS;
		cpu.time_limit = frameTicks; //(int)(frameTime/(1000*tick));
		cpu.time = 0;
		audioTime = 0;
		long time = System.nanoTime()/100000; //System.currentTimeMillis();
		while (runner == thisThread) {
			long tStart = System.nanoTime()/100000;
			cpu.execute();
			drawVram();
			
			long tSym = System.nanoTime()/100000;
			displayer.updateScreen(I);

			long tEnd = System.nanoTime()/100000;
			speaker.ping(cpu.time - audioTime, audio_bit);
			//speaker.step((int)(cpu.time - audioTime), audio_bit);
			
			
			displayer.setDebugFlag(1,(int)(tSym-tStart));
			displayer.setDebugFlag(2,(int)(tEnd-tSym));

			//long tPassed = (System.nanoTime() - tStart) / 1000000;
			//IRF averaging 
			//tAveraged = (tAveraged*9 + tPassed)/10;
			displayer.setDebugFlag(0,0);
			time += timeFrame;  
			long crTime = System.nanoTime()/100000; //System.currentTimeMillis();
			if (time > crTime)
				try {
					Thread.sleep((time - crTime)/10);
				} catch (InterruptedException e) {
					// do nothing
				}
			else{
				
				//overTime = true;
				displayer.setDebugFlag(0,1);
				Thread.yield();
				crTime -= 1000;
				if (crTime > time){
					displayer.setDebugFlag(0,2);
					time = crTime;
				} 
			}
			
			
			//audioTime -= frameTicks;
			cpu.time -= frameTicks;
			//cpu.time =0;
			audioTime = cpu.time;
			//Runtime.getRuntime().gc();
		}
	}

	public void setKeyMatrix(int row, int col, boolean val) {
		if (val) {
			keyMatrix[row] |= (1 << col);
		} else {
			keyMatrix[row] &= ~(1 << col);
		}
		// System.out.printf("%d, %d, %b\n", row, col, val);
	}

	public void setKeyPortB(int row, int col, boolean val) {
		if (row == 8) {
			if (val) {
				portB |= (1 << col);
			} else {
				portB &= ~(1 << col);
			}
		}
		// System.out.printf("%d, %d, %b\n", row, col, val);
	}
	public void launch() {
		initMem();
		loadHex("d:\\2009_n\\other\\amic\\mon_v01\\mon_amic.hex");
		loadHex("d:\\2009_n\\other\\tape\\amic.hex");
		 
		//loadHex("d:\\2009_n\\other\\amic\\bios\\amic_01.hex");

		//loadHex("d:\\2009\\amic_emu\\mon_amic_v01\\mon_amic.hex");
		
		//loadHex("d:\\2009\\z80_tape\\2009_09_02\\tape\\visz80_amic.hex");

		// aMIC.startAddr(0x8017);
		// 
		cpu.reset();
		//cpu.pc(0x8000);
		runner = new Thread(this);
		runner.start();

	}

	

}