#!/usr/bin/perl 
# ************************************************************
# BrunchClust algorithm
# version 1.01 November 10, 2006
#
# Copyright Maria Poptsova and J. Peter Gogarten 2006-2007
#
# E-mail: maria.poptsova@uconn.edu
# http://www.bioinformtics.org/branchclust/
#
# ************************************************************
# Program requires gi-numbers recongnition file, gi_numbers.out, which will contain
# taxa identification table (by gi_numbers, or specific names)
# The web-site contains an examples of how to create taxa recognition files.
# You can also download examples as one archive:
# http://bioinformatics.org/branchclust/BranchClust_all.tgz
#
# !!! IMPORTANT NOTE: - nu of different taxa is defined by the number of lines in the file gi_numbers.out
# make sure that each line represent one taxa
#
# ************************************************************
# history:
# version 1.00 - June, 2006
# ************************************************************
# changes made to the version 1.00
#
# - clusters are not removed from the tree but marked with leaf "R" that will serve as a "stopper"
# this allows to avoid combination of paraphyletic branches
# ************************************************************
use Bio::TreeIO;
use Bio::Phylo::Util::Logger;
use Getopt::Long;
use warnings;
use strict;

my $logger = Bio::Phylo::Util::Logger->new();
my $clusters_out_file   = 'clusters.out';
my $families_list_file  = 'families.list';
my $cluster_log_file    = 'cluster.log';
my $gi_numbers_out_file = 'gi_numbers.out';
my $verbosity           = 3;
my ( $tree_file, $MANY );

GetOptions(
	'verbose'        => sub { $verbosity++ },
	'clusterfile=s'  => \$clusters_out_file,
	'familiesfile=s' => \$families_list_file,
	'logfile=s'      => \$cluster_log_file,
	'gifile=s'       => \$gi_numbers_out_file,
	'treefile=s'     => \$tree_file,
	'many=i'         => \$MANY,
);

if ( not $tree_file or not $MANY ) { 
	die "usage: $0 -t <tree-file> -m <MANY>\n" 
}


# ------------------------------------------------------------------
open( CLUSTERS, '>', $clusters_out_file )  or die "can't open file clusters.out: $!\n";
open( FAMILIES, '>', $families_list_file ) or die "can't open file families.list: $!\n";

{
	open my $log, '>', $cluster_log_file or die "can't open file ${cluster_log_file}: $!\n";
	$logger->set_listeners(
		sub {
			my ($msg) = @_;
		    if ( $log->opened ) {
		    	print $log $msg;
		    }
		}
	); 
	$logger->VERBOSE(
		'-level' => $verbosity,
		'-class' => __PACKAGE__,
	);
	$logger->info("===================$0 logging ready==================");
}

#-----------------------------------------------------------------------------------
#-----------------------------------------------------------------------------------
# Beginning of the program
#
#
# read gi-table into the array
my @gis_table = ();
read_gis_table( \@gis_table );
my $nu_of_species = @gis_table;

# create a tree object
my $input = new Bio::TreeIO(
    '-file'   => $tree_file,
    '-format' => 'newick',
);
my $tree = $input->next_tree;
my $root_id;

# check if the number of species in the tree exactly the same as the number of species
# if so, we just print them as a completed family and exit
my @leaves = grep { $_->is_Leaf && defined $_->id } $tree->get_root_node->get_all_Descendents;
if ( @leaves == $nu_of_species ) {

    # print as is
    my $cycle = 3;
    my @family;
    empty_family( \@family );
    my @empty = ();
    foreach my $member (@leaves) {
        push( @family, $member->id );
    }
    print_cluster( \@family, \@family, \@empty, \@empty, $cycle );
    $logger->info('number of leaves in a tree is equal to the number of different species');
	$logger->info('no parsing is done... the whole tree was reported as family');
    close(CLUSTERS);
    close(FAMILIES);
    exit;
}

#--------------------------------------------------------------------------------
# calibrate the tree
# find the most distant leaf, re-root the tree with its ancestor,
# then find the most distant leaf again and re-root again
my $distant_leaf = distant_leaf( \$tree );
my $id1          = $distant_leaf->id;
$tree->reroot( $distant_leaf->ancestor );
$distant_leaf = distant_leaf( \$tree );
my $id2          = $distant_leaf->id;
$tree->reroot( $distant_leaf->ancestor );

