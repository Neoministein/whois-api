package com.neo.whois.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.whois.persistence.DnsRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

@ApplicationScoped
@Transactional
public class DnsLookupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DnsLookupService.class);

    @PersistenceContext(name = "mainPersistence")
    protected EntityManager entityManager;


    public DnsRecord lookUpDnsRecord(String domain) {
        DnsRecord dnsRecord = internalLookUp(domain);
        if (dnsRecord != null) {
            return dnsRecord;
        }

        dnsRecord = externalLookup(domain);
        if (dnsRecord == null) {
            dnsRecord = new DnsRecord(domain);
        }

        entityManager.persist(dnsRecord);
        return dnsRecord;
    }


    private DnsRecord internalLookUp(String domain) {
        LOGGER.info("Preforming database lookup for domain [{}]", domain);
        String query = """
                SELECT r
                FROM DnsRecord r
                WHERE r.domain = :domain
                """;

        try {
            return entityManager.createQuery(query, DnsRecord.class).setParameter("domain", domain).getSingleResult();
        } catch (NoResultException ex) {
            LOGGER.info("No domain found with name [{}]", domain);
            return null;
        }
    }

    private DnsRecord externalLookup(String domain) {
        LOGGER.info("Looking for domain [{}] in whois network", domain);
        String response = executeWhoIsLookUpCommand(domain);
        if (response.contains("No whois information found.")) {
            LOGGER.info("Whois lookup [failed] for domain [{}]", domain);
            return null;
        }
        ObjectNode data = JsonUtil.emptyObjectNode();
        for (String line: response.split("[\\r\\n]+")) {
            if (line.contains(":")) {
                String[] linePart = line.split(":");

                if (linePart.length == 2) {
                    data.put(linePart[0].trim(), linePart[1].trim());
                }
            }
        }
        LOGGER.info("Whois lookup [success] for domain [{}]", domain);
        return new DnsRecord(domain, data);
    }

    private String executeWhoIsLookUpCommand(String domain) {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {System.getenv("com.neo.whois.path"), domain};


        try {
            Process proc = rt.exec(commands);
            return StringUtils.toString(proc.getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
