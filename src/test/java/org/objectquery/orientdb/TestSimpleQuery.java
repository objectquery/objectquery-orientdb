package org.objectquery.orientdb;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectquery.SelectQuery;
import org.objectquery.generic.GenericSelectQuery;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.generic.OrderType;
import org.objectquery.generic.ProjectionType;
import org.objectquery.orientdb.domain.Home;
import org.objectquery.orientdb.domain.Person;

public class TestSimpleQuery {

	@Test
	public void testBaseCondition() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.eq(target.getName(), "tom");

		Assert.assertEquals("select  from Person where name  =  :name", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test
	public void testDupliedPath() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.eq(target.getName(), "tom");
		qp.eq(target.getName(), "tom3");

		Assert.assertEquals("select  from Person where name  =  :name AND name  =  :name1", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test
	public void testDottedPath() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.eq(target.getDog().getName(), "tom");
		qp.eq(target.getDud().getName(), "tom3");

		Assert.assertEquals("select  from Person where dog.name  =  :dogname AND dud.name  =  :dudname", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test
	public void testProjection() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.prj(target.getName());
		qp.eq(target.getDog().getName(), "tom");

		Assert.assertEquals("select name from Person where dog.name  =  :dogname", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test
	public void testProjectionCountThis() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.prj(target, ProjectionType.COUNT);
		qp.eq(target.getDog().getName(), "tom");

		Assert.assertEquals("select  COUNT(*) from Person where dog.name  =  :dogname", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test
	public void testSelectOrder() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.eq(target.getDog().getName(), "tom");
		qp.order(target.getName());

		Assert.assertEquals("select  from Person where dog.name  =  :dogname order by name", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test
	public void testOrderAsc() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.eq(target.getDog().getName(), "tom");
		qp.order(target.getName(), OrderType.ASC);

		Assert.assertEquals("select  from Person where dog.name  =  :dogname order by name ASC", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test
	public void testOrderDesc() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.eq(target.getDog().getName(), "tom");
		qp.order(target.getName(), OrderType.DESC);
		qp.order(target.getDog().getName(), OrderType.DESC);

		Assert.assertEquals("select  from Person where dog.name  =  :dogname order by name DESC,dog.name DESC", OrientDBObjectQuery.orientdbGenerator(qp)
				.getQuery());

	}

	@Test(expected = ObjectQueryException.class)
	public void testOrderGrouping() {

		SelectQuery<Home> qp = new GenericSelectQuery<Home, Object>(Home.class);
		Home target = qp.target();
		qp.eq(target.getAddress(), "homeless");
		qp.order(qp.box(target.getPrice()), ProjectionType.COUNT, OrderType.ASC);

		Assert.assertEquals("select  from Home where address  =  :address group by A  order by  COUNT(price) ASC", OrientDBObjectQuery.orientdbGenerator(qp)
				.getQuery());

	}

	@Test(expected = ObjectQueryException.class)
	public void testOrderGroupingPrj() {

		SelectQuery<Home> qp = new GenericSelectQuery<Home, Object>(Home.class);
		Home target = qp.target();
		qp.prj(target.getAddress());
		qp.prj(qp.box(target.getPrice()), ProjectionType.COUNT);
		qp.order(qp.box(target.getPrice()), ProjectionType.COUNT, OrderType.ASC);

		Assert.assertEquals("select address, COUNT(price) from Home A group by address order by  COUNT(price) ASC", OrientDBObjectQuery.orientdbGenerator(qp)
				.getQuery());

	}

	@Test
	public void testAllSimpleConditions() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		qp.eq(target.getName(), "tom");
		qp.like(target.getName(), "tom");
		qp.gt(target.getName(), "tom");
		qp.lt(target.getName(), "tom");
		qp.gtEq(target.getName(), "tom");
		qp.ltEq(target.getName(), "tom");
		qp.notEq(target.getName(), "tom");

		Assert.assertEquals(
				"select  from Person where name  =  :name AND name  like  :name1 AND name  >  :name2 AND name  <  :name3 AND name  >=  :name4 AND name  <=  :name5 AND name  <>  :name6",
				OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test()
	public void testINCondition() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		List<String> pars = new ArrayList<String>();
		qp.in(target.getName(), pars);
		qp.notIn(target.getName(), pars);

		Assert.assertEquals("select  from Person where name  in  :name AND name  not in  :name1", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test
	public void testContainsCondition() {

		GenericSelectQuery<Person, Object> qp = new GenericSelectQuery<Person, Object>(Person.class);
		Person target = qp.target();
		Person p = new Person();
		qp.contains(target.getFriends(), p);
		qp.notContains(target.getFriends(), p);

		Assert.assertEquals("select  from Person where friends  contains  :friends AND friends  not contains  :friends1", OrientDBObjectQuery
				.orientdbGenerator(qp).getQuery());

	}

	@Test()
	public void testProjectionGroup() {

		SelectQuery<Home> qp = new GenericSelectQuery<Home, Object>(Home.class);
		Home target = qp.target();
		qp.prj(target.getAddress());
		qp.prj(qp.box(target.getPrice()), ProjectionType.MAX);
		qp.order(target.getAddress());

		Assert.assertEquals("select address, MAX(price) from Home group by address order by address", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test(expected = ObjectQueryException.class)
	public void testProjectionGroupHaving() {

		SelectQuery<Home> qp = new GenericSelectQuery<Home, Object>(Home.class);
		Home target = qp.target();
		qp.prj(target.getAddress());
		qp.prj(qp.box(target.getPrice()), ProjectionType.MAX);
		qp.order(target.getAddress());
		qp.having(qp.box(target.getPrice()), ProjectionType.MAX).eq(0D);

		Assert.assertEquals("select A.address, MAX(A.price) from Home A group by A.address having MAX(A.price) = :price order by A.address",
				OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}

	@Test(expected = ObjectQueryException.class)
	public void testJoin() {

		SelectQuery<Home> qp = new GenericSelectQuery<Home, Object>(Home.class);
		qp.join(Person.class);
		OrientDBObjectQuery.orientdbGenerator(qp);

	}

	@Test
	public void testBetweenCondition() {
		SelectQuery<Home> qp = new GenericSelectQuery<Home, Object>(Home.class);
		Home target = qp.target();
		qp.between(qp.box(target.getPrice()), 20D, 30D);

		Assert.assertEquals("select  from Home where price  BETWEEN  :price AND :price1", OrientDBObjectQuery.orientdbGenerator(qp).getQuery());

	}
}
