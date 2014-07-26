package org.objectquery.orientdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectquery.BaseQuery;
import org.objectquery.DeleteQuery;
import org.objectquery.InsertQuery;
import org.objectquery.SelectMapQuery;
import org.objectquery.SelectQuery;
import org.objectquery.UpdateQuery;
import org.objectquery.generic.GenericBaseQuery;
import org.objectquery.generic.GenericInternalQueryBuilder;
import org.objectquery.generic.GenericSelectQuery;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.generic.Projection;

import com.orientechnologies.orient.core.record.impl.ODocument;
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

	public static <M> List<M> execute(SelectMapQuery<?, M> query, OObjectDatabaseTx db) {
		OrientDBQueryGenerator gen = orientdbGenerator(query);
		GenericSelectQuery<?, M> gq = (GenericSelectQuery<?, M>) query;
		List<ODocument> qr = db.query(new OSQLSynchQuery<ODocument>(gen.getQuery()), gen.getParameters());
		List<Projection> projections = ((GenericInternalQueryBuilder) gq.getBuilder()).getProjections();
		List<M> realR = new ArrayList<>();
		Map<String, Object> values = new HashMap<String, Object>();
		for (ODocument oDocument : qr) {
			values.clear();
			for (String name : oDocument.fieldNames()) {
				values.put(name, oDocument.field(name));
			}
			realR.add(GenericInternalQueryBuilder.setMapping(gq.getMapperClass(), projections, values));
		}
		return realR;
	}
}
