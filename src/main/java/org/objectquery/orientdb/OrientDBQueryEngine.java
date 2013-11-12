package org.objectquery.orientdb;

import java.util.List;

import org.objectquery.DeleteQuery;
import org.objectquery.InsertQuery;
import org.objectquery.QueryEngine;
import org.objectquery.SelectQuery;
import org.objectquery.UpdateQuery;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class OrientDBQueryEngine extends QueryEngine<OObjectDatabaseTx> {

	@Override
	public <RET extends List<?>> RET execute(SelectQuery<?> query, OObjectDatabaseTx engineSession) {
		return OrientDBObjectQuery.execute(query, engineSession);
	}

	@Override
	public int execute(DeleteQuery<?> dq, OObjectDatabaseTx engineSession) {
		return OrientDBObjectQuery.execute(dq, engineSession);
	}

	@Override
	public boolean execute(InsertQuery<?> ip, OObjectDatabaseTx engineSession) {
		return OrientDBObjectQuery.execute(ip, engineSession);
	}

	@Override
	public int execute(UpdateQuery<?> query, OObjectDatabaseTx engineSession) {
		return OrientDBObjectQuery.execute(query, engineSession);
	}

}
