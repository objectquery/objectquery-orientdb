package org.objectquery.orientdb;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectquery.DeleteQuery;
import org.objectquery.generic.GenericeDeleteQuery;
import org.objectquery.orientdb.domain.Other;
import org.objectquery.orientdb.domain.Person;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class TestDeleteQuery {

	private OObjectDatabaseTx db;

	@Before
	public void beforeTest() {
		db = OrientDBTestHelper.getPool().acquire();
		db.begin();
	}

	@Test
	public void testSimpleDelete() {
		Other ot = new Other();
		ot.setText("text");
		db.save(ot);
		DeleteQuery<Other> dq = new GenericeDeleteQuery<Other>(Other.class);
		int deleted = OrientDBObjectQuery.execute(dq, db);
		Assert.assertTrue(deleted != 0);
	}

	@Test
	public void testSimpleDeleteGen() {
		DeleteQuery<Person> dq = new GenericeDeleteQuery<Person>(Person.class);
		OrientDBQueryGenerator q = OrientDBObjectQuery.orientdbGenerator(dq);
		Assert.assertEquals("delete from Person ", q.getQuery());
	}

	@Test
	public void testDeleteCondition() {
		Other to_delete = new Other();
		to_delete.setText("to-delete");
		db.save(to_delete);

		DeleteQuery<Other> dq = new GenericeDeleteQuery<Other>(Other.class);
		dq.eq(dq.target().getText(), "to-delete");
		int deleted = OrientDBObjectQuery.execute(dq, db);
		Assert.assertTrue(deleted != 0);
	}

	@Test
	public void testDeleteConditionGen() {
		DeleteQuery<Person> dq = new GenericeDeleteQuery<Person>(Person.class);
		dq.eq(dq.target().getName(), "tom");
		OrientDBQueryGenerator q = OrientDBObjectQuery.orientdbGenerator(dq);
		Assert.assertEquals("delete from Person  where name  =  :name", q.getQuery());
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
