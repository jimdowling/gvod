#!/usr/bin/perl

#
#
# perl infer_main.pl  [-c | -f]  [-o | -p]
#   -c  command line
#   -f  arguments from file
#   -o  original routing table
#   -p  routing tables after preprocess
#
#  (1)  command line
#         perl infer_main.pl -c -o tempdir summary connlists connpairs output ASmap1 ASmap2
#  (2)  from file
#         perl infer_main.pl -f -o file
#
#

#$execdir = "~/bgpdata/asrelationship/exec/inferas_w_aspath";
$execdir = ".";

if( $#ARGV<2 ) {
    print "\n Command:  perl infer_main.pl [-c | -f] [-p | -o] .... \n";
    exit;
}

$flag1 = $ARGV[0];

$dir_temp = "";
$file_summary = "";
$file_connlists = "";
$file_connpairs = "";
$file_infer = "";
$file_ASmapList = "";
$file_ASmapNum = 0;


#### read file names
if( $flag1 eq "-c" || $flag1 eq "-C" ) {
    if( $#ARGV>=7 ) {
        $dir_temp = $ARGV[2];
        $file_summary = $ARGV[3];
        $file_connlists = $ARGV[4];
        $file_connpairs = $ARGV[5];
        $file_infer = $ARGV[6];
        $file_ASmapNum = $#ARGV - 6;
        for($i=1; $i<=$file_ASmapNum; $i++) {
            $file_ASmapList = join(" : ", $file_ASmapList, $ARGV[$i+6]);
        }
    } else {
        print "\n command line is error\n";
        exit;
    }
} elsif ( $flag1 eq "-f" || $flag1 eq "-F" ) {
    @files = `cat $ARGV[2]`;
    chomp($files[0]);  $dir_temp = $files[0];
    chomp($files[1]);  $file_summary = $files[1];
    chomp($files[2]);  $file_connlists = $files[2];
    chomp($files[3]);  $file_connpairs = $files[3];
    chomp($files[4]);  $file_infer = $files[4];
    $file_ASmapNum = $#files - 4;
    for($i=1; $i<=$file_ASmapNum; $i++) {
        chomp($files[$i+4]);
        $file_ASmapList = join(" : ", $file_ASmapList, $files[$i+4]);
    }
} else{
    print "\n command line is error\n";
    exit;
}

if( $ARGV[1] eq "-o" || $ARGV[1] eq "-O" ) {
    $ASmapType = 0;
} elsif( $ARGV[1] eq "-p" || $ARGV[1] eq "-P" ) {
    $ASmapType = 1;
} else {
    print "\n command line is error\n";
    exit;
}

#print "\n filelist = $file_ASmapList";
#print "\n\n";
#exit;

####  construct one big AS

$routingtable = "$file_infer.aspath";
system(" echo '' > $routingtable ");
@files = split(/:/, $file_ASmapList);
for($i=0; $i<=$#files; $i++)
{
    $file = $files[$i]; chomp($file);
    $file =~ tr/ / /s; $file =~ s/^ //;  $file =~ s/ $//;
    # print "\n DEBUG ===$file===";
    if( $file ne "" && $file ne " ") {
        if( $ASmapType == 0 )  {

            $mycmd = "bzip2 -d $file";
            print "\n DEBUG $mycmd ";
            system($mycmd);

            $file =~ s/.bz2$//;
            #print "\n DEBUG after gzip $file ";
            @r = split(/\//, $file);
            $ofile = "$dir_temp/$r[$#r].aspath";

            $mycmd = "perl $execdir/preprocess_ASmap.pl $file $ofile";
            #print "\n DEBUG $mycmd ";
            system($mycmd);

            $file = $ofile;
        }

        $cmdline = "cat $file >> $routingtable ";
        #print "\n **** $cmdline";
        system($cmdline);
        $cmdline = "sort $routingtable | uniq > $routingtable.new ";
        #print "\n **** $cmdline";
        system($cmdline);
        $cmdline = "rm $routingtable; mv $routingtable.new $routingtable ";
        #print "\n **** $cmdline";
        system($cmdline);
    }
}

$cmdline = "perl $execdir/bco_for_preprocess.pl";
$cmdargs = " $routingtable $file_summary $file_connlists $file_connpairs ";
#print "\n $cmdline $cmdargs";
system("$cmdline $cmdargs");


$cmdline = "perl $execdir/inferas_for_preprocess.pl";
$cmdargs = " $file_connlists $file_connpairs $routingtable $file_infer ";
#print "\n $cmdline $cmdargs";
system("$cmdline $cmdargs");
system(" rm $routingtable ");