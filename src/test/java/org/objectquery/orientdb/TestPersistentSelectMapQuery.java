package org.objectquery.orientdb;

import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectquery.SelectMapQuery;
import org.objectquery.generic.GenericSelectQuery;
import org.objectquery.orientdb.domain.Person;
import org.objectquery.orientdb.domain.PersonDTO;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class TestPersistentSelectMapQuery {
	private OObjectDatabaseTx db;

	@Before
	public void beforeTest() {
		db = OrientDBTestHelper.getPool().acquire();
		db.begin();
	}

	@Test
	public void testSimpleSelectMap() {
		SelectMapQuery<Person, PersonDTO> query = new GenericSelectQuery<Person, PersonDTO>(Person.class, PersonDTO.class);
		query.eq(query.target().getName(), "tom");
		query.prj(query.target().getName(), query.mapper().getName());

		List<PersonDTO> res = OrientDBObjectQuery.execute(query, db);
		assertThat(res.size(), CoreMatchers.is(1));
		assertThat(res.get(0).getName(), CoreMatchers.is("tom"));
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
