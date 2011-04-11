package org.collectionspace.services.id.part;

import java.util.UUID;

public class UUIDType4IDPartAlgorithm implements IDPartAlgorithm {

    public UUIDType4IDPartAlgorithm(){
    }

    @Override
    public String generateID(){
        return UUID.randomUUID().toString();
    }

}
