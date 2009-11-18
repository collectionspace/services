package org.collectionspace.services.id.part;

public class NonEmptyIDPartValidator implements IDPartValidator {

    @Override
    public boolean isValid(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

}
