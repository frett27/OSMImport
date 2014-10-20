package com.poc.osm.regulation;

import java.io.Serializable;

public class MessageRegulatorStatus implements Serializable{
	
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
