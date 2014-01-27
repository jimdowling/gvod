/*
https://developer.mozilla.org/en-US/docs/Building_an_Extension
 */

Components.utils.import("resource://gre/modules/XPCOMUtils.jsm");


const Cc = Components.classes;
const Ci = Components.interfaces;
const Cr = Components.results;

var gvodLoggingEnabled = true;
var hostIp = '127.0.0.1';
var controlPort = 58024;
var mediaPort = 58026;
var torrentDir = "";
var videoDir = "";
var jwplayer = 0;
var callbackTimeout = 1000;
var torrent_uri = "";
var video_uri="";
var alertsService = Components.classes["@mozilla.org/alerts-service;1"].
                      getService(Components.interfaces.nsIAlertsService);
function LOG(displayAlert, aMsg) {
    if (gvodLoggingEnabled)
    {
	aMsg = ("*** GvodChannel.js : " + aMsg);
	Cc["@mozilla.org/consoleservice;1"].getService(Ci.nsIConsoleService).logStringMessage(aMsg);
	dump(aMsg);
	if (displayAlert == true) {
	    alertsService.showAlertNotification("chrome://mozapps/skin/downloads/downloadIcon.png", 
                                    "Gvod problem", aMsg, false, "", null, "");
	}
    }
}


var myPrefObserver =
{
  register: function()
  {
    LOG(false,"Getting preferences");
      try {
	  this.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Ci.nsIPrefService);
	  this._branch = this.prefs.getBranch("extensions.gvod.");
	  this._branch.addObserver("", this, false);
	  this.prefs = this._branch.QueryInterface(Ci.nsIPrefBranch);
	  this.prefService = Cc["@mozilla.org/preferences-service;1"].getService(Ci.nsIPrefBranch).QueryInterface(Ci.nsIPrefService);
	  gvodLoggingEnabled = this.prefService.getBoolPref("gvod.logging.enabled");
	  hostIp = this.prefService.getCharPref("gvod.ip");
	  controlPort = this.prefService.getIntPref("gvod.control.port");
	  mediaPort = this.prefService.getIntPref("gvod.media.port");
	  torrentDir = this.prefService.getCharPref("gvod.torrent.dir");
	  videoDir = this.prefService.getCharPref("gvod.video.dir");
	  jwplayer  = this.prefService.getBoolPref("gvod.jwplayer");
	  callbackTimeout = this.prefService.getIntPref("gvod.time.wait.java");
	  LOG(false,'CONTROL PORT WAS ' + controlPort);
	  LOG(false,'MEDIA PORT WAS ' + mediaPort);
      } catch (e) {
	  LOG(false,"Error getting loggin prefs: " + e + "\n");     
	  gvodLoggingEnabled = true;
      }    

  },

  unregister: function()
  {
    if(!this._branch) return;
    this._branch.removeObserver("", this);
  },

  observe: function(aSubject, aTopic, aData)
  {
    if(aTopic != "nsPref:changed") return;
    // aSubject is the nsIPrefBranch we're observing (after appropriate QI)
    // aData is the name of the pref that's been changed (relative to aSubject)

    // extensions.gvod.pref1 was changed
    switch (aData) {
      case "gvod.torrent.dir":
	// TODO - send msg via control port to gvod 
	// prefs.setCharPref("gvod.torrent.dir", !value);
        break;
      case "gvod.video.dir":
	// TODO - send msg via control port to gvod 	
        break;
      case "gvod.jwplayer":
	// TODO - send msg via control port to gvod 	
        break;
      case "gvod.time.wait.java":
	// TODO - send msg via control port to gvod 	
        break;
      case "gvod.control.port":
	// TODO - send msg via control port to gvod 	
        break;
      case "gvod.media.port":
	// TODO - send msg via control port to gvod 	
        break;
    }
  },
}


// Constructor
function GvodChannel() {
    this.wrappedJSObject = this;
    myPrefObserver.register();
}

