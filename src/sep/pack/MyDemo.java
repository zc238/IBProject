/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package sep.pack;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sep.pack.strategy.IBDelayCalibrator;
import apidemo.util.HtmlButton;


public class MyDemo {
	private JFrame Frame1 = new JFrame("Pair Trader Station");
	private JPanel j2 = new JPanel();
	private JPanel j3 = new JPanel();
	private IBDelayCalibrator calibrator = new IBDelayCalibrator("SPY");
	Engine engine = new Engine();
	HtmlButton b1 = new HtmlButton("Measure Latency"){
		@Override protected void actionPerformed() {
			try{
				t1.selectAll();
				calibrator.measureQuotesDelay(100, t1.getSelectedText(), 3000);
			}catch(Exception e){}
		}
	};
	HtmlButton b2 = new HtmlButton("Request Quotes"){
		@Override protected void actionPerformed() {
			try{
				t2.selectAll();
				System.out.println(t2.getSelectedText());
				engine.startRecordingData(t2.getSelectedText());
			}catch(Exception e){}
		}
	};
	HtmlButton b3 = new HtmlButton("Start Strategy"){
		@Override protected void actionPerformed() {
			try{
				t3.selectAll();
				engine.startStrategy(t3.getSelectedText());
			}catch(Exception e){}
		}
	};
	HtmlButton b4 = new HtmlButton("Terminate Connection"){
		@Override protected void actionPerformed() {
			calibrator.shutDown();
			engine.shutDown();
		}
	};
	JTextField t1 = new JTextField(10);
	JTextField t2 = new JTextField(40);
	JTextField t3 = new JTextField(40);
	JPanel j1 = new JPanel (new FlowLayout());
	JLabel l1 = new JLabel("Ticker: ");
	JLabel l2 = new JLabel("Quotes Saving Directory: ");
	JLabel l3 = new JLabel("Config File Location: ");
	public MyDemo(){
		j1.add(l1);
		j1.add(t1);
		j1.add(b1);
		j2.add(l2);
		j2.add(t2);
		j2.add(b2);
		j3.add(l3);
		j3.add(t3);
		j3.add(b3);
		j3.add(b4);
		Frame1.add(j1);
		Frame1.add(j2);
		Frame1.add(j3);
		Frame1.setLayout(new FlowLayout());
		Frame1.setSize(1000,350);
		Frame1.setVisible(true);
		Frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) throws InterruptedException {
		new MyDemo();
//		IBDelayCalibrator calibrator = new IBDelayCalibrator("SPY");
//		calibrator.measureQuotesDelay(100, "SPY");

//		int option = Integer.parseInt(args[0]);
//		Engine engine = new Engine(args[1]);
//		engine.startStrategy();
//		switch(option){
//			case 1: engine.startRecordingData(); break;
//			case 2: engine.startStrategy(); break;
//			default: System.out.println("Invalid Option...Exiting.");
//		}
	}
}
