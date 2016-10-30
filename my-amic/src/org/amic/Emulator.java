package org.amic;
/*
 * aMIC/PRAE emulator
 * Hardware emulator class
 */
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Emulator implements Z80.Env, Runnable {
	interface Display{
		void updateScreen (Image I);
		Tracer getTracer();
		void showTracer(boolean show);
	}

	private static final BlockingQueue<Integer> SLEEPER = new ArrayBlockingQueue<Integer>(1); //used as a source of precise delays
//Constant values
	final int FPS = 50;
	final int timeFrame = 10000/FPS; //frame duration in ??? unit
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
	boolean showTiming=false;
	
	Thread runner;
	
	public void toggleShowTiming(){
		showTiming = !showTiming;
		displayer.getTracer().setShow(showTiming);
		
	}

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
		    HexFileParser hfp = new HexFileParser();
		     //hfp.parseFile(fName, memo,0);
		    hfp.parseResource(fName, memo,0);
		} catch (Exception e) {
			System.out.println("Can't load ROM file: "+fName);
		    //e.printStackTrace();
		    System.exit(0);
		}
	}
	
	public void loadHexFromResource (String rName){
		
	}
	
	void drawVram() {
		//return;
		final int white = 0xe4e4e4, black = 0x000000;
		for (int addr = 0x4000; addr < 0x6000; addr++) {
			int val = memo[addr];
			int vAddr = addr - 0x4000;
			int row = vAddr / 32;
			int col = (vAddr % 32) * 8;
			int iColor = 0;
			for (int idx = 0; idx < 8; idx++) {
				if (((val << idx) & 0x80) > 0)
					iColor = black;
				else
					iColor = white;
				I.setRGB(col + idx, row, iColor);
			}
		}
	}
	
	public void reset(){
		cpu.reset();
	}
	public void nmi(){
		cpu.nmi();
	}
	
	@Override
	public void run() {

		Thread thisThread = Thread.currentThread();
		speaker = new Audio();
		speaker.setCpuFreq(cpu_freq);
		
		//speaker.test_rect();
		//speaker.test2();
		
		int frameTicks = cpu_freq/FPS;
		cpu.time_limit = frameTicks; 
		cpu.time = 0;
		audioTime = 0;
		long time = System.nanoTime()/100000; //System.currentTimeMillis();
		Tracer tracer = displayer.getTracer();
//		tracer.setValue(0, timeFrame*100);
//		tracer.changePage();
//		tracer.setValue(0, timeFrame*100);
		
		while (runner == thisThread) {
			
			long tStart = System.nanoTime()/1000;
			//long tStart = time*100;
			tracer.setValue(0, timeFrame*100);
			
			tracer.changePage();
			cpu.execute();
			long tCPU = System.nanoTime()/1000;

			tracer.setValue(1, (int)(tCPU-tStart));
			drawVram();
			
			long tSym = System.nanoTime()/1000;
			tracer.setValue(3, (int)(tSym - tCPU));
			displayer.updateScreen(I);

			long tEnd = System.nanoTime()/1000;
			tracer.setValue(2, (int)(tEnd - tSym));
			speaker.ping(cpu.time - audioTime, audio_bit);
			
			time += timeFrame;
			
			cpu.time -= frameTicks;
			audioTime = cpu.time;
			long crTime = System.nanoTime()/100000; //System.currentTimeMillis();
			//tracer.setNumValue(0, (int)(time-crTime));
			if (time > crTime){
				try {
					//Thread.sleep((time - crTime)/10);
					SLEEPER.poll((time - crTime)/10, TimeUnit.MILLISECONDS);
					//SLEEPER.poll(1, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// do nothing
				}
			} else {
				if (time<0){
					System.out.println("time<0");
				}
				Thread.yield();
			}

			//audioTime -= frameTicks;
			
			long finalTime = System.nanoTime()/1000;
			tracer.setValue(4,(int)(finalTime-tEnd));
			//tracer.setNumValue(1,(int)(finalTime/100-time));
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
	}
	public void launch() {
		System.out.println (System.getProperty("user.dir"));
		cpu.reset();
		//cpu.pc(0x8000);
		runner = new Thread(this);
		runner.setPriority(Thread.MAX_PRIORITY);
		runner.start();

	}

	

}