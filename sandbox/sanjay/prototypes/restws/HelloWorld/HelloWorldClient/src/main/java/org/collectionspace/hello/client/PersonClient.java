
package org.collectionspace.hello.client;

import javax.ws.rs.core.Response;

import org.collectionspace.hello.Person;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A PersonClient.

 * @version $Revision:$
 */
public class PersonClient
{

   /**
    * 
    */
   private static final PersonClient instance = new PersonClient();

   /**
    * 
    */
   private PersonProxy personProxy;

   /**
    * 
    * Create a new PersonClient.
    *
    */
   private PersonClient()
   {
      ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(factory);
      personProxy = ProxyFactory.create(PersonProxy.class, "http://localhost:8080/helloworld/cspace");
   }
   
   /**
    * FIXME Comment this
    * 
    * @return
    */
   public static PersonClient getInstance()
   {
      return instance;
   }



   /**
    * @param id
    * @return
    * @see org.collectionspace.hello.client.PersonProxy#getPerson(java.lang.Long)
    */
       public ClientResponse<Person> getPerson(Long id)
   {
      return personProxy.getPerson(id);
   }

   /**
    * @param person
    * @return
    * @see org.collectionspace.hello.client.PersonProxy#createPerson(org.collectionspace.hello.client.entity.Person)
    */
   public ClientResponse<Response> createPerson(Person person)
   {
      return personProxy.createPerson(person);
   }


   /**
    * @param id
    * @param person
    * @return
    * @see org.collectionspace.hello.client.PersonProxy#updatePerson(java.lang.Long, org.collectionspace.hello.client.entity.Person)
    */
   public ClientResponse<Person> updatePerson(Long id, Person person)
   {
      return personProxy.updatePerson(id, person);
   }

   
}
