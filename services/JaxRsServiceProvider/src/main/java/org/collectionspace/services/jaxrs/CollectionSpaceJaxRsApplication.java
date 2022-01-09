/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.jaxrs;

import org.collectionspace.services.account.AccountResource;
import org.collectionspace.services.account.TenantResource;
import org.collectionspace.services.blob.BlobResource;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.id.IDResource;
import org.collectionspace.services.insurance.InsuranceResource;
import org.collectionspace.services.media.MediaResource;
import org.collectionspace.services.group.GroupResource;
import org.collectionspace.services.hit.HitResource;
import org.collectionspace.services.intake.IntakeResource;
import org.collectionspace.services.index.IndexResource;
import org.collectionspace.services.loanin.LoaninResource;
import org.collectionspace.services.loanout.LoanoutResource;
import org.collectionspace.services.transport.TransportResource;
import org.collectionspace.services.uoc.UocResource;
import org.collectionspace.services.audit.AuditResource;
import org.collectionspace.services.valuationcontrol.ValuationcontrolResource;
import org.collectionspace.services.objectexit.ObjectExitResource;
import org.collectionspace.services.batch.BatchResource;
import org.collectionspace.services.imports.ImportsResource;
import org.collectionspace.services.export.ExportResource;
import org.collectionspace.services.location.LocationAuthorityResource;
import org.collectionspace.services.place.PlaceAuthorityResource;
import org.collectionspace.services.work.WorkAuthorityResource;
import org.collectionspace.services.material.MaterialAuthorityResource;
import org.collectionspace.services.concept.ConceptAuthorityResource;
import org.collectionspace.services.taxonomy.TaxonomyAuthorityResource;
import org.collectionspace.services.movement.MovementResource;
import org.collectionspace.services.propagation.PropagationResource;
import org.collectionspace.services.pottag.PottagResource;
import org.collectionspace.services.report.ReportResource;
import org.collectionspace.services.acquisition.AcquisitionResource;
import org.collectionspace.services.dimension.DimensionResource;
import org.collectionspace.services.servicegroup.ServiceGroupResource;
import org.collectionspace.services.structureddate.StructuredDateResource;
import org.collectionspace.services.systeminfo.SystemInfoResource;
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.vocabulary.VocabularyResource;
import org.collectionspace.services.organization.OrgAuthorityResource;
import org.collectionspace.services.person.PersonAuthorityResource;
import org.collectionspace.services.citation.CitationAuthorityResource;
import org.collectionspace.services.claim.ClaimResource;
import org.collectionspace.services.exhibition.ExhibitionResource;
import org.collectionspace.services.osteology.OsteologyResource;
import org.collectionspace.services.conditioncheck.ConditioncheckResource;
import org.collectionspace.services.conservation.ConservationResource;
import org.collectionspace.services.authorization.PermissionResource;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;



//import org.collectionspace.services.common.FileUtils;
import org.collectionspace.services.authorization.RoleResource;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ResourceMapHolder;
import org.collectionspace.services.common.ResourceMapImpl;
import org.collectionspace.services.common.publicitem.PublicItemResource;
import org.collectionspace.services.common.relation.RelationResource;
import org.collectionspace.services.common.security.SecurityInterceptor;

/**
 * CollectionSpaceJaxRsApplication, the root application
 * for enumerating Resource classes in the Services Layer,
 * which in turn respond to and route REST-based requests.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class CollectionSpaceJaxRsApplication extends Application
					implements ResourceMapHolder {

    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> empty = new HashSet<Class<?>>();
    private ResourceMap resourceMap = new ResourceMapImpl();
    private ServletContext servletContext = null;

    public CollectionSpaceJaxRsApplication() {
    	//
    	// Instantiate all our JaxRS resources
    	//
        singletons.add(new SecurityInterceptor());

        singletons.add(new AccountResource());
        singletons.add(new TenantResource());
        singletons.add(new RoleResource());
        singletons.add(new PermissionResource());
        singletons.add(new ServiceGroupResource());
        singletons.add(new ImportsResource());
        singletons.add(new ExportResource());
        singletons.add(new StructuredDateResource());
        singletons.add(new SystemInfoResource());
        singletons.add(new IndexResource());

        addResourceToMapAndSingletons(new VocabularyResource());
        addResourceToMapAndSingletons(new PersonAuthorityResource());
        addResourceToMapAndSingletons(new CitationAuthorityResource());
        addResourceToMapAndSingletons(new OrgAuthorityResource());
        addResourceToMapAndSingletons(new LocationAuthorityResource());
        addResourceToMapAndSingletons(new ConceptAuthorityResource());
        addResourceToMapAndSingletons(new TaxonomyAuthorityResource());
        addResourceToMapAndSingletons(new PlaceAuthorityResource());
        addResourceToMapAndSingletons(new WorkAuthorityResource());
        addResourceToMapAndSingletons(new MaterialAuthorityResource());
        addResourceToMapAndSingletons(new AcquisitionResource());
        addResourceToMapAndSingletons(new ContactResource());
        addResourceToMapAndSingletons(new CollectionObjectResource());
        addResourceToMapAndSingletons(new GroupResource());
        addResourceToMapAndSingletons(new InsuranceResource());
        addResourceToMapAndSingletons(new IntakeResource());
        addResourceToMapAndSingletons(new HitResource());
        addResourceToMapAndSingletons(new DimensionResource());
        addResourceToMapAndSingletons(new RelationResource());
        addResourceToMapAndSingletons(new LoaninResource());
        addResourceToMapAndSingletons(new LoanoutResource());
        addResourceToMapAndSingletons(new ExhibitionResource());
        addResourceToMapAndSingletons(new OsteologyResource());
        addResourceToMapAndSingletons(new ConditioncheckResource());
        addResourceToMapAndSingletons(new ConservationResource());
        addResourceToMapAndSingletons(new UocResource());
        addResourceToMapAndSingletons(new ValuationcontrolResource());
        addResourceToMapAndSingletons(new ObjectExitResource());
        addResourceToMapAndSingletons(new BatchResource());
        addResourceToMapAndSingletons(new MediaResource());
        addResourceToMapAndSingletons(new BlobResource());
        addResourceToMapAndSingletons(new MovementResource());
        addResourceToMapAndSingletons(new PropagationResource());
        addResourceToMapAndSingletons(new PottagResource());
        addResourceToMapAndSingletons(new ClaimResource());
        addResourceToMapAndSingletons(new ReportResource());
        addResourceToMapAndSingletons(new PublicItemResource());
        addResourceToMapAndSingletons(new TransportResource());

        singletons.add(new IDResource());
        singletons.add(new AuditResource());

        /*
        singletons.add(new WorkflowResource());
        */
//        singletons.add(new DomainIdentifierResource());
//        singletons.add(new PingResource());
    }

    private void addResourceToMapAndSingletons(NuxeoBasedResource resource) {
        singletons.add(resource);
        resourceMap.put(resource.getServiceName(), resource);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return empty;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    public ResourceMap getResourceMap() {
        return resourceMap;
    }

    public void setServletContext(ServletContext servletContext) {
    	this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
    	return this.servletContext;
    }

}
