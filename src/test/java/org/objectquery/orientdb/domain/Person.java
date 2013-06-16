package org.objectquery.orientdb.domain;

import java.util.List;

import com.orientechnologies.orient.core.annotation.OVersion;

public class Person {
	private Long id;
	private String name;
	private List<Person> friends;
	private Person mum;
	private Person dud;
	private Home home;
	private Dog dog;
	@OVersion
	private Long version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Person> getFriends() {
		return friends;
	}

	public void setFriends(List<Person> friends) {
		this.friends = friends;
	}

	public Person getMum() {
		return mum;
	}

	public void setMum(Person mum) {
		this.mum = mum;
	}

	public Person getDud() {
		return dud;
	}

	public void setDud(Person dud) {
		this.dud = dud;
	}

	public Home getHome() {
		return home;
	}

	public void setHome(Home home) {
		this.home = home;
	}

	public Dog getDog() {
		return dog;
	}

	public void setDog(Dog dog) {
		this.dog = dog;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
