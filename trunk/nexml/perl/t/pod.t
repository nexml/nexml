#!perl
# $Id: pod.t 4186 2007-07-11 02:15:56Z rvosa $
use Test::More;
eval "use Test::Pod 1.14";
plan skip_all => "Test::Pod 1.14 required for testing POD" if $@;
all_pod_files_ok();
