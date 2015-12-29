package com.mauromiranda.mpr121;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mauromiranda.mpr121.event.PollingEvent;
import com.mauromiranda.mpr121.event.TouchEvent;
import com.mauromiranda.mpr121.listener.PollingListener;
import com.mauromiranda.mpr121.listener.TouchListener;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

/**
 * Utility class for communicating with the Mpr121 through the I2C channel
 * 
 * @author Mauro Miranda
 * 
 */
public class Mpr121 implements Constants {

	protected Map<Electrode, List<TouchListener>> singleListeners;

	protected List<TouchListener> touchListeners;

	protected List<PollingListener> pollingListeners;

	protected I2CDevice device;

	protected GpioController gpio;

	protected GpioPinDigitalInput interrupt;

	boolean[] touchStates = new boolean[12];

	protected Lock lock = new ReentrantLock();

	protected Pin gpioInterrupt;

	protected byte touchThreshold = TOU_THRESH;
	protected byte releaseRhreshold = REL_THRESH;

	public Mpr121(int address, I2CBus bus, Pin gpioInterrupt, byte touchThreshold, byte releaseRhreshold)
			throws IOException {
		this(address, bus, gpioInterrupt);
		System.out.println(
				"Mpr121 Strat with: touchThreshold " + touchThreshold + " releaseRhreshold " + releaseRhreshold);
		this.touchThreshold = touchThreshold;
		this.releaseRhreshold = releaseRhreshold;

	}

	public Mpr121(int address, I2CBus bus, Pin gpioInterrupt) throws IOException {
		singleListeners = new HashMap<Electrode, List<TouchListener>>();
		touchListeners = new ArrayList<TouchListener>();
		pollingListeners = new ArrayList<PollingListener>();
		// mi collego al
		device = bus.getDevice(address);
		this.gpioInterrupt = gpioInterrupt;
	}

	/**
	 * Start campioning values
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		System.out.println("Mpr121 initialize");
		setup();
		// create gpio controller
		gpio = GpioFactory.getInstance();

		// provision gpio pin #02 as an input pin with its internal pull down
		// resistor enabled
		interrupt = gpio.provisionDigitalInputPin(gpioInterrupt, PinPullResistance.PULL_DOWN);

		// create and register gpio pin listener
		interrupt.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				// display pin state on console
				System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());

				try {
					lock.tryLock(100, TimeUnit.MILLISECONDS);
					byte[] reisters = new byte[42];
					device.read(reisters, 0, 42);
					// notifico la lettura dei registri
					notifyPolling(reisters);

					byte LSB = reisters[0];
					byte MSB = reisters[1];
					//
					int touched = ((MSB << 8) | LSB);
					// controllo i primi 8
					for (int i = 0; i < 12; i++) {
						if ((touched & (1 << i)) != 0x00) {
							if (!touchStates[i]) {
								// pin i was just touched
								notifyTouch(Electrode.values()[i]);
							} else {
								// pin i is still being touched
							}
							touchStates[i] = true;
						} else {
							if (touchStates[i]) {
								notifyRelease(Electrode.values()[i]);
								// pin i is no longer being touched
							}
							touchStates[i] = false;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}

		});

		System.out.println("Mpr121 initialized");
	}

	public void setup() throws IOException {
		set_register(ELE_CFG, (byte) 0x00);

		// Section A - Controls filtering when data is > baseline.
		set_register(MHD_R, (byte) 0x01);
		set_register(NHD_R, (byte) 0x01);
		set_register(NCL_R, (byte) 0x00);
		set_register(FDL_R, (byte) 0x00);

		// Section B - Controls filtering when data is < baseline.
		set_register(MHD_F, (byte) 0x01);
		set_register(NHD_F, (byte) 0x01);
		set_register(NCL_F, (byte) 0xFF);
		set_register(FDL_F, (byte) 0x02);

		// Section C - Sets touch and release thresholds for each electrode
		set_register(ELE0_T, touchThreshold);
		set_register(ELE0_R, releaseRhreshold);

		set_register(ELE1_T, touchThreshold);
		set_register(ELE1_R, releaseRhreshold);

		set_register(ELE2_T, touchThreshold);
		set_register(ELE2_R, releaseRhreshold);

		set_register(ELE3_T, touchThreshold);
		set_register(ELE3_R, releaseRhreshold);

		set_register(ELE4_T, touchThreshold);
		set_register(ELE4_R, releaseRhreshold);

		set_register(ELE5_T, touchThreshold);
		set_register(ELE5_R, releaseRhreshold);

		set_register(ELE6_T, touchThreshold);
		set_register(ELE6_R, releaseRhreshold);

		set_register(ELE7_T, touchThreshold);
		set_register(ELE7_R, releaseRhreshold);

		set_register(ELE8_T, touchThreshold);
		set_register(ELE8_R, releaseRhreshold);

		set_register(ELE9_T, touchThreshold);
		set_register(ELE9_R, releaseRhreshold);

		set_register(ELE10_T, touchThreshold);
		set_register(ELE10_R, releaseRhreshold);

		set_register(ELE11_T, touchThreshold);
		set_register(ELE11_R, releaseRhreshold);

		set_register(ELE12_T, touchThreshold);
		set_register(ELE12_R, releaseRhreshold);

		// Section D
		// Set the Filter Configuration
		// Set ESI2
		set_register(FIL_CFG, (byte) 0x04);

		// Section E
		// Electrode Configuration
		// Set ELE_CFG to 0x00 to return to standby mode
		set_register(ELE_CFG, (byte) 0x0C); // Enables all 12 Electrodes

		// Section F
		// Enable Auto Config and auto Reconfig
		/*
		 * set_register( ATO_CFG0, 0x0B); set_register( ATO_CFGU, 0xC9); // USL
		 * = (Vdd-0.7)/vdd*256 = 0xC9 @3.3V set_register( ATO_CFGL, 0x82); //
		 * LSL = 0.65*USL = 0x82 @3.3V set_register( ATO_CFGT, 0xB5);
		 */// Target = 0.9*USL = 0xB5 @3.3V

	}

	private void set_register(byte address, byte value) throws IOException {
		device.write(new byte[] { address, value }, 0, 2);
	}

	public void stop() {
		gpio.removeAllListeners();
	}

	public void addTouchListener(TouchListener l) {
		touchListeners.add(l);
	}

	public void addPollingListener(PollingListener l) {
		pollingListeners.add(l);
	}

	public void addSingleTouchListener(Electrode e, TouchListener l) {
		List<TouchListener> li = singleListeners.get(e);
		if (li == null) {
			li = new ArrayList<TouchListener>();
			singleListeners.put(e, li);
		}
		li.add(l);
	}

	protected void notifyTouch(Electrode electrode) {
		TouchEvent event = new TouchEvent(electrode);
		for (TouchListener l : touchListeners) {
			l.touched(event);
		}
	}

	protected void notifyRelease(Electrode electrode) {
		TouchEvent event = new TouchEvent(electrode);
		for (TouchListener l : touchListeners) {
			l.released(event);
		}
	}

	protected void notifyPolling(byte[] registers) {
		PollingEvent e = new PollingEvent(registers);
		for (PollingListener l : pollingListeners) {
			l.registersState(e);
		}
	}

	protected class PollingTimer extends TimerTask {

		@Override
		public void run() {

		}
	}
}
