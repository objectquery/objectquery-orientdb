package org.objectquery.orientdb;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.objectquery.SelectMapQuery;
import org.objectquery.generic.GenericSelectQuery;
import org.objectquery.orientdb.domain.Person;
import org.objectquery.orientdb.domain.PersonDTO;

public class TestSelectMapQuery {

	@Test
	public void testSimpleSelectMap() {
		SelectMapQuery<Person, PersonDTO> query = new GenericSelectQuery<Person, PersonDTO>(Person.class, PersonDTO.class);
		query.prj(query.target().getName(), query.mapper().getName());

		assertThat(OrientDBObjectQuery.orientdbGenerator(query).getQuery(), CoreMatchers.is("select name as name from Person"));

	}

}
