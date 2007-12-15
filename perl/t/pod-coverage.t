#!perl
# $Id: pod-coverage.t 4186 2007-07-11 02:15:56Z rvosa $
use Test::More;
eval "use Test::Pod::Coverage 1.04";
plan skip_all => "Test::Pod::Coverage 1.04 required for testing POD coverage"
  if $@;
all_pod_coverage_ok();
