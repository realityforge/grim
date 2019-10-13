package com.example;

import grim.annotations.OmitSymbol;
import javax.annotation.Nullable;

public final class OmitConstructorExample
{
  @OmitSymbol
  private OmitConstructorExample()
  {
  }

  @OmitSymbol( unless = "galdr.enable_names" )
  private OmitConstructorExample( @Nullable final String name )
  {
  }

  public OmitConstructorExample( int v )
  {
  }
}
