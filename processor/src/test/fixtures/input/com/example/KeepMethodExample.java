package com.example;

import grim.annotations.KeepSymbol;

public class KeepMethodExample
{
  private String _name;
  private int _score;

  @KeepSymbol
  public String getName()
  {
    return _name;
  }

  @KeepSymbol( unless = "galdr.enable_scores" )
  public int getScore()
  {
    return _score;
  }

  public void doStuff()
  {
  }
}
