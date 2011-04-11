package org.collectionspace.services.id.part;

public class NoOpIDPartValidator implements IDPartValidator {

    public NoOpIDPartValidator() {
    }

    @Override
    public boolean isValid(String id) {
        return true;
    }

}
