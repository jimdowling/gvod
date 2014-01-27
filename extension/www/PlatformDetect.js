/*
    Written by Riccardo Petrocco and Arno 
    Bakker based on "Documentation Template" 
    example from css-tricks.com 
*/


function getPlatform()
{

	var browserName="Unknown"
	var browserVersion=-1;
	var osName="Unknown";
	var archName="Unknown";
	if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent))
	{ 
		var ffbrowserVersion=new Number(RegExp.$1)
		browserName = "Firefox";
		browserVersion = ffbrowserVersion;
	}
	else if (/MSIE (\d+\.\d+);/.test(navigator.userAgent))
	{ 
		var iebrowserVersion=new Number(RegExp.$1)
		browserName = "Internet Explorer";
		browserVersion = iebrowserVersion;
	}
	else if (/Chrome[\/\s](\d+\.\d+)/.test(navigator.userAgent)) 
	{
	    // Must come before Safari, Chrome says it's Safari too. 
		var chbrowserVersion=new Number(RegExp.$1)
		browserName = "Chrome";
		browserVersion = chbrowserVersion;
	}
	else if (/Safari[\/\s](\d+\.\d+)/.test(navigator.userAgent))
	{ 
		var sfbrowserVersion=new Number(RegExp.$1)
		browserName = "Safari";
		browserVersion = sfbrowserVersion;
	}
	else if (/Iceweasel[\/\s](\d+\.\d+)/.test(navigator.userAgent))
	{ 
	    // Iceweasel should be compatible with Firefox
		var ffbrowserVersion=new Number(RegExp.$1)
		browserName = "Firefox";
		browserVersion = ffbrowserVersion;
	}
	else if (/Namoroka[\/\s](\d+\.\d+)/.test(navigator.userAgent))
	{ 
	    // Namoroka should be compatible with Firefox
		var ffbrowserVersion=new Number(RegExp.$1)
		browserName = "Firefox";
		browserVersion = ffbrowserVersion;
	}


	if (navigator.userAgent.indexOf("Win")!=-1) osName="Windows";
	else if (navigator.userAgent.indexOf("Mac")!=-1) osName="MacOS";
	else if (navigator.userAgent.indexOf("Ubuntu")!=-1) osName="Ubuntu Linux";
	else if (navigator.userAgent.indexOf("Linux")!=-1) osName="Linux";
	else if (navigator.userAgent.indexOf("X11")!=-1) osName="UNIX";
	
	if (navigator.userAgent.indexOf("Intel Mac")!=-1) archName="Intel";
	else if (navigator.userAgent.indexOf("PPC Mac")!=-1) archName="PowerPC";

	return {browserName: browserName, browserVersion: browserVersion, osName: osName, archName: archName} 
}



