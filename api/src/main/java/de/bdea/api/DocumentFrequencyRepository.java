package de.bdea.api;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentFrequencyRepository extends MongoRepository<DocumentFrequency, String> {
}
