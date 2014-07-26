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

	@Test
	public void testSimpleSelectMapNoFilter() {
		SelectMapQuery<Person, PersonDTO> query = new GenericSelectQuery<Person, PersonDTO>(Person.class, PersonDTO.class);
		query.prj(query.target().getName(), query.mapper().getName());

		List<PersonDTO> res = OrientDBObjectQuery.execute(query, db);
		assertThat(res.size(), CoreMatchers.is(3));
		for (PersonDTO personDTO : res) {
			assertThat(personDTO.getName(), CoreMatchers.notNullValue());
		}
	}

	@Test
	public void testSimpleSelectMapTwoEntries() {
		SelectMapQuery<Person, PersonDTO> query = new GenericSelectQuery<Person, PersonDTO>(Person.class, PersonDTO.class);
		query.eq(query.target().getName(), "tom");
		query.prj(query.target().getName(), query.mapper().getName());
		query.prj(query.target().getDog().getName(), query.mapper().getSurname());

		List<PersonDTO> res = OrientDBObjectQuery.execute(query, db);
		assertThat(res.size(), CoreMatchers.is(1));
		assertThat(res.get(0).getName(), CoreMatchers.is("tom"));
		assertThat(res.get(0).getSurname(), CoreMatchers.is("cerberus"));
	}

	@Test
	public void testSimpleSelectMapDeep() {
		SelectMapQuery<Person, PersonDTO> query = new GenericSelectQuery<Person, PersonDTO>(Person.class, PersonDTO.class);
		query.eq(query.target().getName(), "tom");
		query.prj(query.target().getName(), query.mapper().getName());
		query.prj(query.target().getHome().getAddress(), query.mapper().getAddressDTO().getStreet());

		List<PersonDTO> res = OrientDBObjectQuery.execute(query, db);
		assertThat(res.size(), CoreMatchers.is(1));
		assertThat(res.get(0).getName(), CoreMatchers.is("tom"));
		assertThat(res.get(0).getAddressDTO(), CoreMatchers.notNullValue());
		assertThat(res.get(0).getAddressDTO().getStreet(), CoreMatchers.is("homeless"));
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
