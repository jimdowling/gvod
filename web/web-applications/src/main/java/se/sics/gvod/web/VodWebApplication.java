package se.sics.gvod.web;

import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.gvod.address.Address;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.WebRequest;
import se.sics.kompics.web.WebResponse;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.RandomSetNeighborsRequest;
import se.sics.gvod.common.RandomSetNeighborsResponse;
import se.sics.gvod.web.port.Status;

public class VodWebApplication extends ComponentDefinition {

    Negative<Web> web = negative(Web.class);
    Positive<Status> status = positive(Status.class);
    private Logger logger;
    private VodAddress self;
    private int parts;
    private WebRequest requestEvent;
    private String htmlHeader,  htmlFooter;
    private Address monitorWebAddress,  bootstrapWebAddress;
    int webPort;

    public VodWebApplication() {
        subscribe(handleInit, control);
        subscribe(handleWebRequest, web);
        subscribe(handleRandomSetNeighborsResponse, status);
    }
    Handler<VodWebApplicationInit> handleInit = new Handler<VodWebApplicationInit>() {

        public void handle(VodWebApplicationInit init) {
            self = init.getSelf();
            monitorWebAddress = init.getMonitorWebAddress();
            bootstrapWebAddress = init.getBootstrapWebAddress();
            webPort = init.getWebPort();
            logger = LoggerFactory.getLogger(VodWebApplication.class.getName() + "@" 
                    + self.getId());

            htmlHeader = getHtmlHeader();
            htmlFooter = "</body></html>";
        }
    };
    private Handler<WebRequest> handleWebRequest = new Handler<WebRequest>() {

        public void handle(WebRequest event) {
            String target = event.getTarget();
            logger.debug("Handling request {}", target);
            //System.out.println("Handling request " + target);
            requestEvent = event;

            RandomSetNeighborsRequest randomSetNeighborsRequest = new RandomSetNeighborsRequest();

            parts = 3;

            trigger(randomSetNeighborsRequest, status);

            WebResponse responseEvent = new WebResponse(htmlHeader,
                    requestEvent, 1, parts);
            trigger(responseEvent, web);
            responseEvent = new WebResponse(htmlFooter, requestEvent, parts,
                    parts);
            trigger(responseEvent, web);
        }
    };
    private Handler<RandomSetNeighborsResponse> handleRandomSetNeighborsResponse = new Handler<RandomSetNeighborsResponse>() {

        public void handle(RandomSetNeighborsResponse event) {
            String html = dumpGVodViewToHtml(event);

            WebResponse responseEvent = new WebResponse(html, requestEvent, 2,
                    parts);
            trigger(responseEvent, web);
        }
    };

    private String dumpGVodViewToHtml(RandomSetNeighborsResponse response) {
        StringBuilder sb = new StringBuilder();

        sb.append("<h3 align=\"center\">Utility : " + response.getUtility().getValue() + "</h3>");
        sb.append("<h3 align=\"center\">changes in UtilitySet per cycle : " +
                String.format("%.4f",response.getNeighbors().getUtilitySetNbChange()/response.getNeighbors().getNbCycles()) + "</h3>");
        sb.append("<h4 align=\"center\">changes in UpperSet per cycle: " +
                String.format("%.4f",response.getNeighbors().getUpperSetNbChange()/response.getNeighbors().getNbCycles()) + "</h4>");
        long atTime = response.getNeighbors().getAtTime();
        ArrayList<VodDescriptor> randomSetDescriptors = response.getNeighbors().getRandomSetDescriptors();
        sb.append(dumpSetToHtml("RandomSet", randomSetDescriptors, atTime));

        ArrayList<VodDescriptor> utilitySetDescriptors = response.getNeighbors().getUtilitySetDescriptors();
        sb.append(dumpSetToHtml("UtilitySet", utilitySetDescriptors, atTime));

        ArrayList<VodDescriptor> upperSetDescriptors = response.getNeighbors().getUpperSetDescriptors();
        sb.append(dumpSetToHtml("upperSet", upperSetDescriptors, atTime));

        ArrayList<VodDescriptor> belowSetDescriptors = response.getNeighbors().getBelowSetDescriptors();
        sb.append(dumpSetToHtml("belowSet", belowSetDescriptors, atTime));

        ArrayList<VodDescriptor> neighbourhoodDescriptors = response.getNeighbors().getNeighbourhoodDescriptors();
        sb.append(dumpSetToHtml("Neighbourhood", neighbourhoodDescriptors, atTime));
        return sb.toString();
    }

