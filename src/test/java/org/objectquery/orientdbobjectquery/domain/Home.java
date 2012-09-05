package org.objectquery.orientdbobjectquery.domain;

import com.orientechnologies.orient.core.annotation.OVersion;

public class Home {
	public enum HomeType {
		KENNEL, HOUSE
	};

	private Long id;
	private String address;
	private HomeType type;
	private int weight;
	private double price;
	@OVersion
	private long version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public HomeType getType() {
		return type;
	}

	public void setType(HomeType type) {
		this.type = type;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
