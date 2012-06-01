package org.acme.insurance;

import java.io.Serializable;
import java.util.Date;

/**
 * This represents a policy that a driver is applying for. 
 * 
 */
public class Policy implements Serializable
{
    private static final long serialVersionUID = 1L;
  	private Date requestDate;
    private String policyType = "AUTO";
	private int vehicleYear;
	private int price;
	
	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public String getPolicyType() {
		return policyType;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}

	public int getVehicleYear() {
		return vehicleYear;
	}

	public void setVehicleYear(int vehicleYear) {
		this.vehicleYear = vehicleYear;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	private Driver driver = new Driver();

	
}
