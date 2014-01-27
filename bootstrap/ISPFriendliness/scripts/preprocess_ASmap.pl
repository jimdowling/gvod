#!/usr/bin/perl 

#
# convert_RouteView.pl - convert the route view raw data file (ASmap.*) to 
#                        the format for input to dump_matrix and compute_relation_matrix
#
# Last modified: May 26, 2001
# by gezihui, ratton
#
#
# PreProcess Routing View BGP Table ------ extract all as path information only
#                                      and remove reduntant AS path and routes.
#
#

#
#
# Copyright (C) 2001 - Computer Networks Research Group
#                      University of Massachusetts
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# A copy of the GNU General Public License is located in the 
# COPYRIGHT file included in this distribution.


$filename=$ARGV[0];

$outputfilename=$ARGV[1];

open(INPUT,"$filename") || die("input file not found");
##open(OUTPUT,">$outputfilename") || die("output file open error");
open(OUTPUT,">$outputfilename.tmp") || die("output file open error"); 

$in_data=0;

while($line=<INPUT>)
{
  if ($in_data == 0)
  {
  	if ($line =~ "LocPrf") { $in_data=1; }
  }
  else {
     $line =~ s/(^.* 0 )//;
     next if (!defined($1));
     #
     # remove origin flag at the end of file
     # modified by Jianhong Xia
     #

     $line =~ s/ [ie\?]//;
     $line =~ tr/ / /s;

     @info = split(" ", $line);
     if( $#info>=1 )
     {
         for($i=$#info; $i>=1; $i--)
         {
             if( $info[$i]==$info[$i-1] )
             {
                 $info[$i] = "";
             }
         }
     }
     
     for($i=0; $i<=$#info; $i++)
     {
         $newline = sprintf("%s %s", $newline, $info[$i]);
     }
     $newline =~ tr/ / /s;
     $newline =~ s/^ +//;
     $newline =~ s/ +$//;
     print OUTPUT $newline, "\n";
     $newline = ""; 
 

 #    print OUTPUT $line;
     }
}

close(INPUT);
close(OUTPUT);

system("sort $outputfilename.tmp | uniq > $outputfilename");
system("rm $outputfilename.tmp");
