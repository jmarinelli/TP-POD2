package ar.edu.itba.pod.mmxivii.sube.service;

import java.util.Date;

import com.google.gson.Gson;

class Operation {
	private static Gson gson = new Gson();
	
	static Operation fromJson(String json) {
		return gson.fromJson(json, Operation.class);
	}
	
	private Double initial;
	private Double current;
	private Date lastUpdated;
	
	Operation(Double initial, Double current, Date lastUpdated) {
		super();
		this.initial = initial;
		this.current = current;
		this.lastUpdated = lastUpdated;
	}
	
	Double diff() {
		return this.current - this.initial;
	}
	
	void reset() {
		this.initial = this.current;
		this.lastUpdated = new Date();
	}
	
	Date getLastUpdated() {
		return this.lastUpdated;
	}
	
	Double getInitial() {
		return this.initial;
	}
	
	Double getCurrent() {
		return this.current;
	}
	
	Double update(Double amount) throws InvalidAmountException {
		Double newBalance = this.current + amount;
		if (newBalance > 100 || newBalance < 0)
			throw new InvalidAmountException();
		this.current += amount;
		this.lastUpdated = new Date();
		return this.current;
	}
	
	String asJson() {
		return gson.toJson(this);
	}
}