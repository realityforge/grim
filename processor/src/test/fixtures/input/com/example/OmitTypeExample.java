package com.example;

import grim.annotations.OmitType;

@OmitType( when = "arez.environment=production" )
@OmitType( unless = "spritz.enable_spies" )
public class OmitTypeExample
{
  private String _name;
}
