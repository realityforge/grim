package com.example;

import grim.annotations.OmitType;

public class OmitOnNestedClassExample
{
  @OmitType( when = "arez.environment=production" )
  static class Foo
  {
  }

  static class Bar
  {
  }
}
