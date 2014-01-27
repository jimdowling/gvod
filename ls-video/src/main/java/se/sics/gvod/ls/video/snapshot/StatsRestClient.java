package se.sics.gvod.ls.video.snapshot;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class StatsRestClient {

    private WebResource webResource;
    private Client client;
    private String statsPath = "stats";
    private String experimentPath = "experiment";

    public StatsRestClient(String url) {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        client = Client.create(config);
        // Read configuration
        if (!url.contains("http://")) {
            url = "http://" + url;
        }
        final String base_uri = url;
        webResource = client.resource(base_uri);
    }

    public ClientResponse createStats(Stats s) {
        return webResource.path(statsPath).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, s);
    }

    public ClientResponse editStats(Stats s) {
        return webResource.path(statsPath).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).put(ClientResponse.class, s);
    }

    public ClientResponse removeStats(String id) {
        return webResource.path(statsPath).path(id).delete(ClientResponse.class);
    }

    public Stats findStats(String id) {
        return webResource.path(statsPath).path(id).accept(MediaType.APPLICATION_JSON).get(Stats.class);
    }

    public List<Stats> findAll() {
        return webResource.path(statsPath).accept(MediaType.APPLICATION_JSON).get(new GenericType<List<Stats>>() {
        });
    }

    public int getStatsCount() {
        return Integer.parseInt(webResource.path(statsPath).path("count").accept(MediaType.TEXT_PLAIN).get(String.class));
    }

    public boolean experimentIterationExists(String id, String iteration) {
        ClientResponse cr = webResource.path("experiment").path(id).path(iteration).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        if (cr.getStatus() < 200 || cr.getStatus() > 299) {
            return false;
        }
        return true;
    }

    public ClientResponse createExperiment(Experiment e) {
        return webResource.path(experimentPath).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, e);
    }

    public ClientResponse editExperiment(Experiment e) {
        return webResource.path(experimentPath).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).put(ClientResponse.class, e);
    }

    public ClientResponse removeExperiment(String id) {
        return webResource.path(experimentPath).path(id).delete(ClientResponse.class);
    }

    public Experiment findExperiment(String id) {
        ClientResponse cr = webResource.path(experimentPath).path(id).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        if (cr.getStatus() != 200) {
            return null;
        } else {
            return cr.getEntity(Experiment.class);
        }
    }

    public int getExperimentCount() {
        ClientResponse cr = webResource.path(experimentPath).path("count").accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        if (200 <= cr.getStatus() && cr.getStatus() < 300) {
            return Integer.valueOf(cr.getEntity(String.class));
        } else {
            return 0;
        }
    }

    public Integer getMaxExperimentId() {
        String max = webResource.path(experimentPath).path("max").accept(MediaType.TEXT_PLAIN).get(String.class);
        if (max == null || max.equals("null")) {
            return null;
        } else {
            return Integer.parseInt(max);
        }
    }

    public void close() {
        client.destroy();
    }

    public static void main(String args[]) {
        StatsRestClient c = new StatsRestClient("http://127.0.0.1:8080/video");
//        Stats s1 = new Stats(4, 1, false);
//        s1.setNatType(VodAddress.NatType.NAT);
//        ClientResponse cr1 = c.createStats(s1);
//        System.out.println(cr1);
//        if (cr1.getClientResponseStatus().getFamily().equals(ClientResponse.Status.fromStatusCode(200).getFamily())) {
//            // OK
//        } else {
//            // ?
//        }
        Experiment e = new Experiment(0);
        e.setArguments("test-args");
        e.setIterations((short) 1);
        e.setScenario("test-scenario");
        e.setEndTs(Date.valueOf("2038-01-19"));
        e.setStatus(Experiment.Status.opened);
        System.out.println(c.createExperiment(e));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(StatsRestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        e.setStatus(Experiment.Status.finished);
        e.setEndTs(null);
        System.out.println(c.editExperiment(e));

        System.out.println("Stats count: " + c.getStatsCount());
        System.out.println("Experiment count: " + c.getExperimentCount());
        c.close();
    }
}
