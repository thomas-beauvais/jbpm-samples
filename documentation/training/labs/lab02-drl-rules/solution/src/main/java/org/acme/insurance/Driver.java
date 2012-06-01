package org.acme.insurance;

import java.io.Serializable;

/**
 * This represents obviously a driver who is applying for an insurance Policy.
 *
 */
public class Driver implements Serializable
{
    private static final long serialVersionUID = 1L;
	private String driverName = "Bill Smith";
	private Integer age = new Integer(0);
	private String ssn;	
	private String dlNumber;
	private Integer numberOfAccidents = new Integer(0);
	private Integer numberOfTickets = new Integer(0);	
	private Integer  creditScore = new Integer(0);

	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getSsn() {
		return ssn;
	}
	public void setSsn(String ssn) {
		this.ssn = ssn;
	}
	public String getDlNumber() {
		return dlNumber;
	}
	public void setDlNumber(String dlNumber) {
		this.dlNumber = dlNumber;
	}
	public Integer getNumberOfAccidents() {
		return numberOfAccidents;
	}
	public void setNumberOfAccidents(Integer numberOfAccidents) {
		this.numberOfAccidents = numberOfAccidents;
	}
	public Integer getNumberOfTickets() {
		return numberOfTickets;
	}
	public void setNumberOfTickets(Integer numberOfTickets) {
		this.numberOfTickets = numberOfTickets;
	}
	public Integer getCreditScore() {
		return creditScore;
	}
	public void setCreditScore(Integer creditScore) {
		this.creditScore = creditScore;
	}
	

	
}
