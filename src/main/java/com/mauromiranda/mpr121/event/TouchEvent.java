package com.mauromiranda.mpr121.event;

import com.mauromiranda.mpr121.Electrode;

public class TouchEvent {

	Electrode electrode;

	public TouchEvent(Electrode electrode) {
		super();
		this.electrode = electrode;
	}

	public Electrode getElectrode() {
		return electrode;
	}

	public void setElectrode(Electrode electrode) {
		this.electrode = electrode;
	}

}
