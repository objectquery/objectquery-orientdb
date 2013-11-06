package org.objectquery.orientdb;

import org.junit.Assert;
import org.junit.Test;
import org.objectquery.SelectQuery;
import org.objectquery.generic.GenericSelectQuery;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.orientdb.domain.Person;

public class TestSubQuery {

	private static String getQueryString(SelectQuery<Person> query) {
		return OrientDBObjectQuery.orientdbGenerator(query).getQuery();
	}

	@Test(expected=ObjectQueryException.class)
	public void testSubquerySimple() {
		SelectQuery<Person> query = new GenericSelectQuery<Person>(Person.class);

		SelectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getName(), "test");
		query.eq(query.target().getDud(), subQuery);

		Assert.assertEquals(
				"select  from Person where dud  =  (select  from Person where name  =  :name)",
				getQueryString(query));

	}

	@Test(expected=ObjectQueryException.class)
	public void testBackReferenceSubquery() {
		GenericSelectQuery<Person> query = new GenericSelectQuery<Person>(Person.class);
		Person target = query.target();
		SelectQuery<Person> subQuery = query.subQuery(Person.class);
		subQuery.eq(subQuery.target().getName(), target.getDog().getName());
		query.eq(query.target().getDud(), subQuery);

		Assert.assertEquals(
				"select  from Person where dud  =  (select  from Person where name  =  $current.parent.dog.name)",
				getQueryString(query));
	}

	@Test(expected=ObjectQueryException.class)
	public void testDoubleSubQuery() {

		GenericSelectQuery<Person> query = new GenericSelectQuery<Person>(Person.class);
		Person target = query.target();
		SelectQuery<Person> subQuery = query.subQuery(Person.class);
		query.eq(target.getDud(), subQuery);
		subQuery.eq(subQuery.target().getName(), target.getDog().getName());
		SelectQuery<Person> doubSubQuery = subQuery.subQuery(Person.class);
		subQuery.eq(subQuery.target().getMum(), doubSubQuery);

		doubSubQuery.eq(doubSubQuery.target().getMum().getName(), subQuery.target().getMum().getName());
		doubSubQuery.eq(doubSubQuery.target().getMum().getName(), query.target().getMum().getName());

		Assert.assertEquals(
				"select  from Person where dud  =  (select  from Person where name  =  $current.parent.dog.name AND mum  =  (select  from Person where mum.name  =  $current.parent.mum.name AND mum.name  =  $current.parent.parent.mum.name))",
				getQueryString(query));

	}

	@Test(expected=ObjectQueryException.class)
	public void testMultipleReferenceSubquery() {
		GenericSelectQuery<Person> query = new GenericSelectQuery<Person>(Person.class);
		Person target = query.target();
		SelectQuery<Person> subQuery = query.subQuery(Person.class);
		SelectQuery<Person> subQuery1 = query.subQuery(Person.class);
		query.eq(target.getDud(), subQuery);
		query.eq(target.getMum(), subQuery1);

		Assert.assertEquals(
				"select  from Person where dud  =  (select  from Person) AND mum  =  (select  from Person)",
				getQueryString(query));

	}

}
