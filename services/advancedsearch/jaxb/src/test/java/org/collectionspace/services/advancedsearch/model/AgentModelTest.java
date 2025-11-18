package org.collectionspace.services.advancedsearch.model;

import java.util.Optional;
import java.util.UUID;

import org.collectionspace.services.advancedsearch.model.AgentModel.AgentWithRole;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.ObjectFactory;
import org.collectionspace.services.collectionobject.ObjectProductionOrganizationGroup;
import org.collectionspace.services.collectionobject.ObjectProductionOrganizationGroupList;
import org.collectionspace.services.collectionobject.ObjectProductionPeopleGroup;
import org.collectionspace.services.collectionobject.ObjectProductionPeopleGroupList;
import org.collectionspace.services.collectionobject.ObjectProductionPersonGroup;
import org.collectionspace.services.collectionobject.ObjectProductionPersonGroupList;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Basic tests to ensure that the preferred ordering is used for the Agent and Agent Role response data
 */
public class AgentModelTest {

    private ObjectFactory objectFactory;
    private ObjectProductionPersonGroupList personGroupList;
    private ObjectProductionOrganizationGroupList orgGroupList;
    private ObjectProductionPeopleGroupList peopleGroupList;

    @BeforeClass
    public void setup() {
        objectFactory = new ObjectFactory();

        ObjectProductionPersonGroup personGroup = objectFactory.createObjectProductionPersonGroup();
        personGroup.setObjectProductionPerson(UUID.randomUUID().toString());
        personGroup.setObjectProductionPersonRole(UUID.randomUUID().toString());
        personGroupList = objectFactory.createObjectProductionPersonGroupList();
        personGroupList.getObjectProductionPersonGroup().add(personGroup);

        ObjectProductionOrganizationGroup orgGroup = objectFactory.createObjectProductionOrganizationGroup();
        orgGroup.setObjectProductionOrganization(UUID.randomUUID().toString());
        orgGroup.setObjectProductionOrganizationRole(UUID.randomUUID().toString());
        orgGroupList = objectFactory.createObjectProductionOrganizationGroupList();
        orgGroupList.getObjectProductionOrganizationGroup().add(orgGroup);

        ObjectProductionPeopleGroup peopleGroup = objectFactory.createObjectProductionPeopleGroup();
        peopleGroup.setObjectProductionPeople(UUID.randomUUID().toString());
        peopleGroup.setObjectProductionPeopleRole(UUID.randomUUID().toString());
        peopleGroupList = objectFactory.createObjectProductionPeopleGroupList();
        peopleGroupList.getObjectProductionPeopleGroup().add(peopleGroup);
    }

    @Test
    public void testOrderWithProductionPerson() {
        final ObjectProductionPersonGroup agentGroup = personGroupList.getObjectProductionPersonGroup().get(0);
        final String expectedAgent = agentGroup.getObjectProductionPerson();
        final String expectedRole = agentGroup.getObjectProductionPersonRole();

        final CollectionobjectsCommon common = objectFactory.createCollectionobjectsCommon();
        common.setObjectProductionPersonGroupList(personGroupList);
        common.setObjectProductionOrganizationGroupList(orgGroupList);
        common.setObjectProductionPeopleGroupList(peopleGroupList);

        final Optional<AgentWithRole> agentWithRole = AgentModel.agent(common);
        Assert.assertTrue(agentWithRole.isPresent());

        final AgentWithRole agent = agentWithRole.get();
        Assert.assertEquals(agent.getAgent(), expectedAgent);
        Assert.assertEquals(agent.getRole(), expectedRole);
    }

    @Test
    public void testOrderWithProductionOrganization() {
        final ObjectProductionOrganizationGroup agentGroup = orgGroupList.getObjectProductionOrganizationGroup().get(0);
        final String expectedAgent = agentGroup.getObjectProductionOrganization();
        final String expectedRole = agentGroup.getObjectProductionOrganizationRole();

        final CollectionobjectsCommon common = objectFactory.createCollectionobjectsCommon();
        common.setObjectProductionOrganizationGroupList(orgGroupList);
        common.setObjectProductionPeopleGroupList(peopleGroupList);

        final Optional<AgentWithRole> agentWithRole = AgentModel.agent(common);
        Assert.assertTrue(agentWithRole.isPresent());

        final AgentWithRole agent = agentWithRole.get();
        Assert.assertEquals(agent.getAgent(), expectedAgent);
        Assert.assertEquals(agent.getRole(), expectedRole);
    }

    @Test
    public void testOrderWithProductionPeople() {
        final ObjectProductionPeopleGroup agentGroup = peopleGroupList.getObjectProductionPeopleGroup().get(0);
        final String expectedAgent = agentGroup.getObjectProductionPeople();
        final String expectedRole = agentGroup.getObjectProductionPeopleRole();

        final CollectionobjectsCommon common = objectFactory.createCollectionobjectsCommon();
        common.setObjectProductionPeopleGroupList(peopleGroupList);

        final Optional<AgentWithRole> agentWithRole = AgentModel.agent(common);
        Assert.assertTrue(agentWithRole.isPresent());

        final AgentWithRole agent = agentWithRole.get();
        Assert.assertEquals(agent.getAgent(), expectedAgent);
        Assert.assertEquals(agent.getRole(), expectedRole);
    }

    @Test
    public void testAllEmpty() {
        final CollectionobjectsCommon common = objectFactory.createCollectionobjectsCommon();
        final Optional<AgentWithRole> agentWithRole = AgentModel.agent(common);
        Assert.assertFalse(agentWithRole.isPresent());
    }
}