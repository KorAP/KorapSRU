# KorapSRU

KorapSRU is the [CLARIN Federated Content Search (FCS)](https://www.clarin.eu/content/federated-content-search-clarin-fcs) endpoint for KorAP. It implements FCS specifications and connects the CLARIN FCS client [Aggregator](http://weblicht.sfs.uni-tuebingen.de/Aggregator/) and KorAP. Thus, public resources in KorAP is accessible from Aggregator through KorapSRU.

## Supported FCS Specifications

CLARIN defines FCS specifications to allow distributed search across multiple heterogenous search engines in a uniform way. FCS specifications are built on the [SRU/CQL protocol](http://www.loc.gov/standards/sru/) for communications between its client and endpoint. FCS 1.0 specification supports SRU (Search Retrieve via URL) 1.2 and FCS 2.0 specification SRU 2.0. 

[KorapSRU 1.0.1 release] (https://github.com/KorAP/KorapSRU/releases/tag/release-1.0.1) implements FCS 1.0 specification and supports basic search using simple CQL (Contextual Query Language) for term query, phrase query and boolean query. FCS 2.0 specification is implemented in the newest version of KorapSRU, but it has not been released yet. It supports extended search (e.g. annotation search) that can be formulated using FCS Query Language (FCSQL) developed based on Corpus Query Processor([CQP](http://cwb.sourceforge.net/files/CQP_Tutorial/)).

Usually CQL and FCSQL queries are translated into the native language of a search engine in an FCS endpoint. Since KorAP supports multiple query languages and has its own query translator [Koral](https://github.com/KorAP/Koral), the translation is implemented in Koral, not in KorapSRU. This allows KorAP users to use CQL and FCSQL in a KorAP user interface such as Kalamar](https://github.com/KorAP/Kalamar).

## Supported SRU requests

* SRU explain request

  gives general information about KorapSRU and some default search setting such as the number of records it retrieves per page (see [http://clarin.ids-mannheim.de/korapsru?operation=explain](http://clarin.ids-mannheim.de/korapsru?operation=explain)). To obtain more information, x-fcs-endpoint-description=true must be added as an extra request parameter (see [http://clarin.ids-mannheim.de/korapsru?operation=explain&x-fcs-endpoint-description=true] (http://clarin.ids-mannheim.de/korapsru?operation=explain&x-fcs-endpoint-description=true)).

* SRU search retrieve request  

  contains a CQL or FCSQL query, for example [http://clarin.ids-mannheim.de/korapsru?operation=searchRetrieve&query=das%20Buch&version=1.2] (http://clarin.ids-mannheim.de/korapsru?operation=searchRetrieve&query=das%20Buch&version=1.2). KorapSRU forwards the CQL or FCSQL query in an SRU search retrieve request URL to Kustvakt, the API provider of KorAP managing the communications among all KorAP components. Moreover, KorapSRU transforms the query results from Kustvakt into an SRU response.
  
## Software Requirements
  
* Java 7 (JDK 1.7 with JCE or OpenJDK 7)
 
* Tomcat 7

## Installation

KorapSRU is built based on FCSSimpleEndpoint library provided by CLARIN. KorapSRU 1.0.2-SNAPSHOT uses the latest FCSSimpleEndpoint version 1.3.0 available from CLARIN Nexus repository. To allow maven to download the library using JDK 1.7, an additional Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 7.

To install a war file of KorapSRU, go to the root directory of the project and run

> mvn install -Dhttps.protocols=TLS1.2

in  a terminal.


