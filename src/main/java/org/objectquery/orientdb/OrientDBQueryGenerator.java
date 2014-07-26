package org.objectquery.orientdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.objectquery.generic.ConditionElement;
import org.objectquery.generic.ConditionGroup;
import org.objectquery.generic.ConditionItem;
import org.objectquery.generic.ConditionType;
import org.objectquery.generic.GenericBaseQuery;
import org.objectquery.generic.GenericInternalQueryBuilder;
import org.objectquery.generic.GenericSelectQuery;
import org.objectquery.generic.Join;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.generic.Order;
import org.objectquery.generic.PathItem;
import org.objectquery.generic.Projection;
import org.objectquery.generic.ProjectionType;
import org.objectquery.generic.SetValue;

public class OrientDBQueryGenerator {

	private Map<String, Object> parameters = new LinkedHashMap<String, Object>();
	private String query;

	OrientDBQueryGenerator(GenericBaseQuery<?> baseQuery) {
		parameters.clear();
		GenericInternalQueryBuilder builder1 = (GenericInternalQueryBuilder) baseQuery.getBuilder();
		switch (builder1.getQueryType()) {
		case SELECT:
			if (baseQuery.getRootPathItem().getName() != null && !baseQuery.getRootPathItem().getName().isEmpty()) {
				baseQuery.getRootPathItem().setName("");
			}
			Stack<PathItem> items = new Stack<PathItem>();
			items.push(baseQuery.getRootPathItem());
			if (baseQuery instanceof GenericSelectQuery<?, ?>)
				buildQuery(baseQuery.getTargetClass(), builder1, ((GenericSelectQuery<?, ?>) baseQuery).getJoins(), items);
			break;
		case DELETE:
			buildDelete(baseQuery.getTargetClass(), builder1, baseQuery.getRootPathItem());
			break;

		case INSERT:
			buildInsert(baseQuery.getTargetClass(), builder1);
			break;

		case UPDATE:
			buildUpdate(baseQuery.getTargetClass(), builder1, baseQuery.getRootPathItem());
			break;

		default:
			break;
		}

	}

	private void buildInsert(Class<?> targetClass, GenericInternalQueryBuilder query) {
		StringBuilder builder = new StringBuilder();
		builder.append("insert into ").append(targetClass.getSimpleName()).append(" (");
		StringBuilder values = new StringBuilder(")values(");
		if (!query.getSets().isEmpty()) {
			Iterator<SetValue> iter = query.getSets().iterator();
			while (iter.hasNext()) {
				SetValue set = iter.next();
				buildName(set.getTarget(), builder);
				values.append(":").append(buildParameterName(set.getTarget(), set.getValue()));
				if (iter.hasNext()) {
					builder.append(",");
					values.append(",");
				}
			}
		}
		this.query = builder.append(values).append(")").toString();
	}

	private void buildUpdate(Class<?> targetClass, GenericInternalQueryBuilder query, PathItem pathItem) {
		StringBuilder builder = new StringBuilder();
		builder.append("update ").append(targetClass.getSimpleName()).append(" set ");
		if (!query.getSets().isEmpty()) {
			Iterator<SetValue> iter = query.getSets().iterator();
			while (iter.hasNext()) {
				SetValue set = iter.next();
				if (set.getTarget().getParent().getParent() != null)
					throw new ObjectQueryException("Not Supported nested field update in oriendb ");
				buildName(set.getTarget(), builder);
				builder.append(" = ").append(":").append(buildParameterName(set.getTarget(), set.getValue()));
				if (iter.hasNext())
					builder.append(",");
			}
		}
		if (!query.getConditions().isEmpty()) {
			builder.append(" where ");
			Stack<PathItem> items = new Stack<PathItem>();
			items.push(pathItem);
			stringfyGroup(query, builder, items);
		}
		this.query = builder.toString();
	}

