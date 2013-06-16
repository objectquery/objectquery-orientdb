package org.objectquery.orientdbobjectquery;

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
import org.objectquery.generic.GenericInternalQueryBuilder;
import org.objectquery.generic.GenericObjectQuery;
import org.objectquery.generic.Join;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.generic.Order;
import org.objectquery.generic.PathItem;
import org.objectquery.generic.Projection;
import org.objectquery.generic.ProjectionType;

public class OrientDBQueryGenerator {

	private Map<String, Object> parameters = new LinkedHashMap<String, Object>();
	private String query;

	OrientDBQueryGenerator(GenericObjectQuery<?> objQuery) {
		parameters.clear();
		StringBuilder builder = new StringBuilder();
		if (objQuery.getRootPathItem().getName() != null && !objQuery.getRootPathItem().getName().isEmpty()) {
			objQuery.getRootPathItem().setName("");
		}
		Stack<PathItem> items = new Stack<PathItem>();
		items.push(objQuery.getRootPathItem());
		buildQuery(objQuery.getTargetClass(), (GenericInternalQueryBuilder) objQuery.getBuilder(), objQuery.getJoins(), builder, items);
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
		StringBuilder name = new StringBuilder();
		buildParameterName(cond, name);
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
		} else if (cond.getValue() instanceof GenericObjectQuery<?>) {
			generateSubquery(sb, (GenericObjectQuery<?>) cond.getValue(), parent);
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

	public void buildQuery(Class<?> clazz, GenericInternalQueryBuilder query, List<Join> joins, StringBuilder builder, Stack<PathItem> parentItem) {
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
					generateSubquery(builder, (GenericObjectQuery<?>) proj.getItem(), parentItem);

				if (proj.getType() != null)
					builder.append(")");
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
			throw new ObjectQueryException("having clause was not supported by orientdb generator", null);
		}
		if (group && !groupby.isEmpty()) {
			builder.append(" group by ");
			Iterator<Projection> projections = groupby.iterator();
			while (projections.hasNext()) {
				Projection proj = projections.next();
				if (proj.getItem() instanceof PathItem)
					buildName((PathItem) proj.getItem(), builder);
				else
					generateSubquery(builder, (GenericObjectQuery<?>) proj.getItem(), parentItem);
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
					generateSubquery(builder, (GenericObjectQuery<?>) ord.getItem(), parentItem);
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

	private void generateSubquery(StringBuilder builder, GenericObjectQuery<?> goq, Stack<PathItem> parentItem) {
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

	private void buildParameterName(ConditionItem conditionItem, StringBuilder builder) {
		GenericInternalQueryBuilder.buildPath(conditionItem.getItem(), builder, "_");
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public String getQuery() {
		return query;
	}

}
