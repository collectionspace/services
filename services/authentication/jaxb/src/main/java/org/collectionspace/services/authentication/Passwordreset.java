package org.collectionspace.services.authentication;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "passwordreset")
public class Passwordreset {

    @XmlElement(required = true)
    protected String token;

    @XmlElement(required = true)
    protected String password;

    public String getToken() {
        return token;
    }

    public Passwordreset setToken(String token) {
        this.token = token;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Passwordreset setPassword(String password) {
        this.password = password;
        return this;
    }
}
