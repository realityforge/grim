package com.example;

import grim.annotations.KeepSymbol;

public class KeepFieldExample
{
  @KeepSymbol
  private String _name;
  @KeepSymbol( unless = "galdr.environment=development" )
  private int _score;
  private int _age;
}
