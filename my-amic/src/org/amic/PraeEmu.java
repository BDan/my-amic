package org.amic;

/*
 * aMIC/PRAE emulator
 * Hardware emulator class
 */
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class PraeEmu implements Z80.Env, Runnable {
	interface Display {
		void updateScreen(Image I);

		Tracer getTracer();

		void showTracer(boolean show);
	}

	
	// Constant values
	final int FPS = 50;
	final int timeFrame = 10000 / FPS; // frame duration in 0.1 ms
	private final int AD_ROM = 0x4000;
	private final int cpu_freq = 2500000; // Hz
	//private final int cpu_freq = 250000; //Hz
	
	
	long audioTime;

	// Hardware modules
	public int memo[] = new int[0x10000];
	Audio speaker;// = new Audio();
	public int audio_bit = 0;
	boolean keyed = true;

	public int portB = 0; // input
	public int keyMatrix[] = new int[8]; // mapped on portA
	private static final int scrW = 256;
	private static final int scrH = 256;
	BufferedImage I = new BufferedImage(scrW, scrH, BufferedImage.TYPE_INT_RGB);

	public final Z80 cpu = new Z80(this);
	Display displayer = null;
	boolean showTiming = false;

	Thread runner;

	public void toggleShowTiming() {
		showTiming = !showTiming;
		displayer.getTracer().setShow(showTiming);

	}

	public void setTargetDisplay(Display displayer) {
		this.displayer = displayer;
	}

	// Z80.Env methods
	public int m1(int pc, int ir) {
		// System.out.printf("M1 @[%02X] -> %02X\n", pc,memo[pc]);

		return memo[pc];
	};

	public int mem(int addr) {
		// System.out.printf("Mem read @[%02X] -> %02X\n", addr,memo[addr]);
		return memo[addr];
	};

	public void mem(int addr, int v) {
		// System.out.printf("%02X -> [%02X]\n", addr,v);
		if (addr >= AD_ROM) {
			memo[addr] = v;
		}
	};

	int counter = 0;

	// eight 5 bit values, corresponding to the physical keyboard of PRAE
	int[] keys = new int[8];

	/**
	 * Sets the virtual keyboard matrix when a key event is received from host
	 * 
	 * @param code
	 *            in format hl h=row, l=column. (columns are connected to
	 *            address bus A8-A15, rows to data bus D0-D5)
	 * @param down
	 *            true is key was pressed, false when released
	 */
	public void setPraeKeys(int code, boolean down) {

		int l = code & 0xf;
		int h = (code & 0xf0) >> 4;
		// System.out.println(""+l+" "+h);
		int mask = 1 << l;
		if (down) {
			this.keys[h] |= mask;
		} else {
			mask = ~mask;
			this.keys[h] &= mask;
		}
	}

	/**
	 * Calculates the value read from the keyboard when the higher byte of the
	 * address bus is addr based on the virtual keyboard matrix in "keys"
	 * 
	 * @param addr
	 * @return
	 */
	public int keyboard(int addr) {
		int retVal = 0x1f;
		for (int i = 0; i < 8; i++) {
			//int tmp = 0x1f;
			//int b = 1;
			if ((addr & 1) == 0) {
				int k = this.keys[i];
				 
				retVal &= ~k;
			}
			addr >>= 1;
		}
		return retVal;
	}

	public int in(int port) {
		// System.out.printf("%04X: IN <-%02X\n",cpu.pc(), port&0xffff);
		int retVal = 0xff;

		if ((port & 0xff) == 0x80) {
			int hi = (port & 0xff00) >>> 8;
			if (hi != 0xFF) {
				retVal = keyboard(hi);
			}
			int cass = Math.random()>0.5?0:0x80;
			retVal |= cass;
			
		}
		if (retVal!=0x1f){
			//System.out.printf("%04X: IN <-%04X %02x\n",cpu.pc(), port&0xffff,retVal);
			
		}
		
		return retVal;
	};

	public void out(int port, int v) {
		//System.out.printf("%04X: OUT  %02X->%02X\n", cpu.pc(), port , v);
		if ((port & 0xff) == 0x80) {

			int newAudioBit = (v & 0x80) == 0 ? 0 : 1;
			if (audio_bit != newAudioBit) {
				speaker.ping(cpu.time - audioTime, audio_bit);
				//System.out.print("--");
				audioTime = cpu.time;
				audio_bit = newAudioBit;

			}

			

		}

	};

	public int mem16(int addr) {
		int retVal = memo[addr] | memo[addr + 1] << 8;
		// System.out.printf("Mem16 read @[%02X] -> %02X\n", addr, retVal);
		return retVal;
	};

	public void mem16(int addr, int v) {
		if (addr >= AD_ROM) {
			memo[addr] = v & 0xff;
			memo[addr + 1] = v >>> 8;
		}
		// System.out.printf("16 bit write: %02X -> [%02X]\n", addr,v);

	};

	public int halt(int n, int ir) {

		System.out.printf("HALT received after %d ticks =%f sec\n", cpu.time, 1.0 * cpu.time / cpu_freq);
		cpu.time_limit = 0;
		return n;
	};

	public void loadMem(int[] data, int offset) {
		for (int i = 0; i < data.length; i++) {
			memo[i + offset] = data[i];
		}

	}

	public void initMem() {
		Arrays.fill(memo, 0xFF);
	}

	public void loadHex(String fName) {
		try {
			HexFileParser hfp = new HexFileParser();
			// hfp.parseFile(fName, memo,0);
			hfp.parseResource(fName, memo, 0);
		} catch (Exception e) {
			System.out.println("Can't load ROM file: " + fName);
			// e.printStackTrace();
			System.exit(0);
		}
	}

	public void loadBin(String fName, int addr) {
		Path path = Paths.get(fName).toAbsolutePath().normalize();
		System.out.println(path);
		try {
			byte[] content = Files.readAllBytes(path);
			for (int i = 0; i < content.length; i++) {
				memo[i + addr] = content[i] & 0xFF;
			}
		} catch (IOException e) {
			System.out.println("Can't load ROM file: " + fName);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void loadHexFromResource(String rName) {

	}

	void drawVram() {
		// return;
		final int white = 0xe4e4e4, black = 0x000000;
		final int VRAM_START = 0xE000;
		final int VRAM_LEN = 0x2000;
		for (int addr = VRAM_START; addr < VRAM_START + VRAM_LEN; addr++) {
			int val = memo[addr];
			int vAddr = addr - VRAM_START;
			int row = vAddr / 32;
			int col = (vAddr % 32) * 8;
			int iColor = 0;
			for (int idx = 0; idx < 8; idx++) {
				if (((val << idx) & 0x80) > 0)
					iColor = black;
				else
					iColor = white;

				I.setRGB((col + idx), row, iColor);
			}
		}
	}

	public void reset() {
		cpu.reset();
	}

	public void nmi() {
		cpu.nmi();
	}

	@Override
	public void run() {

		Thread thisThread = Thread.currentThread();
		speaker = new Audio();
		speaker.setCpuFreq(cpu_freq);

		// speaker.test_rect();
		// speaker.test2();
		
		///timeFrame = 10000/FPS;

		int frameTicks = cpu_freq / FPS;
		cpu.time_limit = frameTicks;
		cpu.time = 0;
		audioTime = 0;
		//long time = System.nanoTime() / 100000; // 
		long time = System.currentTimeMillis();
		Tracer tracer = displayer.getTracer();
		// tracer.setValue(0, timeFrame*100);
		// tracer.changePage();
		// tracer.setValue(0, timeFrame*100);

		long finalTime = System.nanoTime() / 100000;
		
		while (runner == thisThread) {
			//memo[0x4020]=1;

			long tStart = System.nanoTime() / 100000;
			
			tracer.setValue(0, timeFrame);
			

			tracer.changePage();
			cpu.execute();
			long tCPU = System.nanoTime() / 100000;

			tracer.setValue(1, (int) (tCPU - tStart));
			drawVram();

			long tSym = System.nanoTime() / 100000;
			tracer.setValue(2, (int) (tSym - tCPU));
			displayer.updateScreen(I);

			long tEnd = System.nanoTime() / 100000;
			tracer.setValue(3, (int) (tEnd - tSym));
			speaker.ping(cpu.time - audioTime, audio_bit);

			time += timeFrame/10;

			cpu.time -= frameTicks;
			audioTime = cpu.time;
			//long crTime = System.nanoTime() / 100000; // 
			long crTime = System.currentTimeMillis();
			//System.out.println("crTime: "+crTime+" time: "+time+" "+(time-crTime));
			
			if (time > crTime) {
				try {
					//Thread.sleep((time - crTime)/10);
					
					long sleepTime_ms =(time - crTime) ;
					//Thread.sleep(sleepTime_ms);
					//Thread.sleep(0,1);
					java.util.concurrent.TimeUnit.MILLISECONDS.sleep(sleepTime_ms);
					
					
					
					//tracer.setValue(4, (int) (sleepTime_ms));
					//SLEEPER.poll(sleepTime_ms, TimeUnit.MILLISECONDS);
					
					
					//SLEEPER.poll(3, TimeUnit.MILLISECONDS);
					// SLEEPER.poll(1, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// do nothing
				}
			} else {
				//System.out.println("time<crTime");
				if (time < 0) {
					System.out.println("time<0");
				}
				//Thread.yield();
			}

//			while(System.nanoTime() / 100000<time){
//				int a =1;
//				a=a+1;
//			}
			
			// audioTime -= frameTicks;

			finalTime = System.nanoTime() / 100000;
			if (time > crTime){
				tracer.setValue(4, (int) (finalTime - tEnd));
				tracer.setValue(5, 0);
			} else {
				tracer.setValue(5, (int) (finalTime - tEnd));
				tracer.setValue(4, 0);
			}
			//tracer.setValue(5, 0);
			
			
			// tracer.setNumValue(1,(int)(finalTime/100-time));
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

	/**
	 * This is a hack which allows Thread.sleep to have a resolution of 1..2 ms instead of 10..15m in Windows.
	 */
	private void bgThread(){
		Runnable r = new Runnable(){

			@Override
			public void run() {
				while(true){
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {}
				}

			}
		};
		Thread th = new Thread(r);
		th.start();
	}
	public void launch() {
		System.out.println(System.getProperty("user.dir"));
		cpu.reset();
		// cpu.pc(0x8000);
		bgThread();
		runner = new Thread(this);
		runner.setPriority(Thread.MAX_PRIORITY);
		runner.start();

	}

}