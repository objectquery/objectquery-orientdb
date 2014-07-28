package org.objectquery.orientdb;

import org.objectquery.QueryEngine;
import org.objectquery.QueryEngineFactory;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class OrientDBQueryEngineFactory implements QueryEngineFactory {

	@Override
	public <S> QueryEngine<S> createQueryEngine(Class<S> targetSession) {
		if (OObjectDatabaseTx.class.equals(targetSession))
			return createDefaultQueryEngine();
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> QueryEngine<T> createDefaultQueryEngine() {
		return (QueryEngine<T>) new OrientDBQueryEngine();
	}

}
