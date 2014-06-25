package org.objectquery.orientdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectquery.SelectQuery;
import org.objectquery.UpdateQuery;
import org.objectquery.generic.GenericSelectQuery;
import org.objectquery.generic.GenericUpdateQuery;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.orientdb.domain.Home;
import org.objectquery.orientdb.domain.Other;
import org.objectquery.orientdb.domain.Person;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class TestUpdateQuery {

	private OObjectDatabaseTx db;

	@Before
	public void beforeTest() {
		db = OrientDBTestHelper.getPool().acquire();
		db.begin();
	}

	@Test
	public void testSimpleUpdate() {
		Other other = new Other();
		other.setText("old-address");
		db.save(other);

		SelectQuery<Other> q = new GenericSelectQuery<Other, Object>(Other.class);
		q.eq(q.target().getText(), "old-address");
		List<Other> ots = OrientDBObjectQuery.execute(q, db);
		assertFalse(ots.isEmpty());

		UpdateQuery<Other> query = new GenericUpdateQuery<Other>(Other.class);
		query.set(query.target().getText(), "new-address");
		query.eq(query.target().getText(), "old-address");
		int res = OrientDBObjectQuery.execute(query, db);
		assertEquals(1, res);
	}

	@Test
	public void testSimpleUpdateGen() {
		UpdateQuery<Home> query = new GenericUpdateQuery<Home>(Home.class);
		query.set(query.target().getAddress(), "new-address");
		query.eq(query.target().getAddress(), "old-address");
		OrientDBQueryGenerator q = OrientDBObjectQuery.orientdbGenerator(query);
		assertEquals("update Home set address = :address where address  =  :address1", q.getQuery());
	}

	@Test(expected = ObjectQueryException.class)
	public void testSimpleNestedUpdate() {
		UpdateQuery<Person> query = new GenericUpdateQuery<Person>(Person.class);
		query.set(query.target().getHome().getAddress(), "new-address");
		query.eq(query.target().getHome().getAddress(), "old-address");
		OrientDBObjectQuery.execute(query, db);
	}

	@Test(expected = ObjectQueryException.class)
	public void testSimpleNestedUpdateGen() {
		UpdateQuery<Person> query = new GenericUpdateQuery<Person>(Person.class);
		query.set(query.target().getHome().getAddress(), "new-address");
		query.eq(query.target().getHome().getAddress(), "old-address");

		OrientDBQueryGenerator q = OrientDBObjectQuery.orientdbGenerator(query);
		assertEquals("update Person set home.address = :homeaddress where home.address  =  :homeaddress1", q.getQuery());
	}

	@Test
	public void testMultipleNestedUpdate() {
		Other home = new Other();
		home.setText("2old-address");
		db.save(home);

		UpdateQuery<Other> query = new GenericUpdateQuery<Other>(Other.class);
		query.set(query.target().getText(), "new-address");
		query.set(query.box(query.target().getPrice()), 1d);
		query.eq(query.target().getText(), "2old-address");
		int res = OrientDBObjectQuery.execute(query, db);
		assertEquals(1, res);
	}

	@Test
	public void testMultipleNestedUpdateGen() {
		UpdateQuery<Home> query = new GenericUpdateQuery<Home>(Home.class);
		query.set(query.target().getAddress(), "new-address");
		query.set(query.box(query.target().getPrice()), 1d);
		query.eq(query.target().getAddress(), "old-address");

		OrientDBQueryGenerator q = OrientDBObjectQuery.orientdbGenerator(query);
		assertEquals("update Home set address = :address,price = :price where address  =  :address1", q.getQuery());
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
