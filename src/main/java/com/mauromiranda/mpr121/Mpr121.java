package com.mauromiranda.mpr121;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.mauromiranda.mpr121.event.PollingEvent;
import com.mauromiranda.mpr121.event.TouchEvent;
import com.mauromiranda.mpr121.listener.PollingListener;
import com.mauromiranda.mpr121.listener.TouchListener;
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

	protected Timer timer = new Timer();

	protected long polling;

	public Mpr121(int address, I2CBus bus, long polling) throws IOException {
		singleListeners = new HashMap<Electrode, List<TouchListener>>();
		touchListeners = new ArrayList<TouchListener>();
		pollingListeners = new ArrayList<PollingListener>();
		// mi collego al
		device = bus.getDevice(address);
		this.polling = polling;
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
		set_register(ELE0_T, TOU_THRESH);
		set_register(ELE0_R, REL_THRESH);

		set_register(ELE1_T, TOU_THRESH);
		set_register(ELE1_R, REL_THRESH);

		set_register(ELE2_T, TOU_THRESH);
		set_register(ELE2_R, REL_THRESH);

		set_register(ELE3_T, TOU_THRESH);
		set_register(ELE3_R, REL_THRESH);

		set_register(ELE4_T, TOU_THRESH);
		set_register(ELE4_R, REL_THRESH);

		set_register(ELE5_T, TOU_THRESH);
		set_register(ELE5_R, REL_THRESH);

		set_register(ELE6_T, TOU_THRESH);
		set_register(ELE6_R, REL_THRESH);

		set_register(ELE7_T, TOU_THRESH);
		set_register(ELE7_R, REL_THRESH);

		set_register(ELE8_T, TOU_THRESH);
		set_register(ELE8_R, REL_THRESH);

		set_register(ELE9_T, TOU_THRESH);
		set_register(ELE9_R, REL_THRESH);

		set_register(ELE10_T, TOU_THRESH);
		set_register(ELE10_R, REL_THRESH);

		set_register(ELE11_T, TOU_THRESH);
		set_register(ELE11_R, REL_THRESH);
		
		set_register(ELE12_T, TOU_THRESH);
		set_register(ELE12_R, REL_THRESH);

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

		set_register(ELE_CFG, (byte) 0x1C);
	}

	private void set_register(byte address, byte value) throws IOException {
		device.write(new byte[] { address, value }, 0, 2);
	}

	/**
	 * Start campioning values
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		setup();
		// avvio il polling ogni 100 millisecondi
		timer.schedule(new PollingTimer(), 1, polling);
	}

	public void stop() {
		timer.purge();
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

		boolean[] touchStates = new boolean[12];

		@Override
		public void run() {
			try {
				byte[] reisters = new byte[42];
				device.read(reisters, 0, 42);
				//notifico la lettura dei registri
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
			}
		}
	}
}
