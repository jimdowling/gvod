// -*- coding: utf-8 -*-
/*
 */

Components.utils.import("resource://gre/modules/XPCOMUtils.jsm");

const Cc = Components.classes;
const Ci = Components.interfaces;

function GvodTransport() {
}

GvodTransport.prototype =
{
  classDescription: "gvodTransport",
  classID: Components.ID("d61ba3d0-db4b-11df-937b-0800200c9a66"),
  contractID: "@sics.se/gvod/gvodTransport;1",
  QueryInterface: XPCOMUtils.generateQI(
    [Ci.gvodIGvodTransport,
     Ci.nsISecurityCheckedComponent,
     Ci.nsISupportsWeakReference,
     Ci.nsIClassInfo]),
   _xpcom_factory : GvodTransportFactory,
   _xpcom_categories : [{
     category: "JavaScript global constructor",
     entry: "gvodTransport"
   }],
  version: 1.0,
} 

var GvodTransportFactory =
{
  createInstance: function (outer, iid)
  {
    if (outer != null)
      throw Components.results.NS_ERROR_NO_AGGREGATION;

    dump('TransportFactory called');

    if (!iid.equals(Ci.nsIProtocolHandler) &&
        !iid.equals(Ci.nsISupports) )
      throw Components.results.NS_ERROR_NO_INTERFACE;

    return (new GvodTransport()).QueryInterface(iid);
  }
};

if (XPCOMUtils.generateNSGetFactory)
    var NSGetFactory = XPCOMUtils.generateNSGetFactory([GvodTransport]);
else
    var NSGetModule = XPCOMUtils.generateModule([GvodTransport]);

