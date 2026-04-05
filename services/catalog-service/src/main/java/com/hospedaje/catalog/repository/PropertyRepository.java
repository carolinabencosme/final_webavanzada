package com.hospedaje.catalog.repository;

import com.hospedaje.catalog.document.Property;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PropertyRepository extends MongoRepository<Property, String> {

    Optional<Property> findByExternalRef(String externalRef);
}
