#!/usr/bin/perl
my @lines;
my $line;

@lines=<STDIN>;
foreach $line (@lines)
{
	$lastSpaceIndex=rindex($line,' ');
	$fileName= substr($line,1+$lastSpaceIndex);
	print $fileName;
}
