@OmitPattern( symbol = "^_name$", unless = "arez.enable_names" )
@OmitPattern( symbol = "^_name$", when = "arez.environment=production" )
@OmitPattern( symbol = "^_context$", unless = "arez.enable_zones" )
@OmitPattern( type = ".*\\.Arez_.*", symbol = "^toString$", unless = "arez.enable_names" )
@KeepPattern( type = "^arez\\.ArezContextHolder$", symbol = "^\\$clinit$", unless = "arez.enable_zones" )
@KeepPattern( type = "^arez\\.ArezZoneHolder$", symbol = "^\\$clinit$", when = "arez.enable_zones" )
package com.example;

import grim.annotations.KeepPattern;
import grim.annotations.OmitPattern;
