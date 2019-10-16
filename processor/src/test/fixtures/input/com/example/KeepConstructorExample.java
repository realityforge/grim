package com.example;

import grim.annotations.KeepSymbol;
import javax.annotation.Nullable;

public final class KeepConstructorExample
{
  @KeepSymbol
  private KeepConstructorExample()
  {
  }

  @KeepSymbol( unless = "galdr.enable_names" )
  private KeepConstructorExample( @Nullable final String name )
  {
  }

  public KeepConstructorExample( int v )
  {
  }
}
