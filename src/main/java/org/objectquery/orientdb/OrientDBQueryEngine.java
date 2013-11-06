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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int execute(DeleteQuery<?> dq, OObjectDatabaseTx engineSession) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean execute(InsertQuery<?> ip, OObjectDatabaseTx engineSession) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int execute(UpdateQuery<?> query, OObjectDatabaseTx engineSession) {
		// TODO Auto-generated method stub
		return 0;
	}

}