GvodChannel.prototype =
    {
	classDescription: "Gvod channel",
	classID: Components.ID("c789a100-db4b-11df-937b-0800200c9a66"),
	contractID: "@sics.se/gvod/channel;1",
	QueryInterface: XPCOMUtils.generateQI([Ci.gvodIChannel,
					       Ci.nsIChannel,
					       Ci.nsISupports]),
	_xpcom_factory : GvodChannelFactory,
	init: false,
	running: 0,
	torrent_url: '',

	setTorrentUrl: function(url) {
	    LOG(false,"Setting TORRENT URL TO: " + url);
	    if (url != null) {
		this.torrent_url = url;
	    } 
	},

	shutdown: function() {
	    LOG(false,"shutdown called\n"); 
	    var msg = 'SHUTDOWN\r\n';
	    this.outputStream.write(msg, msg.length);

	    //this.outputStream.close();
	    //this.inputStream.close();
	    this.transport.close(Components.results.NS_OK);
	},

	asyncPlay: function(aListener, aContext) 
	{

	},
	asyncOpen: function(aListener, aContext)
	{
	    LOG(false,'asyncOpen called.\n');
	    var _this = this;
	    // if(this.init) {
	    // 	LOG(false,'asyncOpen called again\n');
	    // 	throw Components.results.NS_ERROR_ALREADY_OPENED;
	    // }
	    this.running++;
	    //	    this.init = true;
	    var socketTransportService = Cc["@mozilla.org/network/socket-transport-service;1"].getService(Ci.nsISocketTransportService);
	    
	    // TODO - catch exception if Firefox is 'work offline'. Send msg firefox is offline.
            var portFound = false;
	    var defaultPort = controlPort;

	    // TODO - move the creation of the socket to the GVodChannel constructor
	    try {
		this.transport = socketTransportService.createTransport(null, 0, hostIp, controlPort, null);
	    } catch (e) {
		LOG(true,'Couldnt bind to controlPort' + controlPort +  ' . Try next controlPort');
		return;
	    }
	    if (controlPort >= defaultPort+10) {
	    	LOG(true,'Firefox is probably in offline mode.');
		alert('Error: cannot connect to Gvod. Firefox is probably in offline mode. Change Firefox to online mode.');
	    }

            // Alright to open streams here as they are non-blocking by default
            this.outputStream = this.transport.openOutputStream(0,0,0);
            this.inputStream = this.transport.openInputStream(0,0,0);
	    
	    LOG(false,'opened input and output streams');
    	    /* Let player inform BG process about capabilities
	    */

	    // TODO - if the socket hasn't been created yet, first sleep, then retry creating the socket
	    // then retry sending this msg.
            var msg = 'START ' + this.torrent_url + '\r\n'; // concat, strange async interface
            this.outputStream.write(msg, msg.length);

	    LOG(false,'Written start to output stream ');    

            var dataListener = {
		onStartRequest: function(request, context) {},
		onStopRequest: function(request, context, status) {

		    LOG(false,'Data listener starting/stopping... ');    
		    if(status == Components.results.NS_ERROR_CONNECTION_REFUSED && _this.running < 60) {
			LOG(false,"Connection refused: onStopRequest. Running status=" + _this.running );
			_this.startBackgroundDaemon();
			_this.init=false;
			var timer = Cc["@mozilla.org/timer;1"].createInstance(Ci.nsITimer);
			timer.initWithCallback(function() { _this.asyncOpen(aListener, aContext) },
					       callbackTimeout, Ci.nsITimer.TYPE_ONE_SHOT);
		    }
		    else 
		    {
			LOG(false,'GVod Process closed Control connection\n');
			this.onBGError();
		    }
		},
		// JWPlayer plays the data
		onDataAvailable: function(request, context, inputStream, offset, count) {
		    
		    LOG(false,'Data available');
		    var sInputStream = Cc["@mozilla.org/scriptableinputstream;1"].createInstance(Ci.nsIScriptableInputStream);
		    sInputStream.init(inputStream);

		    // jwplayer().getPosition()

		    var s = sInputStream.read(count);
		    if (s != null) {
			s = s.split('\r\n');
			LOG(false,s);
			for(var i=0;i<s.length;i++) {
			    var cmd = s[i];
			    if (cmd.substr(0,4) == 'PLAY') {
				var video_url = cmd.substr(5);
				video_uri=video_url;
				this.onPlay(video_url);
				break;
			    }
			    if (cmd.substr(0,13) == 'READY_TO_PLAY') {
				LOG(true,'Getting reading to play in 20 seconds....\n');				
				break;
			    }
			    if (cmd.substr(0,2) == 'BW') {
				var bandwidth = cmd.substr(3,8);
				this.displayBw(bandwidth);
				break;
			    }
			    else if (cmd.substr(0,5) == "ERROR") {
				LOG(false,'ERROR in BackgroundProcess\n');
				this.onBGError();
				break;
			    }
			}
		    }
		},
		
		onBGError: function() {
                    //  It's hard to figure out how to throw an exception here
                    // that causes FX to fail over to alternative <source> elements
                    // inside the <video> element. The hack that appears to work is
                    // to create a Channel to some URL that doesn't exist.
                    //
		    LOG(false,'Socket connection broken from browser to client');
                    var fake_video_url = 'http://127.0.0.1:58024/createxpierror.html';
		    var ios = Cc["@mozilla.org/network/io-service;1"].getService(Ci.nsIIOService);
		    var video_channel = ios.newChannel(fake_video_url, null, null);
		    //                video_channel.asyncOpen(aListener, aContext);
		},

		onPlay: function(video_url) {
		    LOG(false,'PLAY !!!!!! '+video_url+'\n');
		    //              var ios = Cc["@mozilla.org/network/io-service;1"].getService(Ci.nsIIOService);
		    //              var video_channel = ios.newChannel(video_url, null, null);
		    //              video_channel.asyncOpen(aListener, aContext);



		    //		var videoName = video_url.substring(video_url.lastIndexOf("/") + 1, video_url.length);
		    //     cleanup if window is closed
		    var windowMediator = Cc["@mozilla.org/appshell/window-mediator;1"].getService(Ci.nsIWindowMediator);
		    var nsWindow = windowMediator.getMostRecentWindow("navigator:browser");
		    nsWindow.content.addEventListener("unload", function() { _this.shutdown() }, false);

		    var ios = Components.classes["@mozilla.org/network/io-service;1"].getService(Ci.nsIIOService);  
		    var video_channel = ios.newChannel(video_url, null, null);
                    video_channel.asyncOpen(aListener, aContext);
		},
		displayBw: function(bandwidth) {

		},

            };
            var pump = Cc["@mozilla.org/network/input-stream-pump;1"].createInstance(Ci.nsIInputStreamPump);
            pump.init(this.inputStream, -1, -1, 0, 0, false);
            pump.asyncRead(dataListener, null);
	    /* create dummy nsIURI and nsIChannel instances */
	},
	startBackgroundDaemon: function() {

	    // I don't think this check works!
	    if (this.running > 1) {
		LOG(false,'Not re-running startBackgroundDaemon. Running=' + this.running);		
	    }

	    var osString = Cc["@mozilla.org/xre/app-info;1"].getService(Ci.nsIXULRuntime).OS;  
	    var gvodScriptPath = "";
	    if (osString == "WINNT")
		gvodScriptPath = 'gvodplayer.exe';
	    else if (osString == "Darwin")
		gvodScriptPath = "GvodPlayer.app/Contents/MacOS/GvodPlayer";
	    else
		gvodScriptPath = 'gvodplayer';


            LOG(false,'EXE script is ' + gvodScriptPath);

	    var _this = this;
	    function runBackgroundDaemon(file, extension_dir) {


		LOG(false,'running background daemon: ' + file.getRelativeDescriptor(extension_dir) + ' torrent_url ' + _this.torrent_url);
		try {
		    file.permissions = 0755;
		} catch (e) {}
		var process = Cc["@mozilla.org/process/util;1"].createInstance(Ci.nsIProcess);
		process.init(file);
		var args = [];

		args.push(_this.torrent_url);
		
		// TODO: Get torrentDir from the plugin's preferences
		if (torrentDir == "") {
		    torrentDir = extension_dir;
		}
		// TODO: Get videoDir from the plugin's preferences
		if (videoDir == "") {
		    videoDir = extension_dir;
		}
		if (torrentDir.exists()) {
                    args.push("-dir");
		    args.push(torrentDir.path);
		} else {
		    LOG(true,'Error: Path not found for torrent directory:' + torrentDir.path + " . Please change the torrent directory to a valid directory in gvod addon preferences.");
		}
		if (videoDir.exists()) {
                    args.push("-videoDir");
		    args.push(videoDir.path);
		} else {
		    LOG(true,'Error: Path not found for video directory:' + torrentDir.path + " . Please change the video directory to a valid directory in gvod addon preferences.");
		}

		LOG(false,'Adding player to args:' + jwplayer);
                args.push("-player");
		if (jwplayer == true) {
		    args.push("0");
		} else {
		    args.push("1"); // flowplayer
		}

		// Get hostname for bootstrap server from the torrent url:
		// http://cloud7.sics.se/topgear.data => cloud7.sics.se
		args.push("-bIp");
		var torr = new String(_this.torrent_url);
		var bIp = torr.split(/\/(.)?/)[2]
		args.push(bIp);


		// TODO: Get from the plugin's preferences
                args.push("-cPort");
                args.push(controlPort.toString());
		// TODO: Get from the plugin's preferences
                args.push("-mPort");
                args.push(mediaPort.toString());

		// Run the process.
		// If first param is true, calling thread will be blocked until called process terminates.
		// Second and third params are used to pass command-line arguments to the process.
		// https://developer.mozilla.org/en/Code_snippets/Running_applications
		LOG(false,'ARGS FOR gvodplayer:' + args);
		var exitValue = process.run(true, args, args.length);
		if (exitValue == 1) {
		    LOG(true,'Incorrect args for java script: ' + args);
		} else if (exitValue == 3) {
		    LOG(true,'Please install java. You have no java, and you need it for this video player.');
		}

	    }
	    try {
		LOG(false,'Trying to launch java program.');
		if (XPCOMUtils.generateNSGetFactory) { // firefox 4
		    LOG(false,'Firefox 4+ loading java');
		    Components.utils.import("resource://gre/modules/AddonManager.jsm");
		    AddonManager.getAddonByID("gvod@sics.se", function(addon) {
			if (addon.hasResource('daemon')) {
			    LOG(false,'Found resource called daemon');
			    var resource = addon.getResourceURI('daemon');
			    var file = resource.QueryInterface(Ci.nsIFileURL).file.QueryInterface(Ci.nsILocalFile);
			    var extension_dir = resource.QueryInterface(Ci.nsIFileURL).file.QueryInterface(Ci.nsILocalFile);
			    file.appendRelativePath(gvodScriptPath);
			    LOG(false,"Executing java file with: " + file.getRelativeDescriptor(extension_dir));
			    runBackgroundDaemon(file, extension_dir);
                            // Windows NT only
		            //file.launch();
			} else {
			    LOG(false,'Problem with gvod addon: No resource called daemon');
			}
		    });

		} else { //firefox 3.6
		    LOG(false,'Firefox 3 loading java');
		    var em = Cc["@mozilla.org/extensions/manager;1"].getService(Ci.nsIExtensionManager);
		    var extension_dir = em.getInstallLocation('gvod@sics.se')
			.getItemFile('gvod@sics.se', 'daemon/');
		    var file = em.getInstallLocation('gvod@sics.se')
			.getItemFile('gvod@sics.se', 'daemon/'+gvodScriptPath);
		    runBackgroundDaemon(file, extension_dir);

		}
	    } catch(e) {
		LOG(false,'Exception while trying to launch java program: ' + e.toString());
		if (e.stack) {
		    LOG(false,e.stack);
		}

	    }
	},

	cancel: function(aStatus){
	    LOG(false,"GvodChannel:cancel");
	    this.status = aStatus;
	    this.done   = true;
	},

	suspend: function(aStatus){
	    LOG(false,"GvodChannel:suspend");
	    this.status = aStatus;
	},

	resume: function(aStatus){
	    LOG(false,"GvodChannel:resume");
	    this.status = aStatus;
	},
	// Channel interfaces
	// We don't implement the open function as it seems to be deprecated.
	open: function() {
	    LOG(false,"GvodChannel:open");
	    throw Cr.NS_ERROR_NOT_IMPLEMENTED;
	},
    } 

var GvodChannelFactory =
    {
	createInstance: function (outer, iid)
	{			
	    if (outer != null)
		throw Components.results.NS_ERROR_NO_AGGREGATION;

	    if (!iid.equals(Ci.gvodIChannel) &&
		!iid.equals(Ci.nsIChannel) &&
		!iid.equals(Ci.nsISupports) )
		throw Components.results.NS_ERROR_NO_INTERFACE;

	    var tc =  new GvodChannel();
	    var tcid = tc.QueryInterface(iid); // 
	    return tcid;
	}
    };

 if (XPCOMUtils.generateNSGetFactory)
     var NSGetFactory = XPCOMUtils.generateNSGetFactory([GvodChannel]);
 else
     var NSGetModule = XPCOMUtils.generateModule([GvodChannel]);
