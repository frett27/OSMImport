package com.poc.osm.regulation;

import java.io.Serializable;

import akka.dispatch.ControlMessage;

public class MessageRegulatorStatus implements Serializable, ControlMessage{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7778042995972095093L;
	
	private double newVelocity = Double.NaN;
	
	public MessageRegulatorStatus()
	{
		
	}
	
	public void setNewVelocity(double newVelocity) {
		this.newVelocity = newVelocity;
	}
	
	public double getNewVelocity() {
		return newVelocity;
	}
	
	
	
}