    private String dumpSetToHtml(String name, ArrayList<VodDescriptor> set, long atTime) {
        StringBuilder sb = new StringBuilder();

        sb.append("<h2 align=\"center\" class=\"style2\">" + name + ":</h2>");

        Collections.sort(set);

        if (set != null) {
            sb.append("<table width=\"500\" border=\"2\" align=\"center\"><tr>");
            sb.append("<th class=\"style2\" width=\"100\" scope=\"col\">Count</th>");
            sb.append("<th class=\"style2\" width=\"100\" scope=\"col\">Peer</th>");
            sb.append("<th class=\"style2\" width=\"100\" scope=\"col\">Age</th>");
            sb.append("<th class=\"style2\" width=\"100\" scope=\"col\">Freshness</th>");
            sb.append("<th class=\"style2\" width=\"100\" scope=\"col\">Utility</th>");
            sb.append("<th class=\"style2\" width=\"100\" scope=\"col\">ref</th></tr>");
            int i = 0;
            for (VodDescriptor descriptor : set) {
                sb.append("<tr><td><div align=\"center\">").append(++i);
                sb.append("</div></td>");
                sb.append("<td><div align=\"center\">");
                appendPeerLink(sb, descriptor.getVodAddress());
                sb.append("</div></td>");
                sb.append("<td><div align=\"center\">");
                sb.append(descriptor.getAge()).append("</div></td>");
                sb.append("<td><div align=\"center\">");
                sb.append(durationToString(atTime - atTime)).append("</div></td>");
                sb.append("<td><div align=\"center\">");
                sb.append(descriptor.getUtility().getValue()).append("</div></td>");
                sb.append("<td><div align=\"center\">");
                sb.append(descriptor.getRefs());
                sb.append("</div></td></tr>");
            }
            sb.append("</table>");
        }
        return sb.toString();
    }

    private String getHtmlHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transiti");
        sb.append("onal//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-tr");
        sb.append("ansitional.dtd\"><html xmlns=\"http://www.w3.org/1999/");
        sb.append("xhtml\"><head><meta http-equiv=\"Content-Type\" conten");
        sb.append("t=\"text/html; charset=utf-8\" /><title>GVod Peer ");
        sb.append(self.getId());
        sb.append("</title><style type=\"text/css\"><!--.style2 {font-fam");
        sb.append("ily: Arial, Helvetica, sans-serif; color: #0099FF;}-->");
        sb.append("</style></head><body><h1 align=\"center\" class=\"styl");
        sb.append("e2\">GVod Peer ").append(self.getId()).append(
                "</h1>");
        sb.append("<div align=\"center\">Peer address: ");
        sb.append(self.getPeerAddress()).append("</div>");
        sb.append("<table width=\"500\" border=\"0\" align=\"center");
        sb.append("\"><tr><td class=\"style2\" width=\"250\" scope=\"col\">");
        sb.append("<div align=\"center\">");
        appendPeerLink(sb, monitorWebAddress, "<b>Monitor Server</b>");
        sb.append("</div></td><td class=\"style2\" width=");
        sb.append("\"250\" scope=\"col\"><div align=\"center\">");
        appendPeerLink(sb, bootstrapWebAddress, "<b>Bootstrap Server</b>");
        sb.append("</div></td></tr></table><hr>");
        return sb.toString();
    }

    private final void appendPeerLink(StringBuilder sb, VodAddress address) {
        sb.append("<a href=\"http://");
        sb.append(address.getPeerAddress().getIp().getHostAddress());
        sb.append(":").append(webPort).append("/");
        sb.append(address.getPeerAddress().getId()).append("/").append("\">");
        sb.append(address.toString()).append("</a>");
    }

    private final void appendPeerLink(StringBuilder sb, Address peerAddress,
            String label) {
        sb.append("<a href=\"http://");
        //    System.out.println("peerAddress = " + peerAddress);
        //    System.out.println("peerAddress.getIp() = " + peerAddress.getIp());
        sb.append(peerAddress.getIp().getHostAddress());
        sb.append(":").append(webPort).append("/");
        sb.append(peerAddress.getId()).append("/GVod").append("\">");
        sb.append(label).append("</a>");
    }

    private String durationToString(long duration) {
        StringBuilder sb = new StringBuilder();

        // get duration in seconds
        duration /= 1000;

        int s = 0, m = 0, h = 0, d = 0, y = 0;
        s = (int) (duration % 60);
        // get duration in minutes
        duration /= 60;
        if (duration > 0) {
            m = (int) (duration % 60);
            // get duration in hours
            duration /= 60;
            if (duration > 0) {
                h = (int) (duration % 24);
                // get duration in days
                duration /= 24;
                if (duration > 0) {
                    d = (int) (duration % 365);
                    // get duration in years
                    y = (int) (duration / 365);
                }
            }
        }
        boolean printed = false;
        if (y > 0) {
            sb.append(y).append("y");
            printed = true;
        }
        if (d > 0) {
            sb.append(d).append("d");
            printed = true;
        }
        if (h > 0) {
            sb.append(h).append("h");
            printed = true;
        }
        if (m > 0) {
            sb.append(m).append("m");
            printed = true;
        }
        if (s > 0 || printed == false) {
            sb.append(s).append("s");
        }
        return sb.toString();
    }
}