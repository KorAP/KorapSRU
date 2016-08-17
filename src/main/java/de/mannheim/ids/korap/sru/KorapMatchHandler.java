package de.mannheim.ids.korap.sru;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Handler for parsing the match snippet from KorAP search API. 
 *  
 * @author margaretha
 *
 */
public class KorapMatchHandler extends DefaultHandler{
	
	private KorapMatch match;
	boolean isLeftContext, isRightContext, isKeyword, isMore;
	private StringBuilder sbLeft, sbRight, sbKey;
	 
	public KorapMatchHandler(KorapMatch m) {
		match = m;		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);		
		
		if (qName.equals("span") &&  attributes.getQName(0).equals("class")){
			switch (attributes.getValue(0)) {
			case "context-left":
				isLeftContext = true;
				sbLeft = new StringBuilder();
				break;			
			case "context-right":
				isRightContext = true;
				sbRight = new StringBuilder();
				break;
			case "match":				
				isKeyword = true;
				sbKey = new StringBuilder();
				break;
			case "more":
				isMore = true;
				break;
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (qName.equals("span")){
			if (isMore){
				isMore = false;
			}
			else if (isLeftContext){
				match.setLeftContext(sbLeft.toString());
				isLeftContext = false;
			}
			else if (isKeyword){
				match.setKeyword(sbKey.toString());
				isKeyword = false;
			}
			else if (isRightContext){
				match.setRightContext(sbRight.toString());
				isRightContext = false;
			}
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (isKeyword){
			sbKey.append(ch, start, length);
		}
		else if (isLeftContext){
			sbLeft.append(ch, start, length);
		}		
		else if (isRightContext){
			sbRight.append(ch, start, length);
		}		
	}
}
