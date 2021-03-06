/* Copyright 2010 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.plugin.membrane_client.creator;

import groovy.xml.MarkupBuilder;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import junit.framework.TestCase;

import com.predic8.schema.Schema;
import com.predic8.schema.SchemaParser;
import com.predic8.wstool.creator.RequestCreator;
import com.predic8.wstool.creator.RequestCreatorContext;
import com.predic8.xml.util.ClasspathResolver;
import com.predic8.xml.util.ResourceResolver;

public class RequestCreatorTest extends TestCase {

	private Schema schema;
	
	private Schema priceListSchema;
	
	protected void setUp() throws Exception {
		ResourceResolver resolver =  new ClasspathResolver();
		
		SchemaParser parser = new SchemaParser();
		parser.setResourceResolver(resolver);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("input", "/person-a.xsd");
		
	    schema = (Schema)parser.parse(params);
	    
	    
	    Map<String, String> params2 = new HashMap<String, String>();
		params2.put("input", "/PriceList.xsd");
		
		priceListSchema = (Schema)parser.parse(params2);
	    
	}
	
	@Test
	public void testEmloyeeList() throws Exception {
		StringWriter strWriter = new StringWriter();
		
		MarkupBuilder markupBuilder = new MarkupBuilder(strWriter);
		RequestCreator creator = new RequestCreator();
		creator.setBuilder(markupBuilder);
		Map<String, String> formParams = new HashMap<String, String>();
	    formParams.put("xpath:/employeeList/employee/firstName", "Kaveh");
	    formParams.put("xpath:/employeeList/employee[1]/firstName", "Shaan");
	    formParams.put("xpath:/employeeList/employee[2]/firstName", "Malkhaz");
	    RequestCreatorContext ctx = new RequestCreatorContext();
	    ctx.setFormParams(formParams);
		schema.getElement("employeeList").create(creator, ctx);
	    System.out.println(strWriter);
	}
	
	@Test
	public void testPriceList() throws Exception {
		StringWriter strWriter = new StringWriter();
		
		MarkupBuilder markupBuilder = new MarkupBuilder(strWriter);
		RequestCreator creator = new RequestCreator();
		creator.setBuilder(markupBuilder);
		Map<String, String> formParams = new HashMap<String, String>();
		
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Header/TransControlID", "TCID-98789");
		
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo/RefIDQual", "VP");
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo[1]/RefIDQual", "BP");
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo[2]/RefIDQual", "MG");
		
		formParams.put("xpath:/PriceListRequest/RequestPriceList/LineInfo/ReturnAvailability", "Y");
		formParams.put("xpath:/PriceListRequest/RequestPriceList/LineInfo[1]/ReturnAvailability", "Y");
		formParams.put("xpath:/PriceListRequest/RequestPriceList/LineInfo[2]/ReturnAvailability", "Y");
		
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo/RefID", "RID-111");
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo[1]/RefID", "RID-222");
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo[2]/RefID", "RID-333");
		
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo/AssignedID", "AsID-111");
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo[1]/AssignedID", "AsID-222");
		formParams.put("xpath:/PriceListRequest/RequestPriceList/Detail/LineInfo[2]/AssignedID", "AsID-333");
		
	    RequestCreatorContext ctx = new RequestCreatorContext();
	    ctx.setFormParams(formParams);
		priceListSchema.getElement("PriceListRequest").create(creator, ctx);
	    System.out.println(strWriter);
	}
	
	
	
}
