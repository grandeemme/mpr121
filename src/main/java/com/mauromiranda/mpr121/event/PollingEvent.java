package com.mauromiranda.mpr121.event;

public class PollingEvent {

	protected int[] values = new int[28];

	public PollingEvent(byte[] registers) {
		super();
		convert(registers);
	}

	public int[] getValues() {
		return values;
	}

	public void setValues(int[] values) {
		this.values = values;
	}

	private void convert(byte[] registers) {
		for (int i = 0; i < 15; i++) {
			values[i] = (registers[i * 2 + 1] & 0x0F) << 8
					| (registers[i * 2] & 0xFF);
		}
		for (int i = 15; i < 28; i++) {
			values[i] = (registers[i + 14] & 0xFF);
		}
	}

}
