package org.objectquery.orientdb;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectquery.QueryEngine;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class QueryEngineTest {

	@Test
	public void testFactory() {
		QueryEngine<OObjectDatabaseTx> instance = QueryEngine.instance(OObjectDatabaseTx.class);
		assertTrue(instance instanceof OrientDBQueryEngine);
	}

	@Test
	public void testDefalutFactory() {
		QueryEngine<OObjectDatabaseTx> instance = QueryEngine.defaultInstance();
		assertTrue(instance instanceof OrientDBQueryEngine);
	}
}
