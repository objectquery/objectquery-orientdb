package org.objectquery.orientdbobjectquery;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectquery.ObjectQuery;
import org.objectquery.generic.GenericObjectQuery;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.generic.OrderType;
import org.objectquery.generic.ProjectionType;
import org.objectquery.orientdbobjectquery.domain.Home;
import org.objectquery.orientdbobjectquery.domain.Person;

public class TestSimpleQuery {

	@Test
	public void testBaseCondition() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.eq(target.getName(), "tom");

		Assert.assertEquals("select  from Person where name  =  :name", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test
	public void testDupliedPath() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.eq(target.getName(), "tom");
		qp.eq(target.getName(), "tom3");

		Assert.assertEquals("select  from Person where name  =  :name AND name  =  :name1", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test
	public void testDottedPath() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.eq(target.getDog().getName(), "tom");
		qp.eq(target.getDud().getName(), "tom3");

		Assert.assertEquals("select  from Person where dog.name  =  :dog_name AND dud.name  =  :dud_name", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test
	public void testProjection() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.prj(target.getName());
		qp.eq(target.getDog().getName(), "tom");

		Assert.assertEquals("select name from Person where dog.name  =  :dog_name", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test
	public void testProjectionCountThis() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.prj(target, ProjectionType.COUNT);
		qp.eq(target.getDog().getName(), "tom");

		Assert.assertEquals("select  COUNT(*) from Person where dog.name  =  :dog_name", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test
	public void testSelectOrder() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.eq(target.getDog().getName(), "tom");
		qp.order(target.getName());

		Assert.assertEquals("select  from Person where dog.name  =  :dog_name order by name", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test
	public void testOrderAsc() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.eq(target.getDog().getName(), "tom");
		qp.order(target.getName(), OrderType.ASC);

		Assert.assertEquals("select  from Person where dog.name  =  :dog_name order by name ASC", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test
	public void testOrderDesc() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.eq(target.getDog().getName(), "tom");
		qp.order(target.getName(), OrderType.DESC);
		qp.order(target.getDog().getName(), OrderType.DESC);

		Assert.assertEquals("select  from Person where dog.name  =  :dog_name order by name DESC,dog.name DESC", OrientDBObjectQuery.oriendbGenerator(qp)
				.getQuery());

	}

	@Test(expected = ObjectQueryException.class)
	public void testOrderGrouping() {

		GenericObjectQuery<Home> qp = new GenericObjectQuery<Home>(Home.class);
		Home target = qp.target();
		qp.eq(target.getAddress(), "homeless");
		qp.order(qp.box(target.getPrice()), ProjectionType.COUNT, OrderType.ASC);

		Assert.assertEquals("select  from Home where address  =  :address group by A  order by  COUNT(price) ASC", OrientDBObjectQuery.oriendbGenerator(qp)
				.getQuery());

	}

	@Test(expected = ObjectQueryException.class)
	public void testOrderGroupingPrj() {

		GenericObjectQuery<Home> qp = new GenericObjectQuery<Home>(Home.class);
		Home target = qp.target();
		qp.prj(target.getAddress());
		qp.prj(qp.box(target.getPrice()), ProjectionType.COUNT);
		qp.order(qp.box(target.getPrice()), ProjectionType.COUNT, OrderType.ASC);

		Assert.assertEquals("select address, COUNT(price) from Home A group by address order by  COUNT(price) ASC", OrientDBObjectQuery.oriendbGenerator(qp)
				.getQuery());

	}

	@Test
	public void testAllSimpleConditions() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		qp.eq(target.getName(), "tom");
		qp.like(target.getName(), "tom");
		qp.max(target.getName(), "tom");
		qp.min(target.getName(), "tom");
		qp.maxEq(target.getName(), "tom");
		qp.minEq(target.getName(), "tom");
		qp.notEq(target.getName(), "tom");

		Assert.assertEquals(
				"select  from Person where name  =  :name AND name  like  :name1 AND name  >  :name2 AND name  <  :name3 AND name  >=  :name4 AND name  <=  :name5 AND name  <>  :name6",
				OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test(expected=ObjectQueryException.class)
	public void testINCondition() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		List<String> pars = new ArrayList<String>();
		qp.in(target.getName(), pars);
		qp.notIn(target.getName(), pars);

		Assert.assertEquals("select  from Person where name  in  :name AND name  not in  :name1", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test
	public void testContainsCondition() {

		GenericObjectQuery<Person> qp = new GenericObjectQuery<Person>(Person.class);
		Person target = qp.target();
		Person p = new Person();
		qp.contains(target.getFriends(), p);
		qp.notContains(target.getFriends(), p);

		Assert.assertEquals("select  from Person where friends  contains  :friends AND friends  not contains  :friends1",
				OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}

	@Test()
	public void testProjectionGroup() {

		ObjectQuery<Home> qp = new GenericObjectQuery<Home>(Home.class);
		Home target = qp.target();
		qp.prj(target.getAddress());
		qp.prj(qp.box(target.getPrice()), ProjectionType.MAX);
		qp.order(target.getAddress());

		Assert.assertEquals("select address, MAX(price) from Home group by address order by address", OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}
	@Test(expected = ObjectQueryException.class)
	public void testProjectionGroupHaving() {

		ObjectQuery<Home> qp = new GenericObjectQuery<Home>(Home.class);
		Home target = qp.target();
		qp.prj(target.getAddress());
		qp.prj(qp.box(target.getPrice()), ProjectionType.MAX);
		qp.order(target.getAddress());
		qp.having(qp.box(target.getPrice()), ProjectionType.MAX).eq(0D);

		Assert.assertEquals(
				"select A.address, MAX(A.price) from org.objectquery.jdoobjectquery.domain.Home A group by A.address having MAX(A.price) = :price order by A.address",
				OrientDBObjectQuery.oriendbGenerator(qp).getQuery());

	}
}
