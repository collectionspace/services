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
import org.collectionspace.services.blob.BlobResource;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.id.IDResource;
import org.collectionspace.services.media.MediaResource;
import org.collectionspace.services.note.NoteResource;
import org.collectionspace.services.group.GroupResource;
import org.collectionspace.services.intake.IntakeResource;
import org.collectionspace.services.loanin.LoaninResource;
import org.collectionspace.services.loanout.LoanoutResource;
import org.collectionspace.services.objectexit.ObjectExitResource;
import org.collectionspace.services.imports.ImportsResource;
import org.collectionspace.services.location.LocationAuthorityResource;
import org.collectionspace.services.taxonomy.TaxonomyAuthorityResource;
import org.collectionspace.services.movement.MovementResource;
import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.report.ReportResource;
import org.collectionspace.services.acquisition.AcquisitionResource;
import org.collectionspace.services.dimension.DimensionResource;
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.vocabulary.VocabularyResource;
import org.collectionspace.services.organization.OrgAuthorityResource;
import org.collectionspace.services.person.PersonAuthorityResource;
import org.collectionspace.services.workflow.WorkflowResource;

//import org.collectionspace.services.query.QueryResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

//import org.collectionspace.services.common.FileUtils;
import org.collectionspace.services.authorization.PermissionResource;
import org.collectionspace.services.authorization.RoleResource;
import org.collectionspace.services.common.security.SecurityInterceptor;
//import org.collectionspace.services.common.document.DocumentUtils;
//import org.collectionspace.services.common.imaging.nuxeo.NuxeoImageUtils;
//import org.collectionspace.services.common.profile.Profiler;

/**
 * CollectionSpaceJaxRsApplication, the root application
 * for enumerating Resource classes in the Services Layer,
 * which in turn respond to and route REST-based requests.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class CollectionSpaceJaxRsApplication extends Application {

    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> empty = new HashSet<Class<?>>();    

    public CollectionSpaceJaxRsApplication() {    	
    	//
    	// Instantiate all our JaxRS resources
    	//
        singletons.add(new SecurityInterceptor());
        singletons.add(new AccountResource());
        singletons.add(new RoleResource());
        singletons.add(new PermissionResource());
        singletons.add(new VocabularyResource());
        singletons.add(new ContactResource());
        singletons.add(new PersonAuthorityResource());
        singletons.add(new OrgAuthorityResource());
        singletons.add(new CollectionObjectResource());
        singletons.add(new GroupResource());
        singletons.add(new IntakeResource());
        singletons.add(new DimensionResource());
        singletons.add(new RelationResource());
        singletons.add(new NoteResource());
        singletons.add(new LoaninResource());
        singletons.add(new LoanoutResource());
        singletons.add(new AcquisitionResource());
        singletons.add(new ObjectExitResource());
        singletons.add(new ImportsResource());
        singletons.add(new MediaResource());
        singletons.add(new BlobResource());
        singletons.add(new MovementResource());
        singletons.add(new ReportResource());
        singletons.add(new LocationAuthorityResource());
        singletons.add(new TaxonomyAuthorityResource());
        singletons.add(new IDResource());
        /*
        singletons.add(new WorkflowResource());
        */
//        singletons.add(new QueryResource());
//        singletons.add(new DomainIdentifierResource());
//        singletons.add(new PingResource());
    }

    @Override
    public Set<Class<?>> getClasses() {
        return empty;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}

