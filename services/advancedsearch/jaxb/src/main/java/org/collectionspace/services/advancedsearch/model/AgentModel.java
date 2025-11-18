package org.collectionspace.services.advancedsearch.model;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.ObjectProductionOrganizationGroup;
import org.collectionspace.services.collectionobject.ObjectProductionOrganizationGroupList;
import org.collectionspace.services.collectionobject.ObjectProductionPeopleGroup;
import org.collectionspace.services.collectionobject.ObjectProductionPeopleGroupList;
import org.collectionspace.services.collectionobject.ObjectProductionPersonGroup;
import org.collectionspace.services.collectionobject.ObjectProductionPersonGroupList;

public class AgentModel {

	public static Optional<AgentWithRole> agent(CollectionobjectsCommon collectionObject) {
		if (collectionObject == null) {
			return Optional.empty();
		}

		ObjectProductionPersonGroupList persons = collectionObject.getObjectProductionPersonGroupList();
		ObjectProductionOrganizationGroupList orgs = collectionObject.getObjectProductionOrganizationGroupList();
		ObjectProductionPeopleGroupList people = collectionObject.getObjectProductionPeopleGroupList();
		return Stream.<Supplier<AgentWithRole>>of(
			() -> personAgent(persons),
			() -> orgAgent(orgs),
			() -> peopleAgent(people))
				.map(Supplier::get)
				.filter(Objects::nonNull)
				.findFirst();
	}

	private static AgentWithRole personAgent(ObjectProductionPersonGroupList personGroupList) {
		AgentWithRole agent = null;
		if (personGroupList != null && !personGroupList.getObjectProductionPersonGroup().isEmpty()) {
			ObjectProductionPersonGroup personGroup = personGroupList.getObjectProductionPersonGroup().get(0);
			String person = personGroup.getObjectProductionPerson();
			if (person != null && !person.isEmpty()) {
				agent = new AgentWithRole(person);
				agent.role = personGroup.getObjectProductionPersonRole();
			}
		}
		return agent;
	}

	private static AgentWithRole orgAgent(ObjectProductionOrganizationGroupList organizationGroupList) {
		AgentWithRole agent = null;
		if (organizationGroupList != null && !organizationGroupList.getObjectProductionOrganizationGroup().isEmpty()) {
			ObjectProductionOrganizationGroup orgGroup =
				organizationGroupList.getObjectProductionOrganizationGroup().get(0);
			String org = orgGroup.getObjectProductionOrganization();
			if (org != null && !org.isEmpty()) {
				agent = new AgentWithRole(org);
				agent.role = orgGroup.getObjectProductionOrganizationRole();
			}
		}
		return agent;
	}

	private static AgentWithRole peopleAgent(ObjectProductionPeopleGroupList peopleGroupList) {
		AgentWithRole agent = null;
		if (peopleGroupList != null && !peopleGroupList.getObjectProductionPeopleGroup().isEmpty()) {
			ObjectProductionPeopleGroup peopleGroup = peopleGroupList.getObjectProductionPeopleGroup().get(0);
			String people = peopleGroup.getObjectProductionPeople();
			if (people != null && !people.isEmpty()) {
				agent = new AgentWithRole(people);
				agent.role = peopleGroup.getObjectProductionPeopleRole();
			}
		}
		return agent;
	}

	public static class AgentWithRole {
		private final String agent;
		private String role;

		public AgentWithRole(final String agent) {
			this.agent = agent;
		}

		public String getAgent() {
			return agent;
		}

		public String getRole() {
			return role;
		}
	}

}
