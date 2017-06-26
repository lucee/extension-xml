package org.lucee.xml.impl;
import java.io.BufferedInputStream;
import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLEntityResolverDefaultHandler extends DefaultHandler {

		private InputSource entityRes;

		public XMLEntityResolverDefaultHandler(InputSource entityRes) {
			this.entityRes=entityRes;
		}
		
		@Override
		public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
			//if(entityRes!=null)print.out("resolveEntity("+(entityRes!=null)+"):"+publicID+":"+systemID);
			
			if(entityRes!=null) return entityRes;
			try {
				return new InputSource(
						new BufferedInputStream(new URL(systemID).openStream()));
			} 
			catch (Exception t) {
				return null;
			}
		}
	}