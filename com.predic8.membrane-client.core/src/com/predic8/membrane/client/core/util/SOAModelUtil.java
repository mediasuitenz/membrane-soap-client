package com.predic8.membrane.client.core.util;

import groovy.xml.MarkupBuilder;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import com.predic8.membrane.client.core.SOAPConstants;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wsdl.WSDLParserContext;
import com.predic8.wsdl.soap11.SOAPBinding;
import com.predic8.wsdl.soap11.SOAPOperation;
import com.predic8.wstool.creator.RequestCreator;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;

public class SOAModelUtil {

	public static String getRequestTemplate(BindingOperation bOperation) {
		SOARequestCreator creator = new SOARequestCreator();
		
		StringWriter stringWriter = new StringWriter();
		creator.setBuilder(new MarkupBuilder(stringWriter));
		creator.setDefinitions(bOperation.getDefinitions());
		creator.setCreator(new RequestTemplateCreator());
		
		try {
			creator.createRequest(getPortTypeName(bOperation), bOperation.getName(), bOperation.getBinding().getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return stringWriter.toString();
	}

	public static String getSOARequest(BindingOperation bOperation, Map<String, String> result) {
		Set<String> keys = result.keySet();
		for (String key : keys) {
			System.out.println(key + ": " + result.get(key));
		}
		
		SOARequestCreator creator = new SOARequestCreator();
		StringWriter stringWriter = new StringWriter();
		creator.setBuilder(new MarkupBuilder(stringWriter));
		creator.setDefinitions(bOperation.getDefinitions());
		creator.setFormParams(result);
		creator.setCreator(new RequestCreator());
		
		try {
			creator.createRequest(SOAModelUtil.getPortTypeName(bOperation), bOperation.getName(), bOperation.getBinding().getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return stringWriter.toString();
	}
	
	
	public static String getPortTypeName(BindingOperation bOperation) {
		return bOperation.getBinding().getPortType().getName();
	}

	public static Definitions getDefinitions(String url) {
		WSDLParserContext ctx = new WSDLParserContext();
		ctx.setInput(url);
		return new WSDLParser().parse(ctx);
	}
	
	public static Definitions getDefinitions(File file) {
		WSDLParserContext ctx = new WSDLParserContext();
		ctx.setInput(file);
		return new WSDLParser().parse(ctx);
	}

	public static String getHost(String url) {
		if ("".equals(url))
			return url;
		
		if (url.startsWith("http://"))
			url = url.substring(7);
		else if (url.startsWith("https://"))
			url = url.substring(8);
		
		String[] tiles = url.split("/");
		
		return tiles[0];
	}
	
	public static String getPathAndQueryString(String dest) throws MalformedURLException {
		URL url = new URL(dest);
		
		String uri = url.getPath();
		if (url.getQuery() != null) {
			return uri + "?" + url.getQuery();
		}
		return uri;
	}
	
	public static String getContentTypeFor(String soapVersion) {
		if (SOAPConstants.SOAP_VERSION_11.equals(soapVersion))
			return "text/xml";
		if (SOAPConstants.SOAP_VERSION_12.equals(soapVersion))
			return "application/soap+xml";
		throw new RuntimeException("Unsupported Soap Version: " + soapVersion);
	}

	public static String getContentTypeFor(BindingOperation bindOp) {
		String soapVersion = getSOAPVersion(bindOp);
		if (SOAPConstants.SOAP_VERSION_11.equals(soapVersion))
			return "text/xml";
		if (SOAPConstants.SOAP_VERSION_12.equals(soapVersion))
			return "application/soap+xml";
		throw new RuntimeException("Unsupported Soap Version: " + soapVersion);
	}
	
	public static String getSOAPVersion(BindingOperation bindOp) {
		Object o = bindOp.getBinding().getBinding();
		if (o instanceof  SOAPBinding) {
			return SOAPConstants.SOAP_VERSION_11;
		} else if (o instanceof  com.predic8.wsdl.soap12.SOAPBinding) {
			return SOAPConstants.SOAP_VERSION_12;
		}
		throw new RuntimeException("Unsupported SOAP version.");
	}
	
	public static String getSoapAction(BindingOperation bindOp) {
		Object sOp = bindOp.getOperation();
		if (sOp instanceof SOAPOperation) {
			if (((SOAPOperation)sOp).getSoapAction() == null)
				return "";
			
			return ((SOAPOperation)sOp).getSoapAction().toString();
		} 
		
		return ((com.predic8.wsdl.soap12.SOAPOperation)sOp).getSoapAction().toString();
	}
	
}