	private void buildDelete(Class<?> targetClass, GenericInternalQueryBuilder query, PathItem pathItem) {
		StringBuilder builder = new StringBuilder();
		builder.append("delete from ").append(targetClass.getSimpleName()).append(" ");
		if (!query.getConditions().isEmpty()) {
			builder.append(" where ");
			Stack<PathItem> items = new Stack<PathItem>();
			items.push(pathItem);
			stringfyGroup(query, builder, items);
		}
		this.query = builder.toString();
	}

	private void stringfyGroup(ConditionGroup group, StringBuilder builder, Stack<PathItem> parent) {
		if (!group.getConditions().isEmpty()) {
			Iterator<ConditionElement> eli = group.getConditions().iterator();
			while (eli.hasNext()) {
				ConditionElement el = eli.next();
				if (el instanceof ConditionItem) {
					stringfyCondition((ConditionItem) el, builder, parent);
				} else if (el instanceof ConditionGroup) {
					builder.append(" ( ");
					stringfyGroup((ConditionGroup) el, builder, parent);
					builder.append(" ) ");
				}
				if (eli.hasNext()) {
					builder.append(" ").append(group.getType().toString()).append(" ");
				}
			}
		}
	}

	private String getConditionType(ConditionType type) {
		switch (type) {
		case CONTAINS:
			return " contains ";
		case EQUALS:
			return " = ";
		case IN:
			return " in ";
		case LIKE:
			return " like ";
		case GREATER:
			return " > ";
		case LESS:
			return " < ";
		case GREATER_EQUALS:
			return " >= ";
		case LESS_EQUALS:
			return " <= ";
		case NOT_CONTAINS:
			return " not contains ";
		case NOT_EQUALS:
			return " <> ";
		case NOT_IN:
			return " not in ";
		case NOT_LIKE:
			return "not like";
		case LIKE_NOCASE:
			return "";// TODO:find specific operator
		case NOT_LIKE_NOCASE:
			return "";// TODO:find specific operator
		case BETWEEN:
			return " BETWEEN ";
		}
		return "";
	}

	private void buildName(PathItem item, StringBuilder sb) {
		if (item.getParent() == null)
			sb.append("*");
		GenericInternalQueryBuilder.buildPath(item, sb);
	}

	private String buildParameterName(ConditionItem cond, Object value) {
		return buildParameterName(cond.getItem(), value);
	}

	private String buildParameterName(PathItem item, Object value) {
		StringBuilder name = new StringBuilder();
		GenericInternalQueryBuilder.buildPath(item, name, "");
		int i = 1;
		String realName = name.toString();
		do {
			if (!parameters.containsKey(realName)) {
				parameters.put(realName, value);
				return realName;
			}
			realName = name.toString() + i++;
		} while (true);
	}

	private void stringfyCondition(ConditionItem cond, StringBuilder sb, Stack<PathItem> parent) {

		buildName(cond.getItem(), sb);
		sb.append(" ").append(getConditionType(cond.getType())).append(" ");
		if (cond.getValue() instanceof PathItem) {
			buildName((PathItem) cond.getValue(), sb);
		} else if (cond.getValue() instanceof GenericSelectQuery<?, ?>) {
			generateSubquery(sb, (GenericSelectQuery<?, ?>) cond.getValue(), parent);
		} else {
			sb.append(":");
			sb.append(buildParameterName(cond, cond.getValue()));
		}
		if (cond.getType().equals(ConditionType.BETWEEN)) {
			sb.append(" AND ");
			if (cond.getValueTo() instanceof PathItem) {
				buildName((PathItem) cond.getValueTo(), sb);
			} else {
				sb.append(":");
				sb.append(buildParameterName(cond, cond.getValueTo()));
			}
		}
	}

	private String resolveFunction(ProjectionType projectionType) {
		switch (projectionType) {
		case AVG:
			return "AVG";
		case MAX:
			return "MAX";
		case MIN:
			return "MIN";
		case COUNT:
			return "COUNT";
		case SUM:
			return "SUM";
		}
		return "";
	}

