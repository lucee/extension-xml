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
 * Lesser General public static License for more details.
 * 
 * You should have received a copy of the GNU Lesser General public static 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package org.lucee.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.processor.TransformerFactoryImpl;
import org.ccil.cowan.tagsoup.Parser;
import org.lucee.xml.impl.Utils;
import org.lucee.xml.impl.XMLValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 */
public final class XMLUtility {
	

	public final static String NON_VALIDATING_DTD_GRAMMAR="http://apache.org/xml/features/nonvalidating/load-dtd-grammar";
	public final static String NON_VALIDATING_DTD_EXTERNAL="http://apache.org/xml/features/nonvalidating/load-external-dtd";
	
	public final static String VALIDATION_SCHEMA="http://apache.org/xml/features/validation/schema";
	public final static String VALIDATION_SCHEMA_FULL_CHECKING="http://apache.org/xml/features/validation/schema-full-checking";
	
	public static final short UNDEFINED_NODE=-1;
	
    /*
    	public static static final Collection.Key XMLCOMMENT = KeyImpl.intern("xmlcomment");
    	public static static final Collection.Key XMLTEXT = KeyImpl.intern("xmltext");
    	public static static final Collection.Key XMLCDATA = KeyImpl.intern("xmlcdata");
    	public static static final Collection.Key XMLCHILDREN = KeyImpl.intern("xmlchildren");
    	public static static final Collection.Key XMLNODES = KeyImpl.intern("xmlnodes");
    	public static static final Collection.Key XMLNSURI = KeyImpl.intern("xmlnsuri");
    	public static static final Collection.Key XMLNSPREFIX = KeyImpl.intern("xmlnsprefix");
    	public static static final Collection.Key XMLROOT = KeyImpl.intern("xmlroot");
    	public static static final Collection.Key XMLPARENT = KeyImpl.intern("xmlparent");
    	public static static final Collection.Key XMLNAME = KeyImpl.intern("xmlname");
    	public static static final Collection.Key XMLTYPE = KeyImpl.intern("xmltype");
    	public static static final Collection.Key XMLVALUE = KeyImpl.intern("xmlvalue");
    	public static static final Collection.Key XMLATTRIBUTES = KeyImpl.intern("xmlattributes");
	*/
    
	//static DOMParser parser = new DOMParser();
	private static DocumentBuilder docBuilder;
	//private static DocumentBuilderFactory factory;
    private static TransformerFactory transformerFactory;
	
    
    
    public static String unescapeXMLString(String str) {
      	
      	StringBuilder rtn=new StringBuilder();
      	int posStart=-1;
      	int posFinish=-1;
      	while((posStart=str.indexOf('&',posStart))!=-1) {
      		int last=posFinish+1;
      		
      		posFinish=str.indexOf(';',posStart);
      		if(posFinish==-1)break;
      		rtn.append(str.substring(last,posStart));
      		if(posStart+1<posFinish) {
      			rtn.append(unescapeXMLEntity(str.substring(posStart+1,posFinish)));
      		}
      		else {
      			rtn.append("&;");
      		}
      		
      		posStart=posFinish+1;
      	}
      	rtn.append(str.substring(posFinish+1));
      	return rtn.toString();
    }
    
   /* public static String unescapeXMLString2(String str) {

    	StringBuffer sb=new StringBuffer();
    	int index,last=0,indexSemi;
    	while((index=str.indexOf('&',last))!=-1) {
    		sb.append(str.substring(last,index));
    		indexSemi=str.indexOf(';',index+1);

    		if(indexSemi==-1) {
    			sb.append('&');
    			last=index+1;
    		}
    		else if(index+1==indexSemi) {
    			sb.append("&;");
    			last=index+2;
    		}
    		else {
    			sb.append(unescapeXMLEntity(str.substring(index+1,indexSemi)));
    			last=indexSemi+1;
    		}
    	}
      	sb.append(str.substring(last));
      	return sb.toString();
    }*/
    
