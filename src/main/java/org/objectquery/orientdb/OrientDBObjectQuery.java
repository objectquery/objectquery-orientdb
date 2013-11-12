package org.objectquery.orientdb;

import java.util.List;

import org.objectquery.BaseQuery;
import org.objectquery.DeleteQuery;
import org.objectquery.InsertQuery;
import org.objectquery.SelectQuery;
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
	public static <RET extends List<?>> RET execute(SelectQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		return db.query(new OSQLSynchQuery(gen.getQuery()), gen.getParameters());
	}

	public static int execute(UpdateQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		return db.command(new OCommandSQL(gen.getQuery())).execute(gen.getParameters());
	}

	public static boolean execute(InsertQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		return db.command(new OCommandSQL(gen.getQuery())).execute(gen.getParameters()) != null;
	}

	public static int execute(DeleteQuery<?> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		return db.command(new OCommandSQL(gen.getQuery())).execute(gen.getParameters());
	}
}
