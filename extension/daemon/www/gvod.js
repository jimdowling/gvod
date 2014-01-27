
// http://www.quirksmode.org/js/detect.html

var BrowserDetect = {
	init: function () {
		this.browser = this.searchString(this.dataBrowser) || "An unknown browser";
		this.version = this.searchVersion(navigator.userAgent)
			|| this.searchVersion(navigator.appVersion)
			|| "an unknown version";
		this.OS = this.searchString(this.dataOS) || "an unknown OS";
	},
	searchString: function (data) {
		for (var i=0;i<data.length;i++)	{
			var dataString = data[i].string;
			var dataProp = data[i].prop;
			this.versionSearchString = data[i].versionSearch || data[i].identity;
			if (dataString) {
				if (dataString.indexOf(data[i].subString) != -1)
					return data[i].identity;
			}
			else if (dataProp)
				return data[i].identity;
		}
	},
	searchVersion: function (dataString) {
		var index = dataString.indexOf(this.versionSearchString);
		if (index == -1) return;
		return parseFloat(dataString.substring(index+this.versionSearchString.length+1));
	},
	dataBrowser: [
		{
			string: navigator.userAgent,
			subString: "Chrome",
			identity: "Chrome"
		},
		{ 	string: navigator.userAgent,
			subString: "OmniWeb",
			versionSearch: "OmniWeb/",
			identity: "OmniWeb"
		},
		{
			string: navigator.vendor,
			subString: "Apple",
			identity: "Safari",
			versionSearch: "Version"
		},
		{
			prop: window.opera,
			identity: "Opera"
		},
		{
			string: navigator.vendor,
			subString: "iCab",
			identity: "iCab"
		},
		{
			string: navigator.vendor,
			subString: "KDE",
			identity: "Konqueror"
		},
		{
			string: navigator.userAgent,
			subString: "Firefox",
			identity: "Firefox"
		},
		{
			string: navigator.vendor,
			subString: "Camino",
			identity: "Camino"
		},
		{		// for newer Netscapes (6+)
			string: navigator.userAgent,
			subString: "Netscape",
			identity: "Netscape"
		},
		{
			string: navigator.userAgent,
			subString: "MSIE",
			identity: "Explorer",
			versionSearch: "MSIE"
		},
		{
			string: navigator.userAgent,
			subString: "Gecko",
			identity: "Mozilla",
			versionSearch: "rv"
		},
		{ 		// for older Netscapes (4-)
			string: navigator.userAgent,
			subString: "Mozilla",
			identity: "Netscape",
			versionSearch: "Mozilla"
		}
	],
	dataOS : [
		{
			string: navigator.platform,
			subString: "Win",
			identity: "Windows"
		},
		{
			string: navigator.platform,
			subString: "Mac",
			identity: "Mac"
		},
		{
			   string: navigator.userAgent,
			   subString: "iPhone",
			   identity: "iPhone/iPod"
	    },
		{
			string: navigator.platform,
			subString: "Linux",
			identity: "Linux"
		}
	]

};
BrowserDetect.init();


var GvodStatusBar = {

  startup: function(overlayId)
  {
    this.refreshInformation();
    window.setInterval(function() { this.refreshInformation(overlayId); }, 1000);
    this.gvodChannel = null;
  },


  // Called periodically to refresh traffic information
  refreshInformation: function()
  {

    var httpRequest = null;
    var fullUrl = "http://127.0.0.1:58026/?&method=get_speed_info";
    var gvodBar = this;
    
    // TODO: call jwplayer().getPosition() and send this back to the control port

    function infoReceived()
    {

	    var gvodPanel = document.getElementById('gvodstatusbar');
	    var inetPanel = document.getElementById('inetstatusbar');
	    var chromePanel = document.getElementById('chromestatusbar');
	    var output = httpRequest.responseText;

// jwplayer().getPosition()
		
	    if (output.length)
	    {
		    var resp = JSON.parse(output);

		    if(resp.success) {
			// firefox 
			if (gvodPanel) {
			    if (gvodPanel.src != "images/icon.png") {
				gvodPanel.src = "images/icon.png";
				gvodPanel.tooltipText="Ecvideo"
			    }
    			    gvodPanel.label = "Down: " + parseInt(resp.downspeed) + " KB/s, Up: " + parseInt(resp.upspeed) + " KB/s";
			} else if (inetPanel) {

			}
		    }				
	    }
    }
    

    //TODO remove
    function reqTimeout()
    {
        httpRequest.abort();
        return;
        // Note that at this point you could try to send a notification to the
        // server that things failed, using the same xhr object.
    }

    // CALLED ON INITIALIZING the STATUS BAR
//    try 
//    {
//        httpRequest = new XMLHttpRequest();
//        httpRequest.open("GET", fullUrl, true);
//        httpRequest.onload = infoReceived;
//        httpRequest.onerror = restoreBar;
//        httpRequest.send(null);
//        // Timeout to abort in 5 seconds
//        //var reqTimeout = setTimeout(reqTimeout(),1000);
//        setTimeout(function()
//            {
//                httpRequest.abort();
//                return;
//            }
//            ,1000);
//    }
//    catch( err )
//    {
//        aMsg = ("*** StatusBar : " + err.description);
//        Cc["@mozilla.org/consoleservice;1"].getService(Ci.nsIConsoleService).logStringMessage(aMsg);
//        dump(aMsg);
//    }
  },
  

  startFirefox: function() {

    if (this.gvodChannel == null) { 
      var gvodChannel = Components.classes['@sics.se/gvod/channel;1'].getService().wrappedJSObject;
      this.gvodChannel = gvodChannel;
    }
    
    if (!gvodChannel.init) {
      gvodChannel.startBackgroundDaemon();
    }
    
  }
  
}


//window.addEventListener("load", function(e) { GvodStatusBar.startup(); }, false);
