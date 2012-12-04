package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.tools;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//DecimalFormat format = new DecimalFormat("0.0E0");
			//double doubleValue = format.parse("4.9E-324").doubleValue();
			//double doubleValue = format.parse("4.9E3").doubleValue();
			double doubleValue = new BigDecimal("4.9E-324").doubleValue();
			System.out.println(" " + doubleValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
