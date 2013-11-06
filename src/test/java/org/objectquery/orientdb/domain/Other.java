package org.objectquery.orientdb.domain;

import com.orientechnologies.orient.core.annotation.OVersion;

public class Other {
	private long id;
	private String text;
	private double price;
	@OVersion
	private Long version;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}
