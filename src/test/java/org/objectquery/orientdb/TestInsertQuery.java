package org.objectquery.orientdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectquery.InsertQuery;
import org.objectquery.generic.GenericInsertQuery;
import org.objectquery.orientdb.domain.Home;
import org.objectquery.orientdb.domain.Other;
import org.objectquery.orientdb.domain.Person;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class TestInsertQuery {

	private OObjectDatabaseTx db;

	@Before
	public void beforeTest() {
		db = OrientDBTestHelper.getPool().acquire();
		db.begin();
	}

	@Test
	public void testSimpleInsert() {
		InsertQuery<Other> ip = new GenericInsertQuery<Other>(Other.class);
		ip.set(ip.target().getText(), "test");
		OrientDBObjectQuery.execute(ip, db);
	}

	@Test
	public void testSimpleInsertGen() {
		InsertQuery<Person> ip = new GenericInsertQuery<Person>(Person.class);
		ip.set(ip.target().getName(), "test");
		OrientDBQueryGenerator q = OrientDBObjectQuery.orientdbGenerator(ip);
		assertEquals("insert into Person (name)values(:name)", q.getQuery());
	}

	@Test
	public void testMultipInsert() {
		InsertQuery<Home> ip = new GenericInsertQuery<Home>(Home.class);
		ip.set(ip.box(ip.target().getPrice()), 4D);
		ip.set(ip.box(ip.target().getWeight()), 6);
		boolean res = OrientDBObjectQuery.execute(ip, db);
		assertTrue(res);
	}

	@Test
	public void testMultipInsertGen() {
		InsertQuery<Home> ip = new GenericInsertQuery<Home>(Home.class);
		ip.set(ip.box(ip.target().getPrice()), 4D);
		ip.set(ip.box(ip.target().getWeight()), 6);
		OrientDBQueryGenerator q = OrientDBObjectQuery.orientdbGenerator(ip);
		assertEquals("insert into Home (price,weight)values(:price,:weight)", q.getQuery());
	}

	@Test
	public void testDupicateFieldInsert() {
		InsertQuery<Other> ip = new GenericInsertQuery<Other>(Other.class);
		ip.set(ip.box(ip.target().getPrice()), 4D);
		ip.set(ip.target().getText(), "aa");
		boolean res = OrientDBObjectQuery.execute(ip, db);
		assertTrue(res);
	}

	@Test
	public void testNestedInsert() {
		InsertQuery<Person> ip = new GenericInsertQuery<Person>(Person.class);
		ip.set(ip.target().getDud().getName(), "test");
		OrientDBQueryGenerator q = OrientDBObjectQuery.orientdbGenerator(ip);
		assertEquals("insert into Person (dud.name)values(:dudname)", q.getQuery());
	}

	@After
	public void afterTest() {
		if (db != null) {
			db.commit();
			db.close();
		}
		db = null;
	}

}
