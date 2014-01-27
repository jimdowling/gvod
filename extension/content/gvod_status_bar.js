/**
  GvodStatuBar - functions for the Gvodplayer status bar
**/

// TODO make async requests using ajax

var GvodStatusBar = {
	// Install a timeout handler to install the interval routine

  startup: function()
  {
    this.refreshInformation();
    window.setInterval(this.refreshInformation, 1000);
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
	    var output = httpRequest.responseText;

// jwplayer().getPosition()
		
	    if (output.length)
	    {
		    var resp = JSON.parse(output);

		    if(resp.success) {
		      
		      if (gvodPanel.src != "chrome://gvod/skin/gvod.png") {
    		    
		        gvodPanel.src = "chrome://gvod/skin/gvod.png";
  		        //gvodPanel.onclick = openWebUI;
  		        gvodPanel.onclick = openAndReuseTab;
			gvodPanel.tooltipText="Click here to access the Gvod Web Interface"
		      }
    		  
		      gvodPanel.label = "Down: " + parseInt(resp.downspeed) + " KB/s, Up: " + parseInt(resp.upspeed) + " KB/s";
		    }				
	    }
    }
    
    function openWebUI()
        {
          var win = Components.classes['@mozilla.org/appshell/window-mediator;1'].
	  getService(Components.interfaces.nsIWindowMediator).getMostRecentWindow('navigator:browser'); 
          win.openUILinkIn('http://127.0.0.1:58026/?', 'tab');
        }
        
    function openAndReuseTab() 
        {
          url = "http://127.0.0.1:58026/?";
          var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"]
                             .getService(Components.interfaces.nsIWindowMediator);
          var browserEnumerator = wm.getEnumerator("navigator:browser");

          // Check each browser instance for our URL
          var found = false;
          while (!found && browserEnumerator.hasMoreElements()) {
            var browserWin = browserEnumerator.getNext();
            var tabbrowser = browserWin.gBrowser;

            // Check each tab of this browser instance
            var numTabs = tabbrowser.browsers.length;
            for (var index = 0; index < numTabs; index++) {
              var currentBrowser = tabbrowser.getBrowserAtIndex(index);
              if (url == currentBrowser.currentURI.spec) {

                // The URL is already opened. Select this tab.
                tabbrowser.selectedTab = tabbrowser.tabContainer.childNodes[index];

                // Focus *this* browser-window
                browserWin.focus();

                found = true;
                break;
              }
            }
          }

          // Our URL isn't open. Open it now.
          if (!found) {
            var recentWindow = wm.getMostRecentWindow("navigator:browser");
            if (recentWindow) {
              // Use an existing browser window
              recentWindow.delayedOpenTab(url, null, null, null, null);
            }
            else {
              // No browser windows are open, so open a new one.
              window.open(url);
            }
          }
      }

    
    function restartBG()
    {

      GvodStatusBar.startBG();

    }
    
    function restoreBar()
    {
	    var gvodPanel = document.getElementById('gvodstatusbar');

      if (gvodPanel.src != "chrome://gvod/skin/gvod.png") {    
          gvodPanel.src = "chrome://gvod/skin/gvod.png";
	      gvodPanel.onclick=restartBG;
	      gvodPanel.label = " ";
		  gvodPanel.tooltipText="GVod: Sharing is disabled. Click here to start sharing"
		  GvodStatusBar.gvodChannel = null;
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
  
  startBG: function() {

    if (this.gvodChannel == null) { 
      var gvodChannel = Components.classes['@sics.se/gvod/channel;1'].getService().wrappedJSObject;
      this.gvodChannel = gvodChannel;
    }
    
    if (!gvodChannel.init) {
      gvodChannel.startBackgroundDaemon();
    }
    
  }
  
}

window.addEventListener("load", function(e) { GvodStatusBar.startup(); }, false);
