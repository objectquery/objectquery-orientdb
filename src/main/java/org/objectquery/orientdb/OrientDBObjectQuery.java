package org.objectquery.orientdb;

import java.util.List;

import org.objectquery.ObjectQuery;
import org.objectquery.generic.GenericObjectQuery;
import org.objectquery.generic.ObjectQueryException;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class OrientDBObjectQuery {

	public static OrientDBQueryGenerator oriendbGenerator(ObjectQuery<?> query) {
		if (query instanceof GenericObjectQuery<?>)
			return new OrientDBQueryGenerator((GenericObjectQuery<?>) query);
		throw new ObjectQueryException("The Object query instance of unconvertable implementation ", null);
	}

	@SuppressWarnings("rawtypes")
	public static <RET extends List<?>> RET execute(ObjectQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = oriendbGenerator(query);
		return db.query(new OSQLSynchQuery(gen.getQuery()), gen.getParameters());
	}
}
