/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package sep.pack;

import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sep.pack.data.Quotes;
import sep.pack.strategy.BackTestStrategy;
import sep.pack.strategy.CubicTransCost;
import sep.pack.strategy.ExpectedProfit;
import sep.pack.strategy.IBDelayCalibrator;
import sep.pack.strategy.TransCost;
import apidemo.util.HtmlButton;


public class MyDemo {
	private JFrame Frame1 = new JFrame("Pair Trading Station");
	private JPanel j2 = new JPanel();
	private JPanel j3 = new JPanel();
	private IBDelayCalibrator calibrator = new IBDelayCalibrator("SPY");
	Engine engine = new Engine();
	HtmlButton b1 = new HtmlButton("Measure Latency"){
		@Override protected void actionPerformed() {
			try{
				t1.selectAll();
				t12.selectAll();
				int interations = 20;
				try{
					interations = Integer.parseInt(t12.getSelectedText());
				}catch(Exception e){
					System.out.println("Incorrect Format...Using 20 iterations");
				}
				calibrator.measureQuotesDelay(interations, t1.getSelectedText(), 5000);
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
	HtmlButton b5 = new HtmlButton("Run BackTesting"){
		@Override protected void actionPerformed() {
			t3.selectAll();
			String configFile = t3.getSelectedText();
			CubicTransCost transCost = Engine.parseTransCost(configFile);
			ExpectedProfit expProfit = Engine.parseExpProfit(configFile);
			final double windowSize = Engine.getWindowSize(configFile);
			BackTestStrategy bts = new BackTestStrategy(transCost, expProfit, "SPY", "SH", windowSize, 1);
			try{
				bts.runSimulation();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	JTextField t1 = new JTextField(10);
	JTextField t12 = new JTextField(2);
	JTextField t2 = new JTextField(40);
	JTextField t3 = new JTextField(40);
	JPanel j1 = new JPanel (new FlowLayout());
	JLabel l1 = new JLabel("Ticker: ");
	JLabel l12 = new JLabel("Iterations: ");
	JLabel l2 = new JLabel("Quotes Saving Directory: ");
	JLabel l3 = new JLabel("Config File Location: ");
	public MyDemo(){
		j1.add(l1);
		j1.add(t1);
		j1.add(l12);
		j1.add(t12);
		j1.add(b1);
		j2.add(l2);
		j2.add(t2);
		j2.add(b2);
		j3.add(l3);
		j3.add(t3);
		j3.add(b3);
		j3.add(b4);
		j3.add(b5);
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
