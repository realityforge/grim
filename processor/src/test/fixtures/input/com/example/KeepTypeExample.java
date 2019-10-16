package com.example;

import grim.annotations.KeepType;

@KeepType( when = "arez.environment=production" )
@KeepType( unless = "spritz.enable_spies" )
public class KeepTypeExample
{
  private String _name;
}
