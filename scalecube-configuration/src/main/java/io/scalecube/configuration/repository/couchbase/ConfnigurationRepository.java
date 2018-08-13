package io.scalecube.configuration.repository.couchbase;

import io.scalecube.configuration.repository.Document;
import org.springframework.data.repository.CrudRepository;

public interface ConfnigurationRepository extends CrudRepository<Document, String> {
}
