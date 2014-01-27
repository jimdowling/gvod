package se.sics.gvod.ls.main;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 * Jersey REST client generated for REST resource:StatsResource
 * [/video.stats]<br> USAGE:
 * <pre>
 *        StatsRestClient client = new StatsRestClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 */
public class BootstrapRestClient {

//    private WebResource webResource;
//    private Client client;
//
//    public BootstrapRestClient(String url) {
//        ClientConfig config = new DefaultClientConfig();
//        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
//        client = Client.create(config);
//        // Read configuration
//        if (!url.contains("http://")) {
//            url = "http://" + url;
//        }
//        final String base_uri = url;
//        webResource = client.resource(base_uri);
//    }
//
//    public ClientResponse createNodes(Nodes s) {
//        return webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, s);
//    }
//
//    public ClientResponse editNodes(Nodes s) {
//        return webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).put(ClientResponse.class, s);
//    }
//
//    public ClientResponse removeNodes(String id) {
//        return webResource.path(id).delete(ClientResponse.class);
//    }
//
//    public Nodes findNodes(String id) {
//        return webResource.path(statsPath).path(id).accept(MediaType.APPLICATION_JSON).get(Nodes.class);
//    }
//
//    public List<Nodes> findAll() {
//        return webResource.accept(MediaType.APPLICATION_JSON).get(new GenericType<List<Nodes>>() {
//        });
//    }
//
//    public int getNodesCount() {
//        return Integer.parseInt(webResource.path("count").accept(MediaType.TEXT_PLAIN).get(String.class));
//    }
//
//
//    public void close() {
//        client.destroy();
//    }

    public static void main(String args[]) {
//        BootstrapRestClient c = new BootstrapRestClient("http://127.0.0.1:8080/nodes");
//        Stats s1 = new Stats(4, 1, false);
//        s1.setNatType(VodAddress.NatType.NAT);
//        ClientResponse cr1 = c.createStats(s1);
//        System.out.println(cr1);
//        if (cr1.getClientResponseStatus().getFamily().equals(ClientResponse.Status.fromStatusCode(200).getFamily())) {
//            // OK
//        } else {
//            // ?
//        }
//        Experiment e = new Experiment(0);
//        e.setArguments("test-args");
//        e.setIterations((short) 1);
//        e.setScenario("test-scenario");
//        e.setEndTs(Date.valueOf("2038-01-19"));
//        e.setStatus(Experiment.Status.opened);
//        System.out.println(c.createExperiment(e));
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(BootstrapRestClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        e.setStatus(Experiment.Status.finished);
//        e.setEndTs(null);
//        System.out.println(c.editExperiment(e));
//
//        System.out.println("Stats count: " + c.getStatsCount());
//        System.out.println("Experiment count: " + c.getExperimentCount());
//        c.close();
    }
}
