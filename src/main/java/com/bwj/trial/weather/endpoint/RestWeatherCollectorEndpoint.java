package com.bwj.trial.weather.endpoint;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bwj.trial.weather.WeatherException;
import com.bwj.trial.weather.model.AirportData;
import com.bwj.trial.weather.model.DataPoint;
import com.bwj.trial.weather.service.WeatherService;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport
 * weather collection sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {

    /**
     * shared gson json to object factory
     */
    private final static Gson gson = new Gson();

    private final WeatherService weatherService;

    @Inject
    public RestWeatherCollectorEndpoint(WeatherService weatherService) {
        super();
        this.weatherService = weatherService;
    }

    @GET
    @Path("/ping")
    @Override
    public Response ping() {
        return Response.status(Response.Status.OK).entity("ready").build();
    }

    @POST
    @Path("/weather/{iata}/{pointType}")
    @Override
    public Response updateWeather(@PathParam("iata") String iataCode, @PathParam("pointType") String pointType,
            String datapointJson) {
        try {
            if (iataCode == null || "".equals(iataCode)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            boolean result = weatherService.addDataPoint(iataCode, pointType,gson.fromJson(datapointJson, DataPoint.class));
            
            Status status = result ? Response.Status.OK : Response.Status.BAD_REQUEST;
            
            return Response.status(status).build();

        } catch (WeatherException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        
    }

    @GET
    @Path("/airports")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getAirports() {

        Set<String> result = weatherService.getIataCodes();

        return Response.status(Response.Status.OK).entity(result).build();
    }

    @GET
    @Path("/airport/{iata}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getAirport(@PathParam("iata") String iata) {

        AirportData ad = weatherService.getAirportDataByIata(iata);

        return Response.status(Response.Status.OK).entity(ad).build();
    }

    @POST
    @Path("/airport/{iata}/{lat}/{long}")
    @Override
    public Response addAirport(@PathParam("iata") String iata, @PathParam("lat") String latString,
            @PathParam("long") String longString) {

        if (iata == null || latString == null || longString == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        boolean result = weatherService.addAirport(iata, latString, longString);
        
        Status status = result ? Response.Status.OK : Response.Status.BAD_REQUEST;
        
        return Response.status(status).build();

    }

    @DELETE
    @Path("/airport/{iata}")
    @Override
    public Response deleteAirport(@PathParam("iata") String iata) {

        if (iata == null || "".equals(iata)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        boolean result = weatherService.deleteAirport(iata);
        
        Status status = result ? Response.Status.OK : Response.Status.BAD_REQUEST;
        
        return Response.status(status).build();
    }

    @GET
    @Path("/exit")
    @Override
    public Response exit() {
        System.exit(0);
        return Response.noContent().build();
    }

}