	private void buildQuery(Class<?> clazz, GenericInternalQueryBuilder query, List<Join> joins, Stack<PathItem> parentItem) {
		StringBuilder builder = new StringBuilder();
		buildQuery(clazz, query, joins, builder, parentItem);
		this.query = builder.toString();
	}

	private void buildQuery(Class<?> clazz, GenericInternalQueryBuilder query, List<Join> joins, StringBuilder builder, Stack<PathItem> parentItem) {
		builder.append("select ");
		boolean group = false;
		List<Projection> groupby = new ArrayList<Projection>();
		if (!query.getProjections().isEmpty()) {
			Iterator<Projection> projections = query.getProjections().iterator();
			while (projections.hasNext()) {
				Projection proj = projections.next();
				if (proj.getType() != null) {
					builder.append(" ").append(resolveFunction(proj.getType())).append("(");
					group = true;
				} else {
					groupby.add(proj);
				}
				if (proj.getItem() instanceof PathItem)
					buildName((PathItem) proj.getItem(), builder);
				else
					generateSubquery(builder, (GenericSelectQuery<?, ?>) proj.getItem(), parentItem);

				if (proj.getType() != null)
					builder.append(")");
				if (proj.getMapper() != null) {
					builder.append(" as ");
					GenericInternalQueryBuilder.buildAlias(proj, builder);
				}
				if (projections.hasNext())
					builder.append(",");
			}
		}
		builder.append(" from ").append(clazz.getSimpleName());
		if (!joins.isEmpty())
			throw new ObjectQueryException("join are not supported by orientdb generator", null);

		if (!query.getConditions().isEmpty()) {
			builder.append(" where ");
			stringfyGroup(query, builder, parentItem);
		}
		if (!query.getHavings().isEmpty()) {
			throw new ObjectQueryException("having clause is not supported by orientdb generator", null);
		}
		if (group && !groupby.isEmpty()) {
			builder.append(" group by ");
			Iterator<Projection> projections = groupby.iterator();
			while (projections.hasNext()) {
				Projection proj = projections.next();
				if (proj.getItem() instanceof PathItem)
					buildName((PathItem) proj.getItem(), builder);
				else
					generateSubquery(builder, (GenericSelectQuery<?, ?>) proj.getItem(), parentItem);
				if (projections.hasNext())
					builder.append(",");
			}
		}
		if (!query.getOrders().isEmpty()) {
			builder.append(" order by ");
			Iterator<Order> orders = query.getOrders().iterator();
			while (orders.hasNext()) {
				Order ord = orders.next();
				if (ord.getProjectionType() != null)
					throw new ObjectQueryException("group operation in order clause is not supported by orientdb query language", null);
				if (ord.getItem() instanceof PathItem)
					buildName((PathItem) ord.getItem(), builder);
				else
					generateSubquery(builder, (GenericSelectQuery<?, ?>) ord.getItem(), parentItem);
				if (ord.getType() != null)
					builder.append(" ").append(ord.getType());
				if (orders.hasNext())
					builder.append(',');
			}
		}

	}

	private void setPaths(Stack<PathItem> parentItem) {
		StringBuilder pathValue = new StringBuilder();
		List<PathItem> toIterate = new ArrayList<PathItem>(parentItem);
		Collections.reverse(toIterate);
		for (PathItem pathItem : toIterate) {
			pathItem.setName(pathValue.toString());
			if (pathValue.length() == 0)
				pathValue.append("$current.parent");
			else
				pathValue.append(".parent");
		}
	}

	private void generateSubquery(StringBuilder builder, GenericSelectQuery<?, ?> goq, Stack<PathItem> parentItem) {
		/*
		parentItem.push(goq.getRootPathItem());
		setPaths(parentItem);
		builder.append("(");
		buildQuery(goq.getTargetClass(), (GenericInternalQueryBuilder) goq.getBuilder(), goq.getJoins(), builder, parentItem);
		builder.append(")");
		parentItem.pop().setName("");
		setPaths(parentItem);
		*/
		throw new ObjectQueryException("Unsupported subquery on orientdb implementation");
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public String getQuery() {
		return query;
	}

}
