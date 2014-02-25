/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package sep.pack;

import java.util.List;

import sep.pack.strategy.IBDelayCalibrator;


public class MyDemo {
	public static void changeList(List<Double> d){
		if (d.size() > 2){
			d.remove(0);
			changeList(d);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		IBDelayCalibrator calibrator = new IBDelayCalibrator("SPY");
		calibrator.measureQuotesDelay(100, "SPY");

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
