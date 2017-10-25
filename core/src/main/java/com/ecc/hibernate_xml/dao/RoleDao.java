package com.ecc.hibernate_xml.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;

import com.ecc.hibernate_xml.model.Role;
import com.ecc.hibernate_xml.model.Person;
import com.ecc.hibernate_xml.util.dao.TransactionScope;

public class RoleDao extends AbstractDao<Role> {
	public RoleDao() {
		super(Role.class);
	} 

	@Override
	protected Throwable onCreateFailure(Role role, Throwable cause) {
		return onUpdateFailure(role, cause);
	}

	@Override
	protected Throwable onUpdateFailure(Role role, Throwable cause) {
		if (cause instanceof ConstraintViolationException) {
			return new RuntimeException(String.format(
				"Role name \"%s\" is already existing.", role.getName()));
		}
		return cause;
	}

	@Override
	protected Throwable onDeleteFailure(Role role, Throwable cause) {
		if (cause instanceof ConstraintViolationException && role.getPersons().size() > 0) {
			String personIds = role.getPersons()
				.stream()
				.map(person -> person.getId().toString())
				.collect(Collectors.joining(", "));

			return new RuntimeException(
				String.format("Role is in used by person IDs [%s].", personIds));
		}
		return cause;
	}

	public List<Role> list(Person person) {
		return TransactionScope.executeTransactionWithResult(session -> {
			return session.createCriteria(Role.class)
				.setCacheable(true)
				.createAlias("persons", "P")
				.add(Restrictions.eq("P.id", person.getId()))
				.addOrder(Order.asc("id"))
				.list();
		});
	}
}