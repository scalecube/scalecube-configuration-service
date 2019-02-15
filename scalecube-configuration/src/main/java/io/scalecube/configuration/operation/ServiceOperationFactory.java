package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.SaveRequest;

public class ServiceOperationFactory {

  public static ServiceOperation<CreateRepositoryRequest, Acknowledgment> createRepository() {
    return new CreateRepository();
  }

  public static ServiceOperation<FetchRequest, FetchResponse> fetch() {
    return new FetchEntry();
  }

  public static ServiceOperation<FetchRequest, FetchResponse> fetchAll() {
    return new FetchEntries();
  }

  public static ServiceOperation<SaveRequest, Acknowledgment> saveEntry() {
    return new SaveEntry();
  }

  public static ServiceOperation<DeleteRequest, Acknowledgment> deleteEntry() {
    return new DeleteEntry();
  }
}
