package com.example;

import grim.annotations.OmitPattern;

@OmitPattern( pattern = "^_name$", unless = "arez.enable_names" )
@OmitPattern( pattern = "^_name$", when = "arez.environment=production" )
@OmitPattern( pattern = "^_name$", unless = "arez.enable_verify" )
public class OmitPatternExample
{
  private String _name;
}