#----------------------------------------------------------------------------------
# We will run the selection three times, first with the root 1, then with the root 2,
# and finally that selection is accepted that contains the least number of paralogs.
my @total_par;
@total_par[1..3] = (0,0,0);
my $cycle;
for $cycle ( 1 .. 3 ) {
    $logger->info("*** Iteration $cycle ***");
	my $id;
    if ( $cycle == 1 ) {
        $id = $id1;
    }
    if ( $cycle == 2 ) {
        $id = $id2;
    }
    if ( $cycle == 3 ) {

        # choose which selection contains the least number of paralogs
        if ( $total_par[1] < $total_par[2] ) {
            $id = $id1;
        }
        else {
            $id = $id2;
        }
        print CLUSTERS "Number of paralogs in run 1: $total_par[1]\n";
        $logger->info("Number of paralogs in run 1: $total_par[1]");
        print CLUSTERS "Number of paralogs in run 2: $total_par[2]\n";
        $logger->info("Number of paralogs in run 2: $total_par[2]");
        if ( $total_par[2] < $total_par[1] ) {
            print CLUSTERS "Run 2 is chosen\n";
            $logger->info('Run 2 is chosen');
        }
        else {
            print CLUSTERS "Run 1 is chosen\n";
            $logger->info('Run 1 is chosen');
        }
    }
    $distant_leaf = $tree->find_node( '-id' => $id );
    $tree->reroot( $distant_leaf->ancestor );
    my $start_node   = distant_leaf( \$tree );
    my $root         = $tree->get_root_node;
    $root_id         = $root->internal_id;
    my $current_node = $start_node;

    # to stop selection set $true to 0
    my $true = 1;
    my $node1;
    while ($true) {
        my @family;
        empty_family( \@family );

        # recursion cycle
        # while current_node is not a cluster
        my $if_cluster;
        while ( !$if_cluster ) {
            $node1 = $current_node;

            # if we already at the root then quit
            if ( $node1->internal_id == $root_id ) {
                $true = 0;
                #$logger->info('tree is processed completely... done');
                last;
            }

            # the same as with the path, we need to
            # do the last iteration of the cycle
            $if_cluster = analyze_node1( \$tree, \$node1 );

            # descend to the next node
            $current_node = $node1->ancestor;
        }
        $if_cluster = 0;

       #------------------------------------------------------------------------
        my @genes_node1 = @{ get_cluster_from_node( \$tree, \$node1 ) };
        my @cluster     = @genes_node1;
        my @refs        = extract_family( \@cluster );
        my $ref_fam     = $refs[0];
        my $ref_par     = $refs[1];
        #my @family      = @$ref_fam; XXX declared on line 147
        @family      = @$ref_fam;
        my @paralogs    = @$ref_par;
        my @out_par =
          @{ get_out_of_cluster_paralogs( \$tree, \$node1, \@family, \@cluster )
          };
        print_cluster( \@cluster, \@family, \@paralogs, \@out_par, $cycle );

    #------------------------------------------------------------------------
    #
    # cluster on the Node 1 is reported
    # Node 1 is removed from the tree, tree is rerooted with the Node1' ancestor
    # and selection continues
    #
        if ($true) {
            my $praancestor = $node1->ancestor;
            $tree->remove_Node($node1);

            # November 2006 -> add an extra node "R"
            my $mark_node = Bio::Tree::Node->new( '-id' => 'R' );
            $praancestor->add_Descendent($mark_node);

            # end of November 2006 insertion
            # check if tree still has leaves
            my @des =
              grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
              $tree->get_root_node->get_all_Descendents;

            # no leaves left:
            if ( @des == 0 ) {

                # finishing clustering
                $true = 0;
            }
            else {

                # if praancestor is already a root
                # then no need to reroot the tree
                if ( $praancestor->internal_id != $root_id ) {
                    $tree->reroot($praancestor);
                    $root_id = $tree->get_root_node->internal_id;
                }
                $current_node = distant_leaf( \$tree )->ancestor;
            }
        }
    }    # while(true)
    $tree = {};
    my $input1 = new Bio::TreeIO(
        '-file'   => "$tree_file",
        '-format' => 'newick',
    );
    $tree = $input1->next_tree;
}    # for(cycle)
close(CLUSTERS);
close(FAMILIES);

# --------------------------------------------------------------------
# -------------------------------------------------------------------
# read gi-numbers patterns to the specified array
sub read_gis_table {
	$logger->debug;
    my $ref = $_[0];
    open my $gis, '<', 'gi_numbers.out' or die "can't open file gi_numbers.out: $!\n";      
    my @gis_legend = <$gis>;
    close $gis;
    for my $i ( 0 .. $#gis_legend ) {

        # check for empty strings
        if ( $gis_legend[$i] ne '' ) {
            chomp $gis_legend[$i];

            # remove species name
            $gis_legend[$i] =~ s/^(.+?)\|\s+//;
            my @words = split /\s+/, $gis_legend[$i];

            # check for empty elements
            for my $j ( 0 .. $#words ) {
                if ( $words[$j] eq '' ) {
                    die "empty record in gi_numbers.out - check file\nprogram aborted...\n";
                }
            }
            if ( @words ) {
                push @$ref, \@words;
            }
        }
    }
}

# end of read_gis_table ----------------------------------------------
# -------------------------------------------------------------------
# calculates the number of different taxa in an array of genes
# ref to an array - argument
sub nu_of_taxa {
	$logger->debug;
    my $ref        = $_[0];
    my $taxa_count = 0;
    my $gi;
  LINE: for my $i ( 0 .. $#gis_table ) {
        for my $j ( 0 .. $#{ $gis_table[$i] } ) {
            for $gi (@$ref) {
                if ( $gi =~ /$gis_table[$i][$j]/ ) {
                    $taxa_count++;
                    next LINE;
                }
            }
        }
    }
    return $taxa_count;
}

# end of nu_of_taxa--------------------------------------------------
#--------------------------------------------------------------------
# returns species index according to gi-numbers' table
# gi-number as an argument
sub species_index {
	$logger->debug;
    my $species = $_[0];
    my $index   = -1;
    for my $i ( 0 .. $#gis_table ) {
        for my $j ( 0 .. $#{ $gis_table[$i] } ) {
            if ( $species =~ /$gis_table[$i][$j]/ ) {
                $index = $i;
                last;
            }
        }
        if ( $index != -1 ) {
            last;
        }
    }
    return $index;
}

# end of species_index -----------------------------------------------
#-------------------------------------------------------------------
# returns the ref to the most distant leaf in a given tree
#
sub distant_leaf {
    my $ref_to_tree = $_[0];
    my $mytree      = $$ref_to_tree;
    my @leaves =
      grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
      $mytree->get_root_node->get_all_Descendents;
    my @len;
    my @tips;
    for my $node (@leaves) {
        my @path;
        my $name = $node->id;
        if ( defined($name) ) {
            while ( defined $node ) {
                push @path, $node->internal_id;
                $node = $node->ancestor;
            }
            push @tips, $name;
            push @len,  scalar @path;
        }
    }
    my ( $n, $index ) = ( 0, -1 );
    for my $i ( 0 .. $#len ) {
        if ( $len[$i] > $n ) {
            ( $n, $index ) = ( $len[$i], $i );
        }
    }    
    my $myleaf = $mytree->find_node( $tips[$index] );
    $logger->info(
    	sprintf(
    		'Path %s is longest (length=%s), for leaf "%s"',
    		$index,
    		$n,
    		$myleaf->id,
    	)
    );
    return $myleaf;
}

# end of distant_leaf------------------------------------------------
#-------------------------------------------------------------------
# for a given node returns array of genes in order starting from the most distant
# and then adding others while descending
# arguments - ref to tree and ref to the node
sub get_cluster_from_node {
	$logger->debug;
    my ( $ref_mytree, $ref_cluster_node ) = @_;
    my $mytree       = $$ref_mytree;
    my $cluster_node = $$ref_cluster_node;
    my $flag         = 1;
    my ( @leaves, @cluster, @path, @tips, @len );    
    @leaves =
      grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
      $cluster_node->get_all_Descendents;

    if ( @leaves == 0 ) {
        return \@cluster;
    }

    # find the most distant
    for my $node (@leaves) {
        if ( defined( $node->id ) ) {
            my $name = $node->id;
            while ( $node->internal_id != $cluster_node->internal_id ) {
                push @path, $node->internal_id;
                $node = $node->ancestor;
            }

            # because we didn't completed the last cycle when
            # ($node->internal_id!=$cluster_node->internal_id)
            # add cluster_node at the end
            push @path, $node->internal_id;
            push @tips, $name;
            push @len,  scalar @path;
        }
    }
    my ( $n, $index ) = ( 0, 0 );
    for my $i ( 0 .. $#len ) {
        if ( $len[$i] > $n ) {
            ( $n, $index ) = ( $len[$i], $i );
        }
    }
    my $myleaf = $mytree->find_node( $tips[$index] );

    # myleaf is the most distant leaf in this cluster
    # we start selction from this leaf
    if ( defined($myleaf) ) {
        @leaves = ();
        my $node       = $myleaf;
        my $node_id    = $node->internal_id;
        my $stop_point = $cluster_node->internal_id;
        while ( $node_id != $stop_point ) {
            @leaves =
              grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
              $node->get_all_Descendents;

            # check if it is already in the array
            for my $leaf (@leaves) {
                if ( defined( $leaf->id ) ) {
                    my $leaf_name = $leaf->id;
                    my @found = grep( /$leaf_name/, @cluster );
                    if ( @found < 1 ) {
                        push @cluster, $leaf_name;
                    }
                }
            }
            $node    = $node->ancestor;
            $node_id = $node->internal_id;
        }

      # the same as with the path, we need to do the last iteration of the cycle
        @leaves =
          grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
          $node->get_all_Descendents;

        # check if it is already in the array
        for my $leaf (@leaves) {
            if ( defined( $leaf->id ) ) {
                my $leaf_name = $leaf->id;
                my @found = grep( /$leaf_name/, @cluster );
                if ( @found < 1 ) {
                    push @cluster, $leaf_name;
                }
            }
        }
        return \@cluster;
    }
    else {
        die
"get_cluster_from_node: cannot find leaf: $tips[$index]\nProgram aborted...\n";
    }
}

# end of get_cluster_from_node----------------------------------------
#--------------------------------------------------------------------
# for a given node and selected family returns out-of_cluster paralogs,
# i.e. those genes that are located after the node containing family
# 4 arguments: ref to the tree, ref to the node,
#              ref to the array-cluster and to the array-family
#
sub get_out_of_cluster_paralogs {
	$logger->debug;
    my ( $ref_mytree, $ref_cluster_node, $ref_fam, $ref_cluster ) = @_;
    my $mytree       = $$ref_mytree;
    my $cluster_node = $$ref_cluster_node;
    my @fam          = @$ref_fam;
    my @cluster      = @$ref_cluster;
    my @outpar       = ();
    my @leaves       = ();
    my @leaves_names = ();
    my @path         = ();
    my @tips         = ();
    my @len          = ();
    my $flag         = 1;
    @leaves =
      grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
      $cluster_node->get_all_Descendents;

    if ( scalar @leaves == 0 ) {
        return \@outpar;
    }

    # find the most distant leaf for a given cluster
    for my $node (@leaves) {
        @path = ();
        if ( defined( $node->id ) ) {
            my $name = $node->id;

            # check if this leaf belongs to a cluster
            my @found = grep( /$name/, @cluster );
            if ( @found > 0 ) {
                while ( $node->internal_id != $cluster_node->internal_id ) {
                    push @path, $node->internal_id;
                    $node = $node->ancestor;
                }
                push @path, $node->internal_id;
                push @tips, $name;
                push @len,  scalar @path;
            }
        }
    }
    my $n     = 0;
    my $index = 0;
    for ( my $i = 0 ; $i < @len ; $i++ ) {
        if ( $len[$i] > $n ) {
            $n     = $len[$i];
            $index = $i;
        }
    }
    my $leaf; # XXX weirdly scoped variables
    my $myleaf = $mytree->find_node( $tips[$index] );
    if ( defined($myleaf) ) {
        @leaves = ();
        my $node       = $myleaf;
        my $node_id    = $node->internal_id;
        my $stop_point = $cluster_node->internal_id;
        my ( $complete ); # XXX weirdly scoped variables
        while ( $node_id != $stop_point ) {
            @leaves =
              grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
              $node->get_all_Descendents;

            # we start descending from the most distant leaf
            # and we want to check if the node contains all the family
            @leaves_names = ();
            for $leaf (@leaves) {
                if ( defined( $leaf->id ) ) {
                    push( @leaves_names, $leaf->id );
                }
            }
            $complete = 1;
            foreach my $member (@fam) {
                if ( $member ne "" ) {
                    my @found = grep( /$member/, @leaves_names );
                    if ( @found == 0 ) {
                        $complete = 0;
                    }
                }
            }
            if ($complete) {
                last;
            }
            $node    = $node->ancestor;
            $node_id = $node->internal_id;
        }

        # the same as with the path, we need to
        # do the last iteration of the cycle
        # BUT, if the family is not complete,
        # it means that the family will be complete
        # on the last node that contains the whole
        # cluster, and that there are no out-of cluster-paralogs
        if ( !$complete ) {
            @outpar = ();
        }
        else {

            # if all the memebers of the families were found, it means that
            # the current node contains all the family, and the next branches
            # will be all outparalogs
            # leaves_names contain now family plus in-paralogs
            # outparalogs are cluster minus leaves_names
            @outpar = @{ diff_members( \@cluster, \@leaves_names ) };
            my $outpar = join( " ", @outpar );
        }
        return \@outpar;
    }
    else {
        die "get_out_of_cluster_paralogs: cannot find $leaf: $tips[$index]\n";
    }
}

# end of get_out_of_cluser_paralogs----------------------------------
#-------------------------------------------------------------------
# takes two arrays (as ref) as arguments
# returns an array with common members
sub common_members {
	$logger->debug;
    my ( $ref1, $ref2 ) = @_;
    my @fam1   = @$ref1;
    my @fam2   = @$ref2;
    my @result = ();
    foreach my $member (@fam1) {
        my @temp = grep /$member/, @fam2;
        push @result, @temp;
    }
    return \@result;
}

# end of common_members----------------------------------------------
#--------------------------------------------------------------------
# takes two arrays (as ref) as arguments
# return the array with different members
sub diff_members {
	$logger->debug;
    my ( $ref1, $ref2 ) = @_;
    my @fam1   = @$ref1;
    my @fam2   = @$ref2;
    my @result = ();
    foreach my $member (@fam1) {
        my @temp = grep /$member/, @fam2;
        if ( @temp == 0 ) {
            push @result, $member;
        }
    }
    foreach my $member (@fam2) {
        my @temp = grep /$member/, @fam1;
        if ( @temp == 0 ) {
            push @result, $member;
        }
    }
    return \@result;
}

# end of diff_members-----------------------------------------------
#---------------- ---------------------------------------------------
# reorder cluster from a branch in order from the most distant leaf
# towards the root
# arguments - ref to the tree, node with a cluster and the array-cluster
sub reorder_branch {
	$logger->debug;
    my ( $ref_mytree, $ref_cluster_node, $ref_cluster ) = @_;
    my $mytree       = $$ref_mytree;
    my $cluster_node = $$ref_cluster_node;
    my @cluster      = @$ref_cluster;
    my @leaves       = ();
    my @branch       = ();
    my @path         = ();
    my @tips         = ();
    my @len          = ();
    my $flag         = 1;
    @leaves =
      grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
      $cluster_node->get_all_Descendents;

    # find the most distant leaf for a given cluster
    # cluster can be part of leaves (branch)
    for my $node (@leaves) {
        @path = ();
        if ( defined( $node->id ) ) {
            my $name = $node->id;

            # check if this leaf belongs to a cluster
            my @found = grep( /$name/, @cluster );
            if ( @found > 0 ) {
                while ( $node->internal_id != $cluster_node->internal_id ) {
                    push @path, $node->internal_id;
                    $node = $node->ancestor;
                }
                push @path, $node->internal_id;
                push @tips, $name;
                push @len,  scalar @path;
            }
        }
    }
    my $n     = 0;
    my $index = 0;
    for ( my $i = 0 ; $i < @len ; $i++ ) {
        if ( $len[$i] > $n ) {
            $n     = $len[$i];
            $index = $i;
        }
    }
    my $myleaf = $mytree->find_node( $tips[$index] );

    # myleaf is the most distant leaf in this cluster
    # we start selction from this leaf
    if ( defined($myleaf) ) {
        @leaves = ();
        my $node       = $myleaf;
        my $node_id    = $node->internal_id;
        my $stop_point = $cluster_node->internal_id;
        while ( $node_id != $stop_point ) {
            @leaves =
              grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
              $node->get_all_Descendents;

            # check if it is already in the array
            foreach my $leaf (@leaves) {
                if ( defined( $leaf->id ) ) {
                    my $leaf_name = $leaf->id;

                    # add only leafs from a branch (@cluster here)
                    my @found_cl = grep( /$leaf_name/, @cluster );
                    if ( @found_cl > 0 ) {
                        my @found = grep( /$leaf_name/, @branch );
                        if ( @found < 1 ) {
                            push @branch, $leaf_name;
                        }
                    }
                }
            }
            $node    = $node->ancestor;
            $node_id = $node->internal_id;
        }

      # the same as with the path, we need to 
      # do the last iteration of the cycle
        @leaves =
          grep { $_->is_Leaf && defined $_->id && $_->id ne 'R' }
          $node->get_all_Descendents;

        # check if it is already in the array
        foreach my $leaf (@leaves) {
            if ( defined( $leaf->id ) ) {
                my $leaf_name = $leaf->id;
                my @found_cl = grep( /$leaf_name/, @cluster );
                if ( @found_cl > 0 ) {
                    my @found = grep( /$leaf_name/, @branch );
                    if ( @found < 1 ) {
                        push @branch, $leaf_name;
                    }
                }
            }
        }
        return \@branch;
    }
    else {
        die
"reorder branch: cannot find leaf $myleaf: $tips[$index]\nProgram aborted.\n";
    }
}