    private static String unescapeXMLEntity(String str) {
    	if("lt".equals(str)) return "<";
    	if("gt".equals(str)) return ">";
    	if("amp".equals(str)) return "&";
    	if("apos".equals(str)) return "'";
    	if("quot".equals(str)) return "\"";
		return "&"+str+";";
	}
    
    
	public static String escapeXMLString(String xmlStr) {
    	char c;
    	StringBuffer sb=new StringBuffer();
    	int len=xmlStr.length();
    	for(int i=0;i<len;i++) {
    		c=xmlStr.charAt(i);
    		if(c=='<') 		sb.append("&lt;");
    		else if(c=='>')	sb.append("&gt;");
    		else if(c=='&')	sb.append("&amp;");
    		//else if(c=='\'')	sb.append("&amp;");
    		else if(c=='"')	sb.append("&quot;");
    		//else if(c>127) sb.append("&#"+((int)c)+";");
    		else sb.append(c);
    	}
    	return sb.toString();
    }
    
    
    public static TransformerFactory getTransformerFactory() {
    	//return TransformerFactory.newInstance();
    	if(transformerFactory==null)transformerFactory=new TransformerFactoryImpl();
        return transformerFactory;
    }
    

    public static final Document parse(InputSource xml,InputSource validator, boolean isHtml) 
        throws SAXException, IOException {
    	return parse(xml, validator, null, isHtml);
    }
    
