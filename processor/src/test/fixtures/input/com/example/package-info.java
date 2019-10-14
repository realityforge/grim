@OmitPattern( symbol = "^_name$", unless = "arez.enable_names" )
@OmitPattern( symbol = "^_name$", when = "arez.environment=production" )
@OmitPattern( symbol = "^_context$", unless = "arez.enable_zones" )
@OmitPattern( type = ".*\\.Arez_.*", symbol = "^toString$", unless = "arez.enable_names" )
package com.example;

import grim.annotations.OmitPattern;