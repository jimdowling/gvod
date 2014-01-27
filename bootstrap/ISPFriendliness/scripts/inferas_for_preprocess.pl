#!/usr/bin/perl

$ftier1      =  "Tier1.txt";

#### Read Command Lines
if( $#ARGV!=3 )
{
    print "\n Usages:  (need 4 parameters)";
    print "\n     perl inferas_for_preprocess.pl list pairs refined_asmap output";
    print "\n";
    exit;
}

$fconnlist = $ARGV[0];
$fconnpairs = $ARGV[1];
$fmap = $ARGV[2];
$fout = $ARGV[3];

for ( $i=1; $i<66000; $i++ )
{
    $deg[$i] = 0;
}

#
#Read each line and put the degree of each node into array degree.
#
open(FILE, $fconnlist);
while (not eof FILE)
{
    $tmp =<FILE>;
    chomp($tmp);
    @input=split(/\s+/,$tmp);

    $connset=":";
    @asset = split(/:/, $input[3]);
    $count = 0;
    $i = 1;
    while ( $asset[$i] != 0 )
    {
        $key = join $asset[$i], ":", ":";
        if ( $asset[$i]!=$input[0] && not ($connset =~ $key ) )
        {
            $connset = join $asset[$i], $connset, ":";
            $count = $count +1;
        }
        $i = $i+1;
        ###  add by Jianhong Xia
        if( $i>$#asset )
        {
            last;
        }
        ###
    }

    $deg[$input[0]] = $count;
}
close( FILE );

#
# initialize counters for transite of each pair
#
open(FILE, $fconnpairs);
while(<FILE>)
{
    ($src, $mid, $dst)=split(/\s+/,$_);
    $times{$src, $dst}= 0;
}
close(FILE);


#
# First pass, find the exception pair.
# by using the highest degree AS as provider, the rest are customerheuristics
# if any pair does not conform to this in degree, then we set the pair asexception
# pair
#

open(FILE,$fmap);

## Read till the line"    Network          Next Hop            Metric LocPrf Weight Path"
##        $tmp =<FILE>;
##        while ( not ($tmp =~ /LocPrf/))
##        {
##          $tmp =<FILE>;
##        }
##

#Read each line
while (not eof FILE)
{
    $tmp =<FILE>;
    chomp($tmp);
    @path=split(/\s+/, $tmp);

    #
    #find the max degree AS
    #
    $max = 0;
    $maxi = $i;
    $i=0;
    while ( not ($path[$i] =~ "{")  )
    {
        if ( $max < $deg[$path[$i]] )
        {
            $max = $deg[$path[$i]];
            $maxi = $i;
        }
        $i++;
        ### Add by Jianhong
        if( $i>$#path )
        {
            last;
        }
        ###
    }

    #
    #  Set the counter for transient for each pair
    #
    #  First going up
    #
    $i = 0;
    while ((not ($path[$i+1] =~ "{")) && ($i<$maxi) )
    {
        $times{$path[$i], $path[$i+1]}++;
        $i = $i+1;
        ### Add by Jianhong
        if( $i>$#path )
        {
            last;
        }
        ###
    }

    #
    #  Second going down
    #
    while ( not ($path[$i+1] =~ "{") )
    {
        $times{$path[$i+1], $path[$i]}++;
        $i = $i+1;
        ### Add by Jianhong
        if( $i>$#path )
        {
            last;
        }
        ###
    }
}
close(FILE);

#
#
#determine peer-peer
#
#

open(FILE, $fmap);
while (not eof FILE)
{
    $tmp =<FILE>;
    chomp($tmp);
    @path=split(/\s+/, $tmp);

    #  find the max degree AS
    $max = 0;
    $maxi = $i;
    $i = 0;

    while ( not ($path[$i] =~ "{") )
    {
        if ( $max < $deg[$path[$i]] )
        {
            $max = $deg[$path[$i]];
            $maxi = $i;
        }
        $i++;
        ### Add by Jianhong
        if( $i>$#path )
        {
            last;
        }
        ###
    }

    # first going up
    $i = 0;
    while ( (not ($path[$i+1] =~ "{") ) && ($i<$maxi-1) )
    {
        $nprivp{$path[$i], $path[$i+1]} = 1;
        $nprivp{$path[$i+1], $path[$i]} = 1;
        $i++;
        ### Add by Jianhong
        if( $i>$#path )
        {
            last;
        }
        ###
    }

    #  Get ride of AS prepend case, where the highest deg AS
    #   shows up serveral times after the first time.

    $i = $maxi +1;

    ##
    ## does not need to remove prepend, because RefineASmap.pl has taken care of it
    ## ==============************************=====================================
    ##
    ##  while ( ( not ($path[$i] =~ "{") )
    ##          && ($path[$i] ne "i") && ($path[$i] ne "?") && ($path[$i] ne "e")
    ##         && ( $path[$i] == $path[$i-1] )  )
    ##  {
    ##     $i++;
    ##    ### Add by Jianhong
    ##    if( $i>$#path )
    ##     {
    ##         last;
    ##     }
    ##    ###
    ##   }


    # choose one pair with smaller deg as non peering pair
    if( $i<=$#path && ( not ($path[$i] =~ "{") ) )
    {
        if( $maxi != 0 &&
            (not($times{$path[$i-1], $path[$i]} > 0 && $times{$path[$i],$path[$i-1]} > 0 ) &&
            not($times{$path[$maxi], $path[$maxi-1]} > 0 && $times{$path[$maxi-1],$path[$maxi]} > 0 )) )
        {
            if( $deg[$path[$maxi-1]] > $deg[$path[$i]] )
            {
                $nprivp{$path[$i-1], $path[$i]} = 1;
                $nprivp{$path[$i], $path[$i-1]} = 1;
            }
            else
            {
                $nprivp{$path[$maxi-1], $path[$i-1]} = 1;
                $nprivp{$path[$i-1], $path[$maxi-1]} = 1;
            }
        }

        #  Second going down
        while ( ( not ($path[$i+1] =~ "{") ) )
        {
            $nprivp{$path[$i], $path[$i+1]} = 1;
            $nprivp{$path[$i+1], $path[$i]} = 1;
            $i ++;
            ### Add by Jianhong
            if( $i>$#path )
            {
                last;
            }
            ###
        }
    }
}
close(FILE);

# read "Tier1.txt"
open(FILE,$ftier1);
while( not eof FILE )
{
    $tier1 =<FILE>;
}
close(FILE);


#print relationship graph

# Go through connlist and find out for every AS, you need to
# list out all its providers, customers, and sibling and peer

# For each AS i
#     For each of its connection AS j
#          check if (i,j) is a siblling i.e., check badset
#          check if (i,j) is a peer-peer
#          check if j transit for i

#open(FILE,"ASconnlist.txt.");

open(FOUT, ">$fout");

open(FILE, $fconnlist);
$providers = ":";
$customers = ":";
$siblings = ":";
$peers     = ":";

#Read each line and put the degree of each node into array degree.


while( not eof FILE)
{
    $tmp =<FILE>;
    chomp($tmp);
    @input=split(/\s+/,$tmp);
    @asset = split(/:/, $input[3]);

    $i = 1;
    $cnt_providers = 0;
    $cnt_customers = 0;
    $cnt_siblings = 0;
    $cnt_peers     = 0;

    $connset=":";

    while ( $asset[$i] != 0   )
    {
        $key = join $asset[$i], ":", ":";

        if ( $asset[$i]!=$input[0] && not ($connset =~ $key ) )
        {
            $connset = join $asset[$i], $connset, ":";

            $src = $input[0];
            $dst = $asset[$i];
            $src2 = join $src , ":",":";
            $dst2 = join $dst , ":",":";

            #check if sibling-to-sibling
            if ( $times{$src, $dst} > 0 && $times{$dst, $src} > 0 )
            {
                if( ($tier1 =~ $src2) && ($tier1 =~ $dst2))
                {
                    $peers = join ":" ,$peers, $asset[$i];
                    $cnt_peers = $cnt_peers + 1;
                }
                else
                {
                    $siblings = join ":" ,$siblings, $asset[$i];
                    $cnt_siblings = $cnt_siblings + 1;
                }
            }
            else
            {
                #check if peer-to-peer
                if( $deg[$dst] != 0
                    && $nprivp{$src, $dst} != 1
                    && $nprivp{$dst, $src} != 1
                    && $deg[$src]/$deg[$dst]<60
                    && $deg[$src]/$deg[$dst]>1/60 )
                {
                    $peers = join ":" ,$peers, $asset[$i];
                    $cnt_peers = $cnt_peers + 1;
                }
                else
                {
                    if (($times{$src, $dst} == 0 ) && not($tier1 =~ $dst2))
                    {
                        $customers = join ":",$customers, $asset[$i];
                        $cnt_customers = $cnt_customers + 1;
                    }
                    else
                    {
                        if(not($tier1 =~ $src2))
                        {
                            $providers = join ":",$providers, $asset[$i];
                            $cnt_providers = $cnt_providers + 1;
                        }
                        else
                        {
                            $peers = join ":" ,$peers, $asset[$i];
                            $cnt_peers = $cnt_peers + 1;
                        }
                    }
                }
            }
        }
        $i = $i + 1;
    }

    print FOUT "AS",$input[0],":","\n";
    print FOUT "Providers :#",$cnt_providers, $providers, ":\n";
    print FOUT "Customers :#",$cnt_customers, $customers, ":\n";
    print FOUT "Siblings  :#",$cnt_siblings,  $siblings, ":\n";
    print FOUT "Peers     :#",$cnt_peers,     $peers,     ":\n";


    $providers = ":";
    $customers = ":";
    $siblings = ":";
    $peers     = ":";

}

close(FILE);
close(FOUT);
