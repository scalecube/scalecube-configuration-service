package io.scalecube.configuration.repository.couchbase;

//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import io.scalecube.configuration.repository.Document;
//import java.util.Collection;
//import java.util.stream.IntStream;
//import org.junit.jupiter.api.Test;

class CouchbaseDataAccessTest {
//  final CouchbaseDataAccess  dataAccess = new CouchbaseDataAccess();
//  final static String namespace = "myorg";
//  final static String repository = "test";
//  @Test
//  void createRepository() {
//    boolean created = false;dataAccess.createRepository(namespace,repository);
//    boolean deleted = dataAccess.deleteRepository(namespace,repository);
//    assertTrue(created);
//    assertTrue(deleted);
//  }
//
//  @Test
//  void get() {
//    dataAccess.createRepository(namespace,repository);
//    try {
//    dataAccess.put(namespace,repository, "key",
//        Document.builder()
//            .id("0")
//            .key("key")
//            .value("config value")
//            .build());
//      Document document = dataAccess.get(namespace, repository, "key");
//      assertNotNull(document);
//      assertEquals(document.key(), "key");
//      assertEquals(document.value(), "config value");
//    } finally {
//      dataAccess.deleteRepository(namespace,repository);
//    }
//  }
//
//  @Test
//  void put() {
//    dataAccess.createRepository(namespace,repository);
//    try {
//      dataAccess.put(namespace,repository, "key",
//          Document.builder()
//              .id("0")
//              .key("key")
//              .value("config value")
//              .build());
//      Document document = dataAccess.get(namespace, repository, "key");
//      assertNotNull(document);
//      assertEquals(document.key(), "key");
//      assertEquals(document.value(), "config value");
//    } finally {
//      dataAccess.deleteRepository(namespace,repository);
//    } }
//
//  @Test
//  void remove() {
//    try {
//      dataAccess.createRepository(namespace,repository);
//      dataAccess.put(namespace,repository, "key",
//          Document.builder()
//              .id("0")
//              .key("key")
//              .value("config value")
//              .build());
//      String id = dataAccess.remove(namespace, repository, "key");
//      assertNotNull(id);
//      assertEquals(id, "key");
//    } finally {
//      dataAccess.deleteRepository(namespace,repository);
//    }
//  }
//
//  @Test
//  void entries() {
//    try {
//      dataAccess.createRepository(namespace, repository);
//      IntStream.range(0, 5)
//          .mapToObj(i -> Document.builder()
//              .id(String.valueOf(i))
//              .key("key-" + i)
//              .value("config_value[" + i + "]")
//              .build()
//          ).forEach(d -> dataAccess.put(namespace, repository, d.key(), d));
//      Collection<Document> documents = dataAccess.entries(namespace, repository);
//      assertNotNull(documents);
//      assertEquals(documents.size(), 5);
//    } finally {
//      dataAccess.deleteRepository(namespace,repository);
//    }
//  }
}