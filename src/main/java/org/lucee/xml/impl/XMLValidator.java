/**
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package org.lucee.xml.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lucee.xml.XMLUtility;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;



public class XMLValidator extends XMLEntityResolverDefaultHandler {

	@Override
	public InputSource resolveEntity(String publicID, String systemID)
			throws SAXException {
		//print.out(publicID+":"+systemID);
		return super.resolveEntity(publicID, systemID);
	}

	private List<String> warnings;
	private List<String> errors;
	private List<String> fatals;
	private boolean hasErrors;
	private String strSchema;

	public XMLValidator(InputSource validator, String strSchema) {
		super(validator);
		this.strSchema=strSchema;
	}
	

	private void release() {
		warnings=null;
		errors=null;
		fatals=null;
		hasErrors=false;
	}
	
    @Override
    public void warning(SAXParseException spe)	{
    	log(spe,"Warning",warnings);
    }
	
	@Override
	public void error(SAXParseException spe) {
		hasErrors=true;
    	log(spe,"Error",errors);
    }

    @Override
    public void fatalError(SAXParseException spe) throws SAXException	{
		hasErrors=true;
    	log(spe,"Fatal Error",fatals);
    }
    
    private void log(SAXParseException spe, String type,List<String> array)	{
        StringBuffer sb = new StringBuffer("["+type+"] ");
        
        String id = spe.getSystemId();
        if(!Utils.isEmpty(id)) {
        	int li=id.lastIndexOf('/');
        	if(li!=-1)sb.append(id.substring(li+1));
        	else sb.append(id);
        }
        sb.append(':');
        sb.append(spe.getLineNumber());
        sb.append(':');
        sb.append(spe.getColumnNumber());
        sb.append(": ");
        sb.append(spe.getMessage());
        sb.append(" ");
        array.add(sb.toString());
    }
    
	public Map<String,Object> validate(InputSource xml) throws IOException {
		warnings=new ArrayList<String>();
		errors=new ArrayList<String>();
		fatals=new ArrayList<String>();
		
		try {
            XMLReader parser = XMLUtility.createXMLReader("org.apache.xerces.parsers.SAXParser");
            parser.setContentHandler(this);
            parser.setErrorHandler(this);
            parser.setEntityResolver(this);
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
            //if(!validateNamespace)
            if(!Utils.isEmpty(strSchema))
            	parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", strSchema);
            parser.parse(xml);
        }
        catch(SAXException e) { }
        
        // result
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("warnings", warnings);
        result.put("errors", errors);
        result.put("fatalerrors", fatals);
        result.put("status", !hasErrors);
        release();
        return result;
	}

}