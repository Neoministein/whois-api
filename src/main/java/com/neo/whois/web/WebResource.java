package com.neo.whois.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.whois.impl.DnsLookupService;
import com.neo.whois.persistence.DnsRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

@ApplicationScoped
@Path(WebResource.RESOURCE_LOCATION)
@Produces(MediaType.TEXT_PLAIN + "; charset=UTF-8")
public class WebResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebResource.class);

    public static final String RESOURCE_LOCATION = "/api/v1/";

    @Inject
    protected DnsLookupService dnsLookupService;

    @GET
    @Path("lookup")
    public Response lookup(@QueryParam("domain") String name) {
        LOGGER.info("Received lookup request for domain [{}]", name);
        if (StringUtils.isEmpty(name)) {
            LOGGER.warn("Missing query parameter [domain]");
            return createErrorResponse(400, "Missing query parameter [domain]");
        }

        URI uri;
        try {
            uri = new URI(name);
        } catch (URISyntaxException e) {
            LOGGER.error("Malformed Domain [{}]", name);
            return Response.status(400).entity("{\"error\": \"Malformed Domain [" + name +"]\"}").build();
        }

        String domain = parseDomain(uri);
        DnsRecord dnsRecord = dnsLookupService.lookUpDnsRecord(domain);

        if (dnsRecord.isLookupFailed()) {
            return Response.status(404).entity(JsonUtil.toJson(dnsRecord)).build();
        }
        return Response.status(200).entity(JsonUtil.toJson(dnsRecord)).build();
    }

    @GET
    @Path("compare")
    public Response compare(@QueryParam("domain") String name, @QueryParam("field") String field, @QueryParam("value") String value) {
        LOGGER.info("Received request for domain [{}]", name);
        if (StringUtils.isEmpty(name)) {
            LOGGER.warn("Missing query parameter [domain]");
            return createErrorResponse(400, "Missing query parameter [domain]");
        }
        if (StringUtils.isEmpty(field)) {
            LOGGER.warn("Missing query parameter [field]");
            return createErrorResponse(400, "Missing query parameter [field]");
        }
        if (StringUtils.isEmpty(value)) {
            LOGGER.warn("Missing query parameter [value]");
            return createErrorResponse(400, "Missing query parameter [value]");
        }

        URI uri;
        try {
            uri = new URI(name);
        } catch (URISyntaxException e) {
            LOGGER.error("Malformed Domain [{}]", name);
            return Response.status(400).entity("{\"error\": \"Malformed Domain [" + name +"]\"}").build();
        }

        String domain = parseDomain(uri);
        DnsRecord dnsRecord = dnsLookupService.lookUpDnsRecord(domain);

        if (dnsRecord.isLookupFailed()) {
            return Response.status(200).entity(false).build();
        }

        JsonNode fieldNode = dnsRecord.getData().get(field);
        if (fieldNode != null && fieldNode.asText().equals(value)) {
            return Response.status(200).entity(true).build();
        }
        return Response.status(200).entity(false).build();
    }

    public String parseDomain(URI uri) {
        String domain = uri.getHost();
        if (domain == null) {
            domain = uri.getPath();
        }
        long numberOfDots = domain.codePoints().filter(ch -> ch == '.').count();
        if (numberOfDots == 1) {
            return domain;
        }

        int lastIndex = -1;
        int secondLastIndex = -1; // Index of second last occurrence of 'e'

        for (int i = 0; i < domain.length(); i++) {
            if (domain.charAt(i) == '.') {
                secondLastIndex = lastIndex;
                lastIndex = i;
            }
        }

        return domain.substring(secondLastIndex + 1);
    }

    protected Response createErrorResponse(int status, String message) {
        return Response.status(status).entity(JsonUtil.toJson(JsonUtil.emptyObjectNode().put("error", message))).build();
    }
}
