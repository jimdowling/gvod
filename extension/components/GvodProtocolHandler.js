// -*- coding: utf-8 -*-
// vi:si:et:sw=2:sts=2:ts=2
/*
  See guide:
  http://www.nexgenmedia.net/docs/protocol/
  https://developer.mozilla.org/en-US/docs/Web-based_protocol_handlers
  Mozilla also needs to be told to reregister the components (components that are registered are listed in the compreg.dat 
  file in the components/ directory), which is achieved by placing an empty file called .autoreg into Mozilla's install directory, 
  at the same level where the Mozilla executable lives.
*/

Components.utils.import("resource://gre/modules/XPCOMUtils.jsm");

const Cc = Components.classes;
const Ci = Components.interfaces;
const Cr = Components.results;


const kSCHEME = "gvod";
const kPROTOCOL_NAME = "Gvod Protocol";
const kPROTOCOL_CONTRACTID = "@mozilla.org/network/protocol;1?name=" + kSCHEME;
const kPROTOCOL_CID = Components.ID("{1df43930-d7a3-11df-937b-0800200c9a66}");

// Mozilla defined
const kSIMPLEURI_CONTRACTID = "@mozilla.org/network/simple-uri;1";
const kIOSERVICE_CONTRACTID = "@mozilla.org/network/io-service;1";
const nsISupports = Components.interfaces.nsISupports;
const nsIIOService = Components.interfaces.nsIIOService;
const nsIProtocolHandler = Components.interfaces.nsIProtocolHandler;
const nsIURI = Components.interfaces.nsIURI;



var gvodLoggingEnabled = true;

function LOG(aMsg) {
    if (gvodLoggingEnabled)
    {
	aMsg = ("*** GvodProtocolHandler.js : " + aMsg);
	Cc["@mozilla.org/consoleservice;1"].getService(Ci.nsIConsoleService).logStringMessage(aMsg);
	dump(aMsg);
    }
}

function GvodProtocol() {
    this.prefService = Cc["@mozilla.org/preferences-service;1"].getService(Ci.nsIPrefBranch).QueryInterface(Ci.nsIPrefService);
    try {
	gvodLoggingEnabled = this.prefService.getBoolPref("gvod.logging.enabled");
        LOG('Logging enabled is:' + gvodLoggingEnabled);
    } catch (e) {
        LOG('Problem getting preference (Logging enabled):' + e);
    }
    this.wrappedJSObject = this;  
}

GvodProtocol.prototype =
    {
	classDescription: "'gvod://' Gvod protocol",
	classID: Components.ID("{1df43930-d7a3-11df-937b-0800200c9a66}"),
	contractID: kPROTOCOL_CONTRACTID,
	QueryInterface: XPCOMUtils.generateQI([Ci.nsIProtocolHandler, Ci.nsISupports]),
	_xpcom_factory : GvodProtocolFactory,
	scheme: kSCHEME,
	defaultPort: -1,
	protocolFlags: Ci.nsIProtocolHandler.URI_NORELATIVE |
            Ci.nsIProtocolHandler.URI_NOAUTH |
            Ci.nsIProtocolHandler.URI_LOADABLE_BY_ANYONE,

	allowPort: function(port, scheme)
	{
	    return false;
	},

	newURI: function(spec, charset, baseURI)
	{
	    LOG('NewUri: ' + baseURI);
	    var uri = Cc["@mozilla.org/network/simple-uri;1"].createInstance(Ci.nsIURI);
	    uri.spec = spec;
	    return uri;
	},

	newChannel: function(input_uri)
	{
	    // aURI is a nsIUri, so get a string from it using .spec
	    var key = input_uri.spec;
	    LOG('Creating gvodChannel: ' + key);
	    if (key == null || key == "") {
		LOG('Exiting. Key was null or empty when creating gvodChannel: ' + key);
		return null;
	    }
	    LOG('Created gvodChannel: ' + key);	    

	    // http://lucan.sics.se/gvod/topgear.mp4.data
	    // gvod://http://lucan.sics.se/gvod/topgear.mp4.data
	    // TODO - check that the URL is well-formed. Send error msg if not.
	    var prefix = "gvod://";
	    var torrent_url = key.substring(prefix.length, key.length);
	    // the URL will not be encoded since we will not be able to decode it afterwards
	    var channel = Cc["@sics.se/gvod/channel;1"].createInstance(Ci.gvodIChannel);
	    channel.setTorrentUrl(torrent_url);
	    return channel;
	},

    } 

var GvodProtocolFactory =
    {
	createInstance: function (outer, iid)
	{
	    LOG("Creating GVOD instance");
            if (outer != null)
		throw Components.results.NS_ERROR_NO_AGGREGATION;

	    if (!iid.equals(Ci.nsIProtocolHandler) &&
		!iid.equals(Ci.nsISupports) )
		throw Components.results.NS_ERROR_NO_INTERFACE;

	    return (new GvodProtocol()).QueryInterface(iid);
	}
    };


if (XPCOMUtils.generateNSGetFactory){
    LOG('Firefox 4'); 
    var NSGetFactory = XPCOMUtils.generateNSGetFactory([GvodProtocol]);
}
else {
      LOG('Firefox 3'); 
      var NSGetModule = XPCOMUtils.generateModule([GvodProtocol]); 
}