    /**
     * parse XML/HTML String to a XML DOM representation
     * @param xml XML InputSource
     * @param isHtml is a HTML or XML Object
     * @return parsed Document
     */
    public static final Document parse(InputSource xml,InputSource validator, EntityResolver entityResolver, boolean isHtml) 
        throws SAXException, IOException {
        
        if(!isHtml) {
        	// try to load org.apache.xerces.jaxp.DocumentBuilderFactoryImpl, oracle impl sucks
        	DocumentBuilderFactory factory = newDocumentBuilderFactory();
        	
        	
        	//print.o(factory);
            if(validator==null) {
            	setAttributeEL(factory,NON_VALIDATING_DTD_EXTERNAL, Boolean.FALSE);
            	setAttributeEL(factory,NON_VALIDATING_DTD_GRAMMAR, Boolean.FALSE);
            }
            else {
            	setAttributeEL(factory,VALIDATION_SCHEMA, Boolean.TRUE);
            	setAttributeEL(factory,VALIDATION_SCHEMA_FULL_CHECKING, Boolean.TRUE);
            
                
            }
            
            
            factory.setNamespaceAware(true);
            factory.setValidating(validator!=null);
            
            try {
				DocumentBuilder builder = factory.newDocumentBuilder();
	            builder.setEntityResolver(entityResolver!=null?entityResolver:new XMLEntityResolverDefaultHandler(validator));
	            builder.setErrorHandler(new ThrowingErrorHandler(true,true,false));
	            return  builder.parse(xml);
			} 
            catch (ParserConfigurationException e) {
				throw new SAXException(e);
			}
            
	        /*DOMParser parser = new DOMParser();
	        print.out("parse");
	        parser.setEntityResolver(new XMLEntityResolverDefaultHandler(validator));
	        parser.parse(xml);
	        return parser.getDocument();*/
        }
        
        XMLReader reader = new Parser();
            reader.setFeature(Parser.namespacesFeature, true);
            reader.setFeature(Parser.namespacePrefixesFeature, true);
        
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            
            DOMResult result = new DOMResult();
            transformer.transform(new SAXSource(reader, xml), result);
            return getDocument(result.getNode());
        } 
        catch (Exception e) {
            throw new SAXException(e);
        }
    }
	
	private static DocumentBuilderFactory newDocumentBuilderFactory() {
		/*try{
    		return new DocumentBuilderFactoryImpl();
    	}
    	catch (Exception t) {
    		return DocumentBuilderFactory.newInstance();
    	}*/
		return DocumentBuilderFactory.newInstance();
	}

	private static void setAttributeEL(DocumentBuilderFactory factory,String name, Object value) {
		try{
			factory.setAttribute(name, value);
		}
		catch (Exception t){
			//SystemOut.printDate("attribute ["+name+"] is not allowed for ["+factory.getClass().getName()+"]");
		}
	}
	
	
    public static void replaceChild(Node newChild, Node oldChild) {
		Node nc = newChild;
		Node oc = oldChild;
		Node p = oc.getParentNode();
		if(nc!=oc)p.replaceChild(nc, oc);
	}

	
     public static boolean nameEqual(Node node, String name) {
		if(name==null) return false;
    	return name.equals(node.getNodeName()) || name.equals(node.getLocalName());
	}

	
    public static Element getRootElement(Node node) {
	    Document doc=null;
		if(node instanceof Document) doc=(Document) node;
		else doc=node.getOwnerDocument();
		return doc.getDocumentElement();
	}


	
    public static Document newDocument() throws ParserConfigurationException, FactoryConfigurationError {
		if(docBuilder==null) {
			docBuilder=newDocumentBuilderFactory().newDocumentBuilder();
		}
		return docBuilder.newDocument();
	}

	
    public static Document getDocument(NodeList nodeList) throws IOException {
		if(nodeList instanceof Document) return (Document)nodeList;
		int len=nodeList.getLength();
		for(int i=0;i<len;i++) {
			Node node=nodeList.item(i);
			if(node!=null) return node.getOwnerDocument();
		}
		throw new IOException("can't get Document from NodeList, in NoteList are no Nodes");
	}
	
	
    public static Document getDocument(Node node) {
		if(node instanceof Document) return (Document)node;
		return node.getOwnerDocument();
	}

	
    public static synchronized ArrayList<Node> getChildNodes(Node node, short type, String filter) {
		ArrayList<Node> rtn=new ArrayList<Node>();
		NodeList nodes=node.getChildNodes();
		int len=nodes.getLength();
		Node n;
		for(int i=0;i<len;i++) {
			try {
				n=nodes.item(i);
				if(n!=null && (type==UNDEFINED_NODE || n.getNodeType()==type)){
					if(filter==null || filter.equals(n.getLocalName()))
					rtn.add(n);
				}
			}
			catch (Exception t){}
		}
		return rtn;
	}
	
	public static synchronized List<Node> getChildNodesAsList(Node node, short type, String filter) {
		List<Node> rtn=new ArrayList<Node>();
		NodeList nodes=node.getChildNodes();
		int len=nodes.getLength();
		Node n;
		for(int i=0;i<len;i++) {
			try {
				n=nodes.item(i);
				if(n!=null && (n.getNodeType()==type|| type==UNDEFINED_NODE)){
					if(filter==null || filter.equals(n.getLocalName()))
					rtn.add(n);
				}
			}
			catch (Exception t){}
		}
		return rtn;
	}

	
    public static synchronized Node getChildNode(Node node, short type, String filter, int index) {
		NodeList nodes=node.getChildNodes();
		int len=nodes.getLength();
		Node n;
		int count=0;
		for(int i=0;i<len;i++) {
			try {
				n=nodes.item(i);
				if(n!=null && (type==UNDEFINED_NODE || n.getNodeType()==type)){
					if(filter==null || filter.equals(n.getLocalName())) {
						if(count==index) return n;
						count++;
					}
				}
			}
			catch (Exception t){}
		}
		return null;
	}
	
    public static Node[] getChildNodesAsArray(Node node, short type) {
    	ArrayList<Node> nodeList=getChildNodes(node, type,null);
        return nodeList.toArray(new Node[nodeList.size()]);
    }

    public static Node[] getChildNodesAsArray(Node node, short type, String filter) {
    	ArrayList<Node> nodeList=getChildNodes(node, type,filter);
        return  nodeList.toArray(new Node[nodeList.size()]);
    }
    
    /**
     * return all Element Children of a node
     * @param node node to get children from
     * @return all matching child node
     */
    public static Element[] getChildElementsAsArray(Node node) {
    	ArrayList<Node> nodeList=getChildNodes(node,Node.ELEMENT_NODE,null);
        return  nodeList.toArray(new Element[nodeList.size()]);
    }
    
    
    public static String transform(InputSource xml, InputSource xsl, Map<String,Object> parameters) throws TransformerException, SAXException, IOException {
    	return transform( parse( xml, null , false ), xsl, parameters );
    }

    
    public static String transform(Document doc, InputSource xsl, Map<String,Object> parameters) throws TransformerException {
    	StringWriter sw = new StringWriter();
    	TransformerFactory factory = getTransformerFactory();
    	factory.setErrorListener(SimpleErrorListener.THROW_FATAL);
		Transformer transformer = factory.newTransformer(new StreamSource(xsl.getCharacterStream()));
		if (parameters != null) {
			Iterator it = parameters.entrySet().iterator();
			while ( it.hasNext() ) {
				Map.Entry e = (Map.Entry) it.next();
				transformer.setParameter(e.getKey().toString(), e.getValue());
			}
		}
		transformer.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();
	}

	public static String getTypeAsString(Node node, boolean cftype) {
		String suffix=cftype?"":"_NODE";
		
        switch(node.getNodeType()) {
    		case Node.ATTRIBUTE_NODE: 				return "ATTRIBUTE"+suffix;
    		case Node.CDATA_SECTION_NODE: 			return "CDATA_SECTION"+suffix;
    		case Node.COMMENT_NODE: 				return "COMMENT"+suffix;
    		case Node.DOCUMENT_FRAGMENT_NODE: 		return "DOCUMENT_FRAGMENT"+suffix;
    		case Node.DOCUMENT_NODE: 				return "DOCUMENT"+suffix;
    		case Node.DOCUMENT_TYPE_NODE: 			return "DOCUMENT_TYPE"+suffix;
    		case Node.ELEMENT_NODE: 				return "ELEMENT"+suffix;
    		case Node.ENTITY_NODE: 					return "ENTITY"+suffix;
    		case Node.ENTITY_REFERENCE_NODE: 		return "ENTITY_REFERENCE"+suffix;
    		case Node.NOTATION_NODE: 				return "NOTATION"+suffix;
    		case Node.PROCESSING_INSTRUCTION_NODE: 	return "PROCESSING_INSTRUCTION"+suffix;
    		case Node.TEXT_NODE: 					return "TEXT"+suffix;
    		default: 								return "UNKNOW"+suffix;
        }
    }

	
    public static synchronized Element getChildWithName(String name, Element el) {
		Element[] children = getChildElementsAsArray(el);
		for(int i=0;i<children.length;i++) {
			if(name.equalsIgnoreCase(children[i].getNodeName()))
				return children[i];
		}
		return null;
	}
    
    public static InputSource toInputSource(File res, Charset cs) throws IOException {
        	String str = Utils.toString((res), cs);
        	return new InputSource(new StringReader(str));
    }

	public static InputSource toInputSource(Object value) throws IOException {
		if(value instanceof InputSource) {
            return (InputSource) value;
        }
		if(value instanceof String) {
            return toInputSource((String)value);
        }
		if(value instanceof StringBuffer) {
            return toInputSource(value.toString());
        }
		if(value instanceof File) {
        	String str = Utils.toString(((File)value), (Charset)null);
        	return new InputSource(new StringReader(str));
        }
		if(value instanceof InputStream) {
			InputStream is = (InputStream)value;
			try {
				String str = Utils.toString(is, (Charset)null);
	        	return new InputSource(new StringReader(str));
			}
			finally {
				Utils.closeSilent(is);
			}
        }
		if(value instanceof Reader) {
			Reader reader = (Reader)value;
			try {
				String str = Utils.toString(reader);
	        	return new InputSource(new StringReader(str));
			}
			finally {
				Utils.closeSilent(reader);
			}
        }
		if(value instanceof byte[]) {
			return new InputSource(new ByteArrayInputStream((byte[])value));
        }
		throw new IOException("cat cast object of type ["+value+"] to a Input for xml parser");
	}
	
	
	public static InputSource toInputSource(String xml) throws IOException {
		return new InputSource(new StringReader(xml.trim()));
	}
	
	
    public static Map<String,Object> validate(InputSource xml, InputSource schema, String strSchema) throws IOException {
    	return new XMLValidator(schema,strSchema).validate(xml);
    }

	
    public static void prependChild(Element parent, Element child) {
		Node first = parent.getFirstChild();
		if(first==null)parent.appendChild(child);
		else {
			parent.insertBefore(child, first);
		}
	}

	
    public static void setFirst(Node parent, Node node) {
		Node first = parent.getFirstChild();
		if(first!=null) parent.insertBefore(node, first);
		else parent.appendChild(node);
	}

	public static XMLReader createXMLReader(String optionalDefaultSaxParser) throws SAXException {
		if(optionalDefaultSaxParser==null) return XMLReaderFactory.createXMLReader();
		
		try{
			return XMLReaderFactory.createXMLReader(optionalDefaultSaxParser);
		}
		catch (Exception t){
			return XMLReaderFactory.createXMLReader();
		}
	}

	
	public static void writeTo(Node node, File file) throws IOException, TransformerException {
        OutputStream os=null;
        try {
			os=new FileOutputStream(file);
			if(!(os instanceof BufferedOutputStream)) os=new BufferedOutputStream(os);
			writeTo(node, new StreamResult(os),false,false,null,null,null);
		}
		finally {
			Utils.closeSilent(os);
		}
	}
		
	public static void writeTo(Node node,Result res,boolean omitXMLDecl,boolean indent, String publicId,String systemId,
			String encoding) throws IOException, TransformerException {
		
			Transformer t = getTransformerFactory().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT,indent?"yes":"no");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,omitXMLDecl?"yes":"no");
			//t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2"); 
			
			// optional properties
			if(!Utils.isEmpty(publicId,true))t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,publicId);
			if(!Utils.isEmpty(systemId,true))t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,systemId);
			if(!Utils.isEmpty(encoding,true))t.setOutputProperty(OutputKeys.ENCODING,encoding);
			
			t.transform(new DOMSource(node), res);
	}

	
	public static String toString(Node node, boolean omitXMLDecl, boolean indent,String publicId, String systemId, String encoding) throws IOException, TransformerException {
		StringWriter sw=new StringWriter();
		try {
			writeTo(node, new StreamResult(sw),omitXMLDecl,indent,publicId,systemId,encoding);
		} 
		finally {
			Utils.closeSilent(sw);
		}
		return sw.getBuffer().toString();
	}
	

	
	public static String toString(NodeList nodes,boolean omitXMLDecl, boolean indent) throws IOException, TransformerException {
		StringWriter sw=new StringWriter();
		try {
			int len = nodes.getLength();
			for(int i=0;i<len;i++){
				writeTo(nodes.item(i), new StreamResult(sw),omitXMLDecl,indent,null,null,null);
			}
		} 
		finally {
			Utils.closeSilent(sw);
		}
		return sw.getBuffer().toString();
	}

	
	public static String toString(Node node,String defaultValue) {
		StringWriter sw=new StringWriter();
		try {
			writeTo(node, new StreamResult(sw),false,false,null,null,null);
		} 
		catch (Exception t){
			return defaultValue;
		}
		finally {
			Utils.closeSilent(sw);
		}
		return sw.getBuffer().toString();
	}

	
	public static Node toNode(Object value) throws SAXException, IOException {
    	if(value instanceof Node) return (Node)value;
        return parse(toInputSource(value),null,false);
    }
	
    
    public static Document createDocument(File res, boolean isHTML) throws SAXException, IOException {
        InputStream is=null;
    	try {
            return parse(toInputSource(res, null),null,isHTML);
        }
        finally {
        	Utils.closeSilent(is);
        }
    }

    
    public static Document createDocument(String xml, boolean isHTML) throws SAXException, IOException {
        return parse(toInputSource(xml),null,isHTML);
    }

    
    public static Document createDocument(InputStream is, boolean isHTML) throws SAXException, IOException {
        return parse(new InputSource(is),null,isHTML);
    }
	
	

	static class SimpleErrorListener implements ErrorListener {

		public static final ErrorListener THROW_FATAL = new SimpleErrorListener(false,true,true);
		public static final ErrorListener THROW_ERROR = new SimpleErrorListener(false,false,true);
		public static final ErrorListener THROW_WARNING = new SimpleErrorListener(false,false,false);
		private boolean ignoreFatal;
		private boolean ignoreError;
		private boolean ignoreWarning;

		public SimpleErrorListener(boolean ignoreFatal, boolean ignoreError, boolean ignoreWarning){
			this.ignoreFatal=ignoreFatal;
			this.ignoreError=ignoreError;
			this.ignoreWarning=ignoreWarning;
		}
		
		
		
		public void error(TransformerException te) throws TransformerException {
			if(!ignoreError) throw te;
		}

		
		public void fatalError(TransformerException te) throws TransformerException {
			if(!ignoreFatal) throw te;
		}

		
		public void warning(TransformerException te) throws TransformerException {
			if(!ignoreWarning) throw te;
		}
	}
	
	static class XMLEntityResolverDefaultHandler extends DefaultHandler {

		private InputSource entityRes;

		public XMLEntityResolverDefaultHandler(InputSource entityRes) {
			this.entityRes=entityRes;
		}
		
		
		public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
			//if(entityRes!=null)print.out("resolveEntity("+(entityRes!=null)+"):"+publicID+":"+systemID);
			
			if(entityRes!=null) return entityRes;
			try {
				return new InputSource(new BufferedInputStream(new URL(systemID).openStream()));
			} 
			catch (Exception t) {
				return null;
			}
		}
	}
	static class ThrowingErrorHandler implements ErrorHandler {

		private boolean throwFatalError;
		private boolean throwError;
		private boolean throwWarning;

		public ThrowingErrorHandler(boolean throwFatalError,boolean throwError,boolean throwWarning) {
			this.throwFatalError=throwFatalError;
			this.throwError=throwError;
			this.throwWarning=throwWarning;
		}
		
		
		public void error(SAXParseException e) throws SAXException {
			if(throwError)throw new SAXException(e);
		}

		
		public void fatalError(SAXParseException e) throws SAXException {
			if(throwFatalError)throw new SAXException(e);
		}

		
		public void warning(SAXParseException e) throws SAXException {
			if(throwWarning)throw new SAXException(e);
		}
	}
}