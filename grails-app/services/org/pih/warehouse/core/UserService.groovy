package org.pih.warehouse.core

import groovy.sql.Sql;

class UserService {

	def dataSource
	boolean transactional = true
	
	User getUser(String id) { 
		return User.get(id)
	}
	
	Boolean isUserInRole(String userId, Collection roles) { 
		User userInstance = getUser(userId)
		return userInstance?.roles.any { roles.contains(it.roleType) }
	}
	
	
	boolean isUserInAdminRole(User u) {
		def user = User.get(u.id)
		return user.roles.any { it.roleType == RoleType.ROLE_ADMIN }
	}
	
	boolean isUserInUserRole(User u) {
		def user = User.get(u.id)
		return user.roles.any { it.roleType == RoleType.ROLE_USER }
	}
	
	boolean isUserInManagerRole(User u) {
		def user = User.get(u.id)
		return user.roles.any { it.roleType == RoleType.ROLE_MANAGER }
	}
	
	
	def findUsers(String term) { 
		
	}
	
	def findPersons(String terms, Map params) { 		
		def criteria = Person.createCriteria()
		def results = criteria.list (params) {
			or { 
				like("firstName", terms)
				like("lastName", terms)
				like("email", terms)
			}
			order("lastName", "desc")
		}
		
	}
	
	
	void convertPersonToUser(String personId) { 
		def user = User.get(personId) 
		if (!user) { 
			def person = Person.get(personId)
			if (person) {
				def encodedPassword = "password"?.encodeAsPassword()
				Sql sql = new Sql(dataSource)
				sql.execute('insert into user (id, username, password) values (?, ?, ?)', [person?.id, person?.email, encodedPassword])		
			}
		}
	}

	void convertUserToPerson(String personId) {
		def person = Person.get(personId)
		if (person) {
			Sql sql = new Sql(dataSource)
			sql.execute('delete from user where id = ?', [personId])
		}
	}
	
	def findUsersByRoleType(RoleType roleType) { 
		def users = []
		def role = Role.findByRoleType(roleType)
		if (role) {
			def criteria = User.createCriteria()
			users = criteria.list {
				roles {
					eq("id", role.id)
				}
			}
		}
		return users;		
	}
	

}
