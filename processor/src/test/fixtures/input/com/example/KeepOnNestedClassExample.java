package com.example;

import grim.annotations.KeepType;

public class KeepOnNestedClassExample
{
  @KeepType( when = "arez.environment=production" )
  static class Foo
  {
  }

  static class Bar
  {
  }
}
