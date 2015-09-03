package com.rfid;

import java.util.Random;

public class Calculater {

	public static double calDistanceByRSSI (int RSSI){
		int iRssi = Math.abs(RSSI);  
		int A;//the path loss at the reference distance 1m,
		double r;//the path loss exponent; 2.0 in Vacuum, 2.2 in office. (1~5)
		double distance;// distance in meters
		double Xg;//a normal (or Gaussian) random variable with zero mean, reflecting the attenuation (in decibel) caused by flat fading
		Xg = 8.7;
		A = 53;
		r = 2.2;
	    //without Gaussian
		double power = (iRssi-A)/(10*r);
		
		//with Gaussian
//		Random ran = new Random();
//	    double power = (iRssi-A-ran.nextGaussian()*Xg)/(10*r);  
	    
	    distance = Math.pow(10, power);
		return distance;
	}
}
