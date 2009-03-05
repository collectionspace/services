package org.collectionspace.hello.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "person", propOrder = {
    "firstName",
    "lastName",
    "street",
    "city",
    "state",
    "zip",
    "country"
})
public class Person extends Listener {

    @XmlAttribute
    private int id;
    @XmlAttribute
    private int version = 1;
    private String firstName;
    private String lastName;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * JAXB Callback method used to reassociate the item with the owning contact.
     * JAXB doesn't seem to read this method from a super class and it must
     * therefore be placed on any subclass.
     *
     * @param unmarshaller the JAXB {@link Unmarshaller}.
     * @param parent the owning {@link Contact} instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        super.afterUnmarshal(unmarshaller, parent);
    }
}
