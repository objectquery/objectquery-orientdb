package org.objectquery.orientdb;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.objectquery.BaseSelectQuery;
import org.objectquery.SelectQuery;
import org.objectquery.generic.GenericSelectQuery;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.orientdb.domain.Dog;
import org.objectquery.orientdb.domain.Person;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class TestPersistentSubQuery {

	private OObjectDatabaseTx db;

	@Before
	public void beforeTest() {
		db = OrientDBTestHelper.getPool().acquire();
		db.begin();
	}

	@Test(expected = ObjectQueryException.class)
	@SuppressWarnings("unchecked")
	public void testSubquerySimple() {
		SelectQuery<Person> query = new GenericSelectQuery<Person, Object>(Person.class);

		BaseSelectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getName(), "tomdud");
		query.in(query.target().getDud(), subQuery);

		List<Person> res = (List<Person>) OrientDBObjectQuery.execute(query, db);
		assertEquals(1, res.size());
		assertEquals(res.get(0).getName(), "tom");
	}

	@Test(expected = ObjectQueryException.class)
	@SuppressWarnings("unchecked")
	public void testBackReferenceSubquery() {
		GenericSelectQuery<Person, Object> query = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = query.target();
		BaseSelectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getDog().getName(), target.getDog().getName());
		subQuery.notEq(subQuery.target(), target);
		query.eq(query.target().getDud(), subQuery);

		List<Person> res = (List<Person>) OrientDBObjectQuery.execute(query, db);
		assertEquals(1, res.size());
		assertEquals(res.get(0).getName(), "tom");
	}

	@Test(expected = ObjectQueryException.class)
	@SuppressWarnings("unchecked")
	public void testDoubleSubQuery() {

		GenericSelectQuery<Person, Object> query = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = query.target();
		BaseSelectQuery<Person> subQuery = query.subQuery(Person.class);
		query.eq(target.getDud(), subQuery);
		subQuery.eq(subQuery.target().getDog().getName(), target.getDog().getName());
		BaseSelectQuery<Dog> doubSubQuery = subQuery.subQuery(Dog.class);
		subQuery.eq(subQuery.target().getDog(), doubSubQuery);

		doubSubQuery.notEq(doubSubQuery.target().getOwner(), subQuery.target());
		doubSubQuery.notEq(doubSubQuery.target().getOwner(), query.target().getMum());

		List<Person> res = (List<Person>) OrientDBObjectQuery.execute(query, db);
		assertEquals(1, res.size());
		assertEquals(res.get(0).getName(), "tom");

	}

	@Test(expected = ObjectQueryException.class)
	@SuppressWarnings("unchecked")
	public void testMultipleReferenceSubquery() {
		GenericSelectQuery<Person, Object> query = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = query.target();
		BaseSelectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getName(), "tomdud");
		BaseSelectQuery<Person> subQuery1 = query.subQuery(Person.class);
		subQuery1.eq(subQuery1.target().getName(), "tommum");
		query.eq(target.getDud(), subQuery);
		query.eq(target.getMum(), subQuery1);

		List<Person> res = (List<Person>) OrientDBObjectQuery.execute(query, db);
		assertEquals(1, res.size());
		assertEquals(res.get(0).getName(), "tom");

	}

}