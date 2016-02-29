package de.mannheim.ids.korap.sru;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUSearchResultSet;
import eu.clarin.sru.server.fcs.XMLStreamWriterHelper;

public class KorapSRUSearchResultSet extends SRUSearchResultSet {

	private Logger logger = (Logger) LoggerFactory
			.getLogger(KorapSRUSearchResultSet.class);
	
	private int i = -1;
	private KorapResult korapResult;
	private String dataview;
	private SAXParser saxParser;

	public KorapSRUSearchResultSet(SRUDiagnosticList diagnostics,
			KorapResult korapResult, String dataview)
			throws SRUException {
		super(diagnostics);

		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		try {
			saxParser = saxParserFactory.newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR);
		}

		this.korapResult = korapResult;
		this.dataview = dataview;
	}

	@Override
	public int getTotalRecordCount() {
		return korapResult.getTotalResults();
	}

	@Override
	public int getRecordCount() {
		return korapResult.getMatchSize();
	}

	@Override
	public String getRecordSchemaIdentifier() {		
		return KorapSRU.CLARIN_FCS_RECORD_SCHEMA;
	}

	@Override
	public boolean nextRecord() throws SRUException {
		return (++i < korapResult.getMatchSize() ? true : false);
	}

	@Override
	public String getRecordIdentifier() {
		return korapResult.getMatch(i).getID();
	}

	@Override
	public void writeRecord(XMLStreamWriter writer) throws XMLStreamException {
		KorapMatch match = korapResult.getMatch(i);
		String snippet = "<snippet>" + match.getSnippet() + "</snippet>";
		InputStream is = new ByteArrayInputStream(snippet.getBytes());
		try {
			saxParser.parse(is, new KorapMatchHandler(match));
		} catch (SAXException | IOException e) {
			// /throw e;
		}
		
		if (dataview.equals("kwic")) {
			XMLStreamWriterHelper.writeResourceWithKWICDataView(writer,
					match.getID(), KorapSRU.redirectBaseURI + match.getID(),
					match.getLeftContext(), match.getKeyword(),
					match.getRightContext());
			
		} else {
			XMLStreamWriterHelper.writeResourceWithHitsDataView(writer,
					match.getID(), KorapSRU.redirectBaseURI + match.getID(),
					match.getLeftContext(), match.getKeyword(),
					match.getRightContext());
		}
	}
}
