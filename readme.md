# KorapSRU

KorapSRU is the [CLARIN Federated Content Search (FCS)](https://www.clarin.eu/content/federated-content-search-clarin-fcs) endpoint for KorAP. It implements FCS specifications and connects the CLARIN FCS client [Aggregator](http://weblicht.sfs.uni-tuebingen.de/Aggregator/) and KorAP. Thus, public resources in KorAP are accessible from Aggregator through KorapSRU.

## Supported FCS Specifications

CLARIN defines FCS specifications to allow distributed search across multiple heterogenous search engines in a uniform way. FCS specifications are built on the [SRU/CQL protocol](http://www.loc.gov/standards/sru/) for communications between its client and endpoint. FCS 1.0 specification supports SRU (Search Retrieve via URL) 1.2 and FCS 2.0 specification supports SRU 2.0. 


[KorapSRU 1.0.1 release](https://github.com/KorAP/KorapSRU/releases/tag/release-1.0.1) implements FCS 1.0 specification and supports basic search using simple CQL (Contextual Query Language) for term query, phrase query and boolean query. FCS 2.0 specification is implemented in the newest version of KorapSRU, but it has not been released yet. It supports extended search (e.g. annotation search) that can be formulated using FCS Query Language (FCSQL) developed based on Corpus Query Processor ([CQP](http://cwb.sourceforge.net/files/CQP_Tutorial/)). FCSQL is only available with SRU version 2.0, whilst CQL is available with SRU version 1.1, 1.2 and 2.0.

Usually CQL and FCSQL queries are translated into the native language of a search engine in an FCS endpoint. Since KorAP supports multiple query languages and has its own query translator [Koral](https://github.com/KorAP/Koral), the translation is implemented in Koral, not in KorapSRU. Therefore, KorAP users will also be able to use CQL and FCSQL.

## Supported SRU requests

### SRU explain request

gives general information about KorapSRU and some default search settings, for instance the number of records it retrieves per page. See: 
> [https://clarin.ids-mannheim.de/korapsru?operation=explain](http://clarin.ids-mannheim.de/korapsru?operation=explain)


To obtain more information such as supported annotation layers needed for requesting an extended search, 

```
x-fcs-endpoint-description=true 
```

must be added as an extra request parameter. See: 
> [https://clarin.ids-mannheim.de/korapsru?operation=explain&x-fcs-endpoint-description=true](http://clarin.ids-mannheim.de/korapsru?operation=explain&x-fcs-endpoint-description=true)

### SRU search retrieve request  

contains a CQL or FCSQL query. KorapSRU forwards the CQL or FCSQL query in an SRU search retrieve request URL to [Kustvakt](https://github.com/KorAP/Kustvakt), the API provider of KorAP managing the communications among all KorAP components. Moreover, KorapSRU transforms the query results from Kustvakt into an SRU response.

Examples:
* Basic search using CQL

Searching for all occurrences of term ```Buch``` (means book in German)
> [https://clarin.ids-mannheim.de/korapsru?operation=searchRetrieve&query=Buch&version=1.2](http://clarin.ids-mannheim.de/korapsru?operation=searchRetrieve&query=Buch&version=1.2)

* Annotation search using FCSQL 

Searching for all lemmas from Tree tagger annotations containing ```heit```,
in FCS query: ```[tt:lemma=".*heit"]```
> https://clarin.ids-mannheim.de/korapsru?operation=startRetrieve&query=%5Btt%3Alemma%3D%22.*heit%22%5D&queryType=fcs
  
  
## Software Requirements
  
* Java 7 (JDK 1.7 with JCE or OpenJDK 7)
 
* Tomcat 7

* Kustvakt

## Installation

Configure the service URI in ```/src/main/webapp/WEB-INF/web.xml``` to a Kustvakt server URI, for example:

```
<context-param>
  <param-name>korap.service.uri</param-name>
  <param-value>https://korap.ids-mannheim.de/api/v1.0/</param-value>
</context-param>
```

KorapSRU is built based on the FCSSimpleEndpoint library provided by CLARIN. KorapSRU 1.0.2-SNAPSHOT uses FCSSimpleEndpoint version 1.3.0 available from CLARIN Nexus repository. To allow Maven to download the library using JDK 1.7, an additional Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 7 is needed.

To install a war file of KorapSRU, go to the root directory of the project and run

```
$ mvn install -Dhttps.protocols=TLS1.2
```

in  a terminal.

## License

KorapSRU is published under the [BSD-2 License](https://raw.githubusercontent.com/KorAP/Kustvakt/master/LICENSE). It is developed as part of [CLARIN](https://www.clarin.eu/) and [KorAP](https://korap.ids-mannheim.de/), the Corpus Analysis Platform at the [Leibniz Institute for the German Language (IDS)](https://www.ids-mannheim.de/),
member of the [Leibniz Association](https://www.leibniz-gemeinschaft.de).
