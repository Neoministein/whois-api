package com.neo.whois.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.whois.impl.JsonNodeStringJavaDescriptor;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "DNS_RECORD")
public class DnsRecord {

    @Id
    @Column(name = "DOMAIN")
    private String domain;

    @Convert(converter = JsonNodeStringJavaDescriptor.class)
    @Column(name = "DATA", columnDefinition = "text")
    private JsonNode data;

    @Column(name = "LOOKUP_FAILED")
    private boolean lookupFailed;

    private Instant lookupDate;

    public DnsRecord(String name, JsonNode data) {
        this.domain = name;
        this.data = data;
        this.lookupFailed = false;
        this.lookupDate = Instant.now();
    }


    public DnsRecord(String name) {
        this.domain = name;
        this.lookupFailed = true;
        this.lookupDate = Instant.now();
    }

    protected DnsRecord() {
    }

    public String getDomain() {
        return domain;
    }

    public JsonNode getData() {
        return data;
    }

    public boolean isLookupFailed() {
        return lookupFailed;
    }

    public Instant getLookupDate() {
        return lookupDate;
    }
}
