#!perl
# $Id$
use Test::More;
plan skip_all => 'env var TEST_AUTHOR not set' if not $ENV{'TEST_AUTHOR'};
eval "use Test::Pod::Coverage 1.04";
plan skip_all => "Test::Pod::Coverage 1.04 required for testing POD coverage"
  if $@;
all_pod_coverage_ok();
