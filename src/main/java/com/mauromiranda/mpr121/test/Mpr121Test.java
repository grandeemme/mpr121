package com.mauromiranda.mpr121.test;

import java.io.FileInputStream;
import java.util.Properties;

import com.mauromiranda.mpr121.Mpr121;
import com.mauromiranda.mpr121.event.PollingEvent;
import com.mauromiranda.mpr121.event.TouchEvent;
import com.mauromiranda.mpr121.listener.PollingListener;
import com.mauromiranda.mpr121.listener.TouchListener;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

public class Mpr121Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final Properties prop = new Properties();
			prop.load(new FileInputStream("config.properties"));

			String touchThreshold = prop.getProperty("touchThreshold");
			String releaseRhreshold = prop.getProperty("releaseRhreshold");

			System.out.println("Start mpr121");

			final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

			Mpr121 mpr = null;
			if (touchThreshold != null && releaseRhreshold != null) {
				mpr = new Mpr121(0x5A, bus, RaspiPin.GPIO_07, Byte.parseByte(touchThreshold, 16),
						Byte.parseByte(releaseRhreshold, 16));
			} else {
				mpr = new Mpr121(0x5A, bus, RaspiPin.GPIO_07);
			}

			mpr.addTouchListener(new TouchListener() {

				@Override
				public void touched(TouchEvent e) {
					System.out.println("pin " + e.getElectrode().ordinal() + " was just touched");
				}

				@Override
				public void released(TouchEvent e) {
					System.out.println("pin " + e.getElectrode().ordinal() + "  is no longer being touched");
				}
			});

			mpr.addPollingListener(new PollingListener() {

				@Override
				public void registersState(PollingEvent event) {
					// String value = "";
					// for (int b : event.getValues()) {
					// value += "\t" + b;
					// }
					// System.out.println(value);
				}
			});
			mpr.start();
			synchronized (mpr) {
				mpr.wait();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
