package teste;
import java.applet.Applet;
import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;


public class TxtExample extends Applet implements KeyListener, FocusListener {
	private TextField userName;
	private TextArea in;
	private TextArea out;
	private String name;
	private int age;
	private double weight, height ;
	private StringTokenizer st; 
	
	
	public void init(){
		this.requestFocus();
		this.setFocusable(true);
		setLayout(null);
		userName = new TextField("user Name",10);
		in = new TextArea(100,50);
		out = new TextArea(100,50);
		userName.setBounds(0, 20, 100, 20);
		out.setBounds(0,80,400,100);
		in.setBounds(0,300, 400, 100);
		add(in);
		add(out);
		add(userName);
		in.addKeyListener(this);
		out.addFocusListener(this);
	}
	
	public void paint(Graphics g){
		this.getGraphics().drawString("in", 0, 300);// g.draw... is the same
		this.getGraphics().drawString("out", 0, 80);
		//this.getGraphics().draw3DRect(0, 20, 200, 50, true);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		String s= userName.getText() + " ";
		s = s.concat(in.getText());
		out.setText(s);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusGained(FocusEvent e) {
		if(e.getSource().equals(out)){
			st=new StringTokenizer(out.getText());
			if(st.hasMoreTokens()){
				userName.setText(st.nextToken());
			}
		}
		
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	


}