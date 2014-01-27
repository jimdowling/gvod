#!/usr/bin/perl

# bco.pl ciscobgpfile

if( $#ARGV!=3 )
{
    print "\nUsage:  need parameters";
    print "\n    perl bco-for-preprocess.pl aspathfile summary connlist connpairs";
    print "\n";
    exit;
}

$infname=$ARGV[0];
$file_summary = $ARGV[1];
$file_connlist = $ARGV[2];
$file_connpairs = $ARGV[3];

open(infile,"$infname") || die("Cannot open input file $infname");

###
# remove by J. Xia
#
#    while(<infile>) {
#     ($t,$t1,$t,$t2,$t,$t3)=unpack("a3a7a10a8a12a25",$_);
#     if(($t1 eq "Network")&&($t2 eq "Next Hop")&&($t3 =~ "Metric")){last;}
#   }
#
###

while(<infile>)
{

    ###
    #
    #  $t3="";
    #  ($t1,$t2,$t,$t3)=unpack("a1a1a1a6",$_);
    #  if($t3 =~ '\.'){
    #   ($flags,$net,$hop,$weight,@r)=split(' ',$_);
    # # printf"$net\n";
    #   $pfxcnt++;
    #  } else {
    #   ($flags,$hop,$weight,@r)=split(' ',$_);
    #  }
    #
    ###

    @r = split(' ', $_);

    while($v=pop(@r))
    {
        if($v > 0)
        {
            $asa{$v}++;
            if($lastas != 0)
            {
                ##
                ## have some problems if both A<->B and B<->A in routing table
                ## it might overcount the degrees of A or B
                ## reported by Zhenhai Duan, duan@cs.fsu.edu @ Apr. 15, 2005
                ##
                ##   #printf"$lastas:$v\n";
                ##   if((!$asparray{$lastas,$v})&&(!$asparray{$v,$lastas}))
                ##   {
                ##       $aspcnt++;
                ##       $asparray{$lastas,$v}++;
                ##       $ascarray{$lastas}=join(':',$ascarray{$lastas},$v);
                ##       $asccnt{$lastas}++;
                ##   }
                ##   if(!$aspiarray{$v,$lastas})
                ##   {
                ##       $aspiarray{$v,$lastas}++;
                ##       $ascarray{$v}=join(':',$ascarray{$v},$lastas);
                ##       $asccnt{$v}++;
                ##   }
                ##

                # path array (parray) is directed
                # connection array (carray) is undirected
                # modified by Zhenhai Duan. duan@cs.fsu.edu
                #
                if (!$asparray{$lastas,$v})
                {
                    $aspcnt++;
                    $asparray{$lastas,$v}++;
                }
                if (!$asct{$lastas,$v})
                {
                    $asct{$lastas,$v}++;
                    $ascarray{$lastas} = join(':', $ascarray{$lastas},$v);
                    $asccnt{$lastas}++;
                }
                if (!$asct{$v,$lastas})
                {
                    $asct{$v,$lastas}++;
                    $ascarray{$v} = join(':', $ascarray{$v},$lastas);
                    $asccnt{$v}++;
                }
                #
            }

            $lastas=$v;
        }
    }
    $lastas=0;
}

foreach $key ( keys %asa)
{
    $ascnt++;
}

($t,$id,$is)=split('\.',$infname);
$fname=join('.',$id,$is);

#open(O,">ASconnsummary.$fname") || die("Cannot open output file ASconnlist");
open(O,">$file_summary") || die("Cannot open output file ASconnlist");
printf O "%d net/mask prefixes, %d AS numbers, %d AS interconnections\n",
$pfxcnt,$ascnt,$aspcnt;
printf O "\nSorted by interconnections:\nAS\tCount\n======= ========\n";
foreach $key ( sort cnumerically ( keys %asccnt))
{
    printf O "%d\t%d\n",$key,$asccnt{$key};
}
close(O);

#open(O,">ASconnlist.$fname") || die("Cannot open output file ASconnlist");
open(O,">$file_connlist") || die("Cannot open output file ASconnlist");
foreach $key ( sort numerically ( keys %ascarray))
{
    printf O "%d\t->\t%d\t%s\n",$key,$asccnt{$key},$ascarray{$key};
}
close(O);

#open(O,">ASconnpairs.$fname") || die("Cannot open output file ASconnlist");
open(O,">$file_connpairs") || die("Cannot open output file ASconnlist");
foreach $key ( sort numerically ( keys %asparray))
{
    ($src, $dst) = split( $;, $key ) ;
    printf O "$src <-> $dst \n",$src,$dst;
}
close(O);

sub numerically  {         $a  <=>         $b ;}
sub cnumerically { $asccnt{$b} <=> $asccnt{$a};}
