package org.objectquery.orientdb;

import java.util.List;

import org.objectquery.BaseQuery;
import org.objectquery.DeleteQuery;
import org.objectquery.InsertQuery;
import org.objectquery.ObjectQuery;
import org.objectquery.UpdateQuery;
import org.objectquery.generic.GenericBaseQuery;
import org.objectquery.generic.ObjectQueryException;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class OrientDBObjectQuery {

	public static OrientDBQueryGenerator orientdbGenerator(BaseQuery<?> query) {
		if (query instanceof GenericBaseQuery<?>)
			return new OrientDBQueryGenerator((GenericBaseQuery<?>) query);
		throw new ObjectQueryException("The Object query instance of unconvertable implementation ", null);
	}

	@SuppressWarnings("rawtypes")
	public static <RET extends List<?>> RET execute(ObjectQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		return db.query(new OSQLSynchQuery(gen.getQuery()), gen.getParameters());
	}

	@SuppressWarnings("rawtypes")
	public static <RET> RET execute(UpdateQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		return db.command(new OCommandSQL(gen.getQuery())).execute(gen.getParameters());
	}

	@SuppressWarnings("rawtypes")
	public static <RET> RET execute(InsertQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		return db.command(new OCommandSQL(gen.getQuery())).execute(gen.getParameters());
	}

	@SuppressWarnings("rawtypes")
	public static int execute(DeleteQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		// gen.getParameters()
		return db.command(new OCommandSQL(gen.getQuery())).execute(gen.getParameters());
	}
}
