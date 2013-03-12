package org.amic;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class AmicMain extends  JFrame implements KeyEventDispatcher { //extends JFrame 
	AmicPanel screenCanvas;
	Emulator monAmic;
    static final long serialVersionUID = -6339304136266227478L;

    public AmicMain() {
	
	}

    public void initialiseFrame(){
    	JFrame mainFrame = new JFrame("aMIC emulator");
		JPanel panel = (JPanel)mainFrame.getContentPane();
		panel.setPreferredSize(new Dimension(512,512));
		//panel.setLayout(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		screenCanvas = new AmicPanel();
		monAmic = new Emulator();
		monAmic.setTargetDisplay(screenCanvas);
		panel.add(screenCanvas);
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		
		mainFrame.pack();
		//setResizable(false);
		mainFrame.setVisible(true);
		monAmic.initMem();
		System.out.println (System.getProperty("user.dir"));
		monAmic.loadHex("/mon_z80_00.hex");
		monAmic.loadHex("/vis_z80_amic.hex");
		monAmic.launch();

    	
    }

	public static void main(String[] arguments) {
		AmicMain mainApp = new AmicMain();
		mainApp.initialiseFrame();
	}

	private int keyScan (KeyEvent e){
		//TODO: for key codes between 0x20 and 0x60 (34 keys) we can use a table
		int c = e.getKeyCode();
		int retVal=0xFF;
		switch(c) {
		case '1': retVal=0x01 ;break;
		case '2': retVal=0x02 ;break;
		case '3': retVal=0x03 ;break;
		case '4': retVal=0x04 ;break;
		case '5': retVal=0x05 ;break;
		case '6': retVal=0x06 ;break;
		case '7': retVal=0x07 ;break;
		case '8': retVal=0x10 ;break;
		case '9': retVal=0x11 ;break;
		case '0': retVal=0x12 ;break;
		case '-': retVal=0x13 ;break;
		case '=': retVal=0x14 ;break;
		case 'Q': retVal=0x21 ;break;
		case 'W': retVal=0x22 ;break;
		case 'E': retVal=0x23 ;break;
		case 'R': retVal=0x24 ;break;
		case 'T': retVal=0x25 ;break;
		case 'Y': retVal=0x26 ;break;
		case 'U': retVal=0x27 ;break;
		case 'I': retVal=0x30 ;break;
		case 'O': retVal=0x31 ;break;
		case 'P': retVal=0x32 ;break;
		case '[': retVal=0x33 ;break; //aMIC does not have a ']' key 
		case ']': retVal=0xFF ;break; // ] = SHIFT + [
		case KeyEvent.VK_QUOTE: retVal=0x52 ;break;
		case '\\': retVal=0x34 ;break;
		case 'A': retVal=0x40 ;break;
		case 'S': retVal=0x41 ;break;
		case 'D': retVal=0x42 ;break;
		case 'F': retVal=0x43 ;break;
		case 'G': retVal=0x44 ;break;
		case 'H': retVal=0x45 ;break;
		case 'J': retVal=0x46 ;break;
		case 'K': retVal=0x47 ;break;
		case 'L': retVal=0x50 ;break;
		case ';': retVal=0x51 ;break;
		case '\'': retVal=0x52 ;break;
		case 'Z': retVal=0x60 ;break;
		case 'X': retVal=0x61 ;break;
		case 'C': retVal=0x62 ;break;
		case 'V': retVal=0x63 ;break;
		case 'B': retVal=0x64 ;break;
		case 'N': retVal=0x65 ;break;
		case 'M': retVal=0x66 ;break;
		case ',': retVal=0x67 ;break;
		case '.': retVal=0x70 ;break;
		case '/': retVal=0x71 ;break;
		case ' ': retVal=0x73 ;break;
		case KeyEvent.VK_BACK_SPACE:retVal = 0x15; break;

		case KeyEvent.VK_ENTER :retVal = 0x54; break;
		case KeyEvent.VK_TAB :retVal = 0x00; break;
		case KeyEvent.VK_DELETE :retVal = 0x36; break;
		case KeyEvent.VK_ESCAPE :retVal = 0x53; break;
		
//		case KeyEvent.VK_F5:
//			monAmic.reset();
//		break;
//		case KeyEvent.VK_F6:
//			monAmic.nmi();
//		break;
//		case KeyEvent.VK_F7:
//			monAmic.toggleShowTiming();
//		break;

		
		case KeyEvent.VK_INSERT:
		case KeyEvent.VK_KP_LEFT:
		case KeyEvent.VK_LEFT: 
		case KeyEvent.VK_KP_DOWN:
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_UP:
		case KeyEvent.VK_UP:
		
		case KeyEvent.VK_SHIFT: retVal = 0x85; break;
		case KeyEvent.VK_CONTROL: retVal = 0x86; break;
		case KeyEvent.VK_CAPS_LOCK:retVal = 0x87; break;
		case KeyEvent.VK_ALT: 
		default: retVal = 0xFF;
	}
		return retVal;

		
	}
	
	void setKeyboadPorts(int key, boolean isDown){
		if ((key&0x80) == 0){
			monAmic.setKeyMatrix(((key&0xf0)>>>4),(key&0x0f), isDown);
		}else {
			monAmic.setKeyPortB(((key&0xf0)>>>4),(key&0x0f), isDown);
		}
		
	}
	
	boolean isSysKey(KeyEvent e){
		if (e.getID()== KeyEvent.KEY_RELEASED){
			switch (e.getKeyCode()){
			case KeyEvent.VK_F5:
				monAmic.reset();
				return true;
			
			case KeyEvent.VK_F6:
				monAmic.nmi();
				return true;
			
			case KeyEvent.VK_F7:
				monAmic.toggleShowTiming();
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
		int ks = keyScan(e);
        if (e.getID() == KeyEvent.KEY_PRESSED) {
        	setKeyboadPorts(ks, true);
        } else if (e.getID()== KeyEvent.KEY_RELEASED){
        	setKeyboadPorts(ks, false);
        }
		
		return false;
	}
}
