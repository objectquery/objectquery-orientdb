package org.objectquery.orientdbobjectquery;

import org.junit.Assert;
import org.junit.Test;
import org.objectquery.ObjectQuery;
import org.objectquery.generic.GenericObjectQuery;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.orientdbobjectquery.domain.Person;

public class TestSubQuery {

	private static String getQueryString(ObjectQuery<Person> query) {
		return OrientDBObjectQuery.oriendbGenerator(query).getQuery();
	}

	@Test(expected=ObjectQueryException.class)
	public void testSubquerySimple() {
		ObjectQuery<Person> query = new GenericObjectQuery<Person>(Person.class);

		ObjectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getName(), "test");
		query.eq(query.target().getDud(), subQuery);

		Assert.assertEquals(
				"select  from Person where dud  =  (select  from Person where name  =  :name)",
				getQueryString(query));

	}

	@Test(expected=ObjectQueryException.class)
	public void testBackReferenceSubquery() {
		GenericObjectQuery<Person> query = new GenericObjectQuery<Person>(Person.class);
		Person target = query.target();
		ObjectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getName(), target.getDog().getName());
		query.eq(query.target().getDud(), subQuery);

		Assert.assertEquals(
				"select  from Person where dud  =  (select  from Person where name  =  $current.parent.dog.name)",
				getQueryString(query));
	}

	@Test(expected=ObjectQueryException.class)
	public void testDoubleSubQuery() {

		GenericObjectQuery<Person> query = new GenericObjectQuery<Person>(Person.class);
		Person target = query.target();
		ObjectQuery<Person> subQuery = query.subQuery(Person.class);
		query.eq(target.getDud(), subQuery);
		subQuery.eq(subQuery.target().getName(), target.getDog().getName());
		ObjectQuery<Person> doubSubQuery = subQuery.subQuery(Person.class);
		subQuery.eq(subQuery.target().getMum(), doubSubQuery);

		doubSubQuery.eq(doubSubQuery.target().getMum().getName(), subQuery.target().getMum().getName());
		doubSubQuery.eq(doubSubQuery.target().getMum().getName(), query.target().getMum().getName());

		Assert.assertEquals(
				"select  from Person where dud  =  (select  from Person where name  =  $current.parent.dog.name AND mum  =  (select  from Person where mum.name  =  $current.parent.mum.name AND mum.name  =  $current.parent.parent.mum.name))",
				getQueryString(query));

	}

	@Test(expected=ObjectQueryException.class)
	public void testMultipleReferenceSubquery() {
		GenericObjectQuery<Person> query = new GenericObjectQuery<Person>(Person.class);
		Person target = query.target();
		ObjectQuery<Person> subQuery = query.subQuery(Person.class);
		ObjectQuery<Person> subQuery1 = query.subQuery(Person.class);
		query.eq(target.getDud(), subQuery);
		query.eq(target.getMum(), subQuery1);

		Assert.assertEquals(
				"select  from Person where dud  =  (select  from Person) AND mum  =  (select  from Person)",
				getQueryString(query));

	}

}
