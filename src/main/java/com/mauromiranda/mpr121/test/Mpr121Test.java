package com.mauromiranda.mpr121.test;

import java.io.FileInputStream;
import java.util.Properties;

import com.jogamp.openal.sound3d.AudioSystem3D;
import com.jogamp.openal.sound3d.Context;
import com.jogamp.openal.sound3d.Device;
import com.jogamp.openal.sound3d.Listener;
import com.jogamp.openal.sound3d.Source;
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
			Mpr121 mpr = new Mpr121(0x5A, bus);

			AudioSystem3D.init();
		
			// create the initial context - this can be collapsed into the init.
			Device device = AudioSystem3D.openDevice(null);
			if(device == null){
				System.out.println("Device is null");
				return;
			}
			Context context = AudioSystem3D.createContext(device);
			AudioSystem3D.makeContextCurrent(context);

			// get the listener object
			Listener listener = AudioSystem3D.getListener();
			listener.setPosition(0, 0, 0);

			// load a source and play it
			final Source source1 = AudioSystem3D.loadSource(new FileInputStream(prop.getProperty("beep1")));
			source1.setPosition(0, 0, 0);
			source1.setLooping(true);

			final Source source2 = AudioSystem3D.loadSource(new FileInputStream(prop.getProperty("beep2")));
			source2.setPosition(0, 0, 0);
			source2.setLooping(true);

			final Source source3 = AudioSystem3D.loadSource(new FileInputStream(prop.getProperty("beep4")));
			source3.setPosition(0, 0, 0);
			source3.setLooping(true);

			final Source source4 = AudioSystem3D.loadSource(new FileInputStream(prop.getProperty("beep4")));
			source4.setPosition(0, 0, 0);
			source4.setLooping(true);

			mpr.addTouchListener(new TouchListener() {

				@Override
				public void touched(TouchEvent e) {
					System.out.println("pin " + e.getElectrode().ordinal() + " was just touched");
					switch (e.getElectrode()) {
					case EL0:
						source1.play();
						break;
					case EL1:
						source2.play();
						break;
					case EL2:
						source3.play();
						break;
					case EL3:
						source4.play();
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
						source1.stop();
						break;
					case EL1:
						source2.stop();
						break;
					case EL2:
						source3.stop();
						break;
					case EL3:
						source4.stop();
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
