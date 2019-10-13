package com.example;

import grim.annotations.OmitSymbol;

public class OmitMethodExample
{
  private String _name;
  private int _score;

  @OmitSymbol
  public String getName()
  {
    return _name;
  }

  @OmitSymbol( unless = "galdr.enable_scores" )
  public int getScore()
  {
    return _score;
  }

  public void doStuff()
  {
  }
}