# end of reorder_branch--------------------------------------------
#--------------------------------------------------------------------
# reporting cluster
# arguments: ref to array of cluster, ref to family,
#            ref to inparalogs and out-of-cluster paralogs
sub print_cluster {
	$logger->debug;
    my ( $ref_cluster, $ref_fam, $ref_par, $ref_outpar, $cycle ) = @_;
    my @cluster   = @$ref_cluster;
    my @fam       = @$ref_fam;
    my @paralog   = @$ref_par;
    my @out_par   = @$ref_outpar;
    my @in_par    = @{ diff_members( \@paralog, \@out_par ) };
    my $clustline = join( " ", @cluster );
    my $paralogs  = join( " ", @paralog );
    my $out_par   = join( " ", @out_par );
    my $in_par    = join( " ", @in_par );

    # this is global variable:
    if ( $total_par[$cycle] < scalar @paralog ) {
    #if ( $total_par[$cycle] < scalar @paralogs ) { XXX this looked like a typo
        $total_par[$cycle] = scalar @paralog;
    }

    # we print output only on the third cycle
    if ( $cycle == 3 ) {

        # print cluster
        print CLUSTERS "\n------------ CLUSTER -----------\n";
        print CLUSTERS "$clustline\n";

        # print family
        print CLUSTERS "\n------------ FAMILY ------------\n";
        my $count = 0;
        foreach my $gene (@fam) {

            # print "&&$gene \n";
            if ( $gene ne "" ) {
                print CLUSTERS "$gene ";
                print FAMILIES "$gene ";
                $count++;
            }
        }
        print FAMILIES "\n";
        print CLUSTERS "\n";
        my $complete;
        if ( $count == $nu_of_species ) {
            $complete = "COMPLETE";
        }
        else {
            $complete = "INCOMPLETE";
        }
        print CLUSTERS "$complete: $count\n";
        print FAMILIES "$complete: $count\n";
        if (@paralog) {
        #if (@paralogs) { XXX this looked like a typo
            print CLUSTERS "\n>>>>> IN-PARALOGS -----------\n";
            print CLUSTERS "      $in_par\n";
            print CLUSTERS "\n<<<<< OUT-OF_CLUSTER PARALOGS -----------\n";
            print CLUSTERS "      $out_par\n";
        }
        print CLUSTERS "\n";
    }
}

