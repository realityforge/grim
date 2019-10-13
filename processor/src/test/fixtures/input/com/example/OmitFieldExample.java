package com.example;

import grim.annotations.OmitSymbol;

public class OmitFieldExample
{
  @OmitSymbol
  private String _name;
  @OmitSymbol( unless = "galdr.environment=development" )
  private int _score;
  private int _age;
}
