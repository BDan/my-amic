package org.amic;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class PraeMain extends  JFrame implements KeyEventDispatcher { //extends JFrame 
	PraePanel screenCanvas;
	PraeEmu mPrae;
    static final long serialVersionUID = -6339304136266227478L;

    public PraeMain() {
    	makePRAEKeyMap();
	
	}

    public void initialiseFrame(){
    	JFrame mainFrame = new JFrame("PRAE emulator");
		JPanel panel = (JPanel)mainFrame.getContentPane();
		panel.setPreferredSize(new Dimension(768,512));
		//panel.setLayout(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		screenCanvas = new PraePanel();
		mPrae = new PraeEmu();
		mPrae.setTargetDisplay(screenCanvas);
		panel.add(screenCanvas);
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		
		mainFrame.pack();
		//setResizable(false);
		mainFrame.setVisible(true);
		mPrae.initMem();
		System.out.println (System.getProperty("user.dir"));
		//mPrae.loadHex("/mon_z80_00.hex");
		//mPrae.loadHex("/vis_z80_amic.hex");
		mPrae.loadBin("./PRAE_ROM_3_6.bin",0);
		mPrae.launch();

    	
    }

	Map<Integer,Integer> keyMap = new HashMap<>();
    
    
    private void makePRAEKeyMap(){
    	final int[] scancode={
    			'1','2','Q','W',KeyEvent.VK_CONTROL,'A','Z',KeyEvent.VK_SHIFT,
    			'3','4','E','R','S','D','C','X',
    			'5','6','T','Y','F','G','B','V',
    			'7','8','U','I','H','J','M','N',
    			'9','0','O','P','K','L',KeyEvent.VK_ENTER,' '};
    	
    	for (int i=0;i<scancode.length;i++){
    		int data = i/8;
    		int addr=i%8;
    		keyMap.put(scancode[i], addr*16+data);
    	}
    }
    
    private int keyScanPRAE (KeyEvent e){
    	
    	Integer retVal = keyMap.get(e.getKeyCode());
    	if (retVal==null){
    		return -1;
    	}
    	
    	
    	return retVal.intValue();
    }


	
	boolean isSysKey(KeyEvent e){
		if (e.getID()== KeyEvent.KEY_RELEASED){
			switch (e.getKeyCode()){
			case KeyEvent.VK_F5:
				mPrae.reset();
				return true;
			
			case KeyEvent.VK_F6:
				mPrae.nmi();
				return true;
			
			case KeyEvent.VK_F7:
				mPrae.toggleShowTiming();
				return true;
			}
		}
		return false;
		
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (isSysKey(e)){
			return false;
		}
		//System.out.println(""+e.getID());
		int ks = keyScanPRAE(e);
		//System.out.printf("%04X\n",ks);
		if (ks>=0){
	        if (e.getID() == KeyEvent.KEY_PRESSED) {
	        	mPrae.setPraeKeys(ks,true);
	        } else if (e.getID()== KeyEvent.KEY_RELEASED){
	        	mPrae.setPraeKeys(ks,false);
	        }
		}
		
		return false;
	}
	

	public static void main(String[] arguments) {
		PraeMain mainApp = new PraeMain();
		mainApp.initialiseFrame();
	}
	
}