# end of print_cluster-----------------------------------------------
#--------------------------------------------------------------------
# takes a ref to an array of genes and extarct family ( even uncomplete)
# return ref to the array family and array with paralogs
sub extract_family {
	$logger->debug;
    my ($ref_cluster) = @_;
    my @cluster       = @$ref_cluster;
    my ( @family, @paralogs );
    for my $k ( 0 .. $nu_of_species - 1 ) {
        $family[$k] = "";
    }
    $logger->info( 'genes: ' . join ', ', @cluster );
    for my $gene (@cluster) {    	
        my $index = species_index($gene);
        if ( $index == -1 && $gene ne "R" ) {
            die
              "Error finding species index. Gene -> $gene. Program aborted.\n";
        }

        # array family is filled with gi-numbers only once
        if ( $family[$index] eq "" ) {
            $family[$index] = $gene;
        }

        # if we already have the entry for this species
        # we write this entry to the list of paralogs
        if ( ( $family[$index] ne "" ) && ( $family[$index] ne $gene ) ) {
            push( @paralogs, $gene );
        }
    }
    my @refs = ();
    push @refs, ( \@family, \@paralogs );
    return @refs;
}

# end of extract_family-------------------------------------------------------------
#-----------------------------------------------------------------------------------
sub empty_family {
	$logger->debug;
    my ($ref) = @_;
    for ( my $k = 0 ; $k < $nu_of_species ; $k++ ) {
        $$ref[$k] = "";
    }
}

# end of empty_family--------------------------------------------------------------
#-----------------------------------------------------------------------------------
# Recursion
# if Node1 is a cluster (complete or incomplete) then function returns 1,
# if we descend further - return 0
# arguments - ref to tree and to node
sub analyze_node1 {
	$logger->debug;
    my ( $ref_tree, $ref_node1 ) = @_;

    # get information about node 1
    my @genes_node1 = @{ get_cluster_from_node( $ref_tree, $ref_node1 ) };
    $logger->debug( "number of descendents is " . scalar(@genes_node1) );
    my $nu_branch1 = nu_of_taxa( \@genes_node1 );
    $logger->debug("nu of taxa in this line is $nu_branch1");

    # if nu_of_taxa >=  $nu_of_species then node 1 is complete
    if ( $nu_branch1 >= $nu_of_species ) {
        $logger->debug("Node 1 contains more than number of species ($nu_branch1>=$nu_of_species) species. Node 1 is complete, leaving cycle");
        return 1;
    }

    # get information about node 2
    my $node2 = $$ref_node1->ancestor;

    # November 2006 -> check if the node2 contains a stopper
    # if it has then we report node1 as a cluster
    # we should check if the ancestor of the node1 (not the
    # node1 itself) contains stoppers, because stoppers can
    # be contained together with the leaves
    #my @stopeers = ();
    my @stoppers =
      grep { $_->is_Leaf && $_->id eq 'R' } $node2->get_all_Descendents;
    if ( ( scalar @stoppers > 0 ) && ( $node2->internal_id != $root_id ) ) {
        $logger->info('Stopper (removed cluster) found. Node 1 is a cluster. Leave the cycle');
        return 1;
    }

    # end of November 2006 insertion
    my @genes_node2 = ();
    @genes_node2 = @{ get_cluster_from_node( $ref_tree, \$node2 ) };

    # branch2 - is the branch that was added by descending to the node2
    my @genes_branch2 = @{ diff_members( \@genes_node1, \@genes_node2 ) };
    my $nu_branch2 = nu_of_taxa( \@genes_branch2 );

    # if branch 2 conatins A LOT of species, we have two options that depend on
    # how many species was on Node 1
    if ( $nu_branch2 >= $MANY ) {
        $logger->info("branch 2 contains MANY ($nu_branch2) species. Analyzing Node 1 ...");

        # if Node 1 contains A LOT of species
        if ( $nu_branch1 >= $MANY ) {
        	$logger->info("Node 1 contains MANY ($nu_branch1) species. Node 1 is incomplete cluster. Leave the cycle");
            return 1;
        }

        # if Node 1 contains FEW of species
        else {
        }
    }
    else {

        # if the branch 2 contains FEW species, we descend further
        $logger->debug("branch 2 contains FEW ($nu_branch2) species, we add it to the cluster. Continue...");
    }
    return 0;
}

# end of recursion------------------------------------------------------------------