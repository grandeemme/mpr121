package com.mauromiranda.mpr121.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.mauromiranda.mpr121.Mpr121;
import com.mauromiranda.mpr121.event.PollingEvent;
import com.mauromiranda.mpr121.event.TouchEvent;
import com.mauromiranda.mpr121.listener.PollingListener;
import com.mauromiranda.mpr121.listener.TouchListener;
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

			System.out.println("Start mpr121");

			final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
			Mpr121 mpr = new Mpr121(0x5A, bus, 100);

			final Clip clip1 = AudioSystem.getClip();
			clip1.open(AudioSystem.getAudioInputStream(new File(prop.getProperty("beep1"))));

			final Clip clip2 = AudioSystem.getClip();
			clip2.open(AudioSystem.getAudioInputStream(new File(prop.getProperty("beep2"))));

			mpr.addTouchListener(new TouchListener() {

				@Override
				public void touched(TouchEvent e) {
					System.out.println("pin " + e.getElectrode().ordinal() + " was just touched");
					switch (e.getElectrode()) {
					case EL0:
						clip1.start();
						break;
					case EL1:
						clip2.start();
						break;
					case EL2:

						break;
					case EL3:

						break;
					default:
						break;
					}

				}

				@Override
				public void released(TouchEvent e) {
					System.out.println("pin " + e.getElectrode().ordinal() + "  is no longer being touched");

					switch (e.getElectrode()) {
					case EL0:
						clip1.stop();
						clip1.setFramePosition(0);
						break;
					case EL1:
						clip2.stop();
						clip2.setFramePosition(0);
						break;
					case EL2:

						break;
					case EL3:

						break;
					default:
						break;
					}
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
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
