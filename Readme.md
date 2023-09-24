# WhoIsApi

A rest API written in java to lookup domain infos.

## Setup

### Java 

This project requires [NeoUtil](https://github.com/Neoministein/NeoUtil) to build locally.

- JDK 17
- Maven 3.9.3
- Main class: io.helidon.microprofile.cdi.Main

### Docker

Build container
```shell
docker-compose -f ./install/docker/base/docker-compose.yml build --no-cache
```
Start container
```shell
docker-compose -f ./install/docker/base/docker-compose.yml up
```
Stop container
```shell
docker-compose -f ./install/docker/base/docker-compose.yml stop
```
## API

Default port: `8110`

### Lookup domain info

GET `api/v1/lookup`

Requires path param: 

- domain 
  - Specifies the domain to lookup. Accepts input like:
    - Normal domains: google.com
    - Http Addresses: http://google.com/search
    - Subdomains: domains.google.com

Request example:

`http://127.0.0.1:8110/api/v1/lookup?domain=google.com`

Response example:
```json
{
  "domain": "google.com",
  "data": {
    "WHOIS Server": "whois.markmonitor.com",
    "Registrar": "MarkMonitor, Inc.",
    "Registrar IANA ID": "292",
    "Registrar Abuse Contact Email": "abusecomplaints@markmonitor.com",
    "Registrar Abuse Contact Phone": "+1.2086851750",
    "Name Server": "ns4.google.com",
    "DNSSEC": "unsigned",
    "For more information on Whois status codes, please visit https": "//icann.org/epp",
    "NOTICE": "The expiration date displayed in this record is the date the",
    "TERMS OF USE": "You are not authorized to access or query our Whois",
    "by the following terms of use": "You agree that you may use this Data only",
    "to": "(1) allow, enable, or otherwise support the transmission of mass",
    "Registrant Organization": "Google LLC",
    "Registrant State/Province": "CA",
    "Registrant Country": "US",
    "Admin Organization": "Google LLC",
    "Admin State/Province": "CA",
    "Admin Country": "US",
    "Tech Organization": "Google LLC",
    "Tech State/Province": "CA",
    "Tech Country": "US",
    "https": "//domains.markmonitor.com/whois",
    "Visit MarkMonitor at https": "//www.markmonitor.com",
    "Domain Name": "google.com",
    "Registry Domain ID": "2138514_DOMAIN_COM-VRSN",
    "Registrar WHOIS Server": "whois.markmonitor.com"
  },
  "lookupFailed": false,
  "lookupDate": 1695568450096 
}
```

The lookupDate is UNIX_EPOCH timestamp.

### Compare domain info

GET `api/v1/compare`

Requires path param:

- domain
  - Specifies the domain to lookup. Accepts input like:
    - Normal domains: google.com
    - Http Addresses: http://google.com/search
    - Subdomains: domains.google.com
- field
  - Defines the field which should be compared two
- value
  - The value that should be checked against

Request example:

`http://127.0.0.1:8110/api/v1/compare?domain=google.com&field=Registrant Country&value=US`

Response example:
``true``