import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class AmicMain extends JFrame implements KeyListener {
	AmicPanel screenCanvas;
	Emulator monAmic;
    static final long serialVersionUID = -6339304136266227478L;

    public AmicMain() {
		super("aMIC emulator");
		//setSize(550, 450);
		JPanel panel = (JPanel)getContentPane();
		panel.setPreferredSize(new Dimension(512,512));
		//panel.setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		screenCanvas = new AmicPanel();
		monAmic = new Emulator();
		monAmic.setTargetDisplay(screenCanvas);
		panel.add(screenCanvas);
		
		addKeyListener(this);
		
		pack();
		//setResizable(false);
		setVisible(true);
		monAmic.launch();
	}

	public static void main(String[] arguments) {
		AmicMain mainApp = new AmicMain();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int ks = keyScan(e);
		//System.out.printf("Got %c\n",e.getKeyChar());
		if ((ks&0x80) == 0){
			monAmic.setKeyMatrix(((ks&0xf0)>>>4),(ks&0x0f), true);
		}else {
			monAmic.setKeyPortB(((ks&0xf0)>>>4),(ks&0x0f), true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int ks = keyScan(e);
		if ((ks&0x80) == 0){
			monAmic.setKeyMatrix(((ks&0xf0)>>>4),(ks&0x0f), false);
		} else {
			monAmic.setKeyPortB(((ks&0xf0)>>>4),(ks&0x0f), false);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

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
}
