package io.scalecube.config.service.example;

class Person {
  public Person() {}

  private String name;
  private Integer age;

  @Override
  public String toString() {
    return "my name is " + name + " and my age is " + age;
  }
}