package com.mauromiranda.mpr121.listener;

import com.mauromiranda.mpr121.event.TouchEvent;

public interface TouchListener {

	public void touched(TouchEvent e);
	
	public void released(TouchEvent e);
	

}
