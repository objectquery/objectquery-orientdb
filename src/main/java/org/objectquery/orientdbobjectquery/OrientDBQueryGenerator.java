package org.objectquery.orientdbobjectquery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectquery.generic.ConditionElement;
import org.objectquery.generic.ConditionGroup;
import org.objectquery.generic.ConditionItem;
import org.objectquery.generic.ConditionType;
import org.objectquery.generic.GenericInternalQueryBuilder;
import org.objectquery.generic.GenericObjectQuery;
import org.objectquery.generic.ObjectQueryException;
import org.objectquery.generic.Order;
import org.objectquery.generic.PathItem;
import org.objectquery.generic.Projection;
import org.objectquery.generic.ProjectionType;

public class OrientDBQueryGenerator {

	private Map<String, Object> parameters = new LinkedHashMap<String, Object>();
	private String query;

	OrientDBQueryGenerator(GenericObjectQuery<?> jpqlObjectQuery) {
		buildQuery(jpqlObjectQuery.getTargetClass(), (GenericInternalQueryBuilder) jpqlObjectQuery.getBuilder());
	}

	private void stringfyGroup(ConditionGroup group, StringBuilder builder) {
		if (!group.getConditions().isEmpty()) {
			Iterator<ConditionElement> eli = group.getConditions().iterator();
			while (eli.hasNext()) {
				ConditionElement el = eli.next();
				if (el instanceof ConditionItem) {
					stringfyCondition((ConditionItem) el, builder);
				} else if (el instanceof ConditionGroup) {
					builder.append(" ( ");
					stringfyGroup((ConditionGroup) el, builder);
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
		case MAX:
			return " > ";
		case MIN:
			return " < ";
		case MAX_EQUALS:
			return " >= ";
		case MIN_EQUALS:
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

	private void stringfyCondition(ConditionItem cond, StringBuilder sb) {

		buildName(cond.getItem(), sb);
		sb.append(" ").append(getConditionType(cond.getType())).append(" ");
		if (cond.getValue() instanceof PathItem) {
			buildName((PathItem) cond.getValue(), sb);
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
				sb.append(buildParameterName(cond, cond.getValue()));
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

	public void buildQuery(Class<?> clazz, GenericInternalQueryBuilder query) {
		parameters.clear();
		StringBuilder builder = new StringBuilder();
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
				buildName(proj.getItem(), builder);
				if (proj.getType() != null)
					builder.append(")");
				if (projections.hasNext())
					builder.append(",");
			}
		}
		builder.append(" from ").append(clazz.getSimpleName());
		if (!query.getConditions().isEmpty()) {
			builder.append(" where ");
			stringfyGroup(query, builder);
		}
		if (!query.getHavings().isEmpty()) {
			throw new ObjectQueryException("having clause was not supported by orientdb generator", null);
		}
		if (group && !groupby.isEmpty()) {
			builder.append(" group by ");
			Iterator<Projection> projections = groupby.iterator();
			while (projections.hasNext()) {
				Projection proj = projections.next();
				buildName(proj.getItem(), builder);
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
				buildName(ord.getItem(), builder);
				if (ord.getType() != null)
					builder.append(" ").append(ord.getType());
				if (orders.hasNext())
					builder.append(',');
			}
		}

		this.query = builder.toString();
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
