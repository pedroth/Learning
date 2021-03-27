package teste;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import userGraph.Window;

import javax.swing.*;


public class WordDistribution extends JFrame implements KeyListener, ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Window _wd;
	public TextField _userName;
	public Button _button;
	private Hashtable<Double, Integer> _table; 
	private int _lsize;
	private Enumeration<Double> _enumKey;

	WordDistribution() {
		// Set JFrame title
		super("WordDistribution Test");
		// Set default close operation for JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Set JFrame size
		setSize(600, 600);
		// Make JFrame visible
		setVisible(true);
		this.init();
	}

	public void init(){
		setLayout(null);
		_wd= new Window(this, this.getGraphics(), Color.white);
		_userName = new TextField(10);
		_userName.addKeyListener(this);
		_button = new Button("gaussian");
		_wd.viewWindow(0, 5, -1, 5);
		_wd.setPercHeight(1);
		_wd.setPercWidth(1);
		_wd.setXYDisplacement(0, 0);
		_userName.setBounds(_wd.changeCoordX(2.2),_wd.changeCoordY(4.5), 100, 20);
		_table =  new Hashtable<Double, Integer>();
		_button.addActionListener(this);
		_lsize=0;
		this.add(_button);
		this.add(_userName);
	}
	
	public void paint(Graphics g){
		Double aux1,xmax,ymax,r;
		String str;
		Integer iaux;
		Double kaux;
		xmax = _wd.getXmax();
		ymax = _wd.getYmax();
		
		//set text in right position
		
		_userName.setBounds(_wd.changeCoordX(((_wd.getXmax()- _wd.getXmin())*0.43)), _wd.changeCoordY((_wd.getYmax()- _wd.getYmin())*0.80), 100, 20);
		_button.setBounds(_wd.changeCoordX(((_wd.getXmax()- _wd.getXmin())*0.43))+100, _wd.changeCoordY((_wd.getYmax()- _wd.getYmin())*0.80), 60, 20);
		
		// drawing the axis
		_wd.drawLine(0, 0,xmax,0);
		_wd.drawLine(0, 0, 0,ymax);
		
		
		aux1 = Math.abs(xmax-_wd.getXmin())/10;
		for(int i=0;i<10;i++){
			r=_wd.getXmin() + aux1 * i;
			str= String.format("%.1f", r);
			_wd.drawString(str,r,0);
		}
		
		// ---------------
		
		
		_lsize = _table.size();
		_wd.setDrawColor(Color.red);
		//drawing
		_enumKey = _table.keys(); 
		while(_enumKey.hasMoreElements()){
			kaux = _enumKey.nextElement();
			iaux = _table.get(kaux);
			if(kaux>xmax){
				_wd.viewWindow(_wd.getXmin(),kaux*1.5, _wd.getYmin(), 5*iaux);
				repaint();
			}
			_wd.drawLine((double) kaux, 0, (double) kaux, (double) iaux);
		}
		_wd.setDrawColor(Color.black);
		//-----------------------
	}
	@Override
	public void keyPressed(KeyEvent e) {
		String aux;
		double aux1=0;
		int slen,aux2 = 1;
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			aux = _userName.getText();
			aux1 = hashFunction(aux);
			if(_table.containsKey(aux1)){
				aux2 = _table.get(aux1);
				aux2++;
				_table.remove(aux1);
				_table.put(aux1, aux2);
			}else{
				_table.put(aux1, aux2);
			}
		}
		repaint();
		
	}
	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void drawGaussian(int samples){
		Set<Double> set = _table.keySet();
		Double[] setArray = set.toArray(new Double[0]);
		double w = Math.abs(_wd.getXmax()-_wd.getXmin());
		double step = (double) w/samples;
		double dx = 10E-02;
		double xmin= _wd.getXmin();
		double xmax = _wd.getXmax();
		double cumulative=0;
		double[] aux=new double[samples];
		int n=0;
		for(double x = xmin; x < xmax && n<samples ;x+=step){
			for(int i=0;i<set.size();i++){
				cumulative+= Math.exp(- ((1/dx)*(x - setArray[i])) * ((1/dx)*(x - setArray[i])));
			}
			aux[n]=cumulative;
			n++;
			cumulative=0;
		}

		for(int i=0;i<samples-1;i++){
			_wd.drawLine(xmin, aux[i], xmin+step, aux[i+1]);
			xmin+=step;
		}
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(_button.equals(e.getSource())){
			drawGaussian(1000);
		}
		
	}
	
	public double hashFunction(String aux){
		double aux1=0.0;
		int slen = aux.length();
		for(int i=1;i<slen+1;i++){
			aux1 = aux1 + ((double)aux.charAt(i-1) - 'a')* Math.exp(-i);  
		}
//		for(int i=1;i<slen+1;i++){
//			aux1 = aux1 + ((double)aux.charAt(i-1) - 'a')* Math.pow(7.0,slen-i);  
//		}
		return aux1;
	}

	public static void main(String[] args) {
		new WordDistribution();
	}
}
