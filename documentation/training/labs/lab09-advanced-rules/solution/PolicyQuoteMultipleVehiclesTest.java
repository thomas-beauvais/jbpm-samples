package com.sample;


import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.acme.insurance.Driver;
import org.acme.insurance.Policy;
import org.acme.insurance.Rejection;

/**
 * This is a sample class to launch a rule.
 */


public class PolicyQuoteMultipleVehiclesTest {

	static KnowledgeBase kbase;
	static StatefulKnowledgeSession ksession;
	static KnowledgeRuntimeLogger logger;

	
	@BeforeClass
	public static void setupKsession() {
		try {
			// load up the knowledge base
			kbase = readKnowledgeBase();
			ksession = kbase.newStatefulKnowledgeSession();
			logger = KnowledgeRuntimeLoggerFactory.newThreadedFileLogger(ksession, "log/policyQuote", 500);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@AfterClass
	public static void closeKsession() {
		try {
			// load up the knowledge base
			logger.close();
			ksession.dispose();

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	
	@Test
	public void multipleVehiclesTest() {
		
		try {
			//now create some test data
			Driver driver= new Driver();
			driver.setAge(20);
			driver.setCreditScore(500);
			driver.setNumberOfAccidents(0);
			driver.setNumberOfTickets(0);
			
			Policy policy = new Policy();
			policy.setPolicyType("AUTO");
			policy.setVehicleYear(2005);
			policy.setDriver(driver);
			
			Policy policy2 = new Policy();
			policy2.setPolicyType("AUTO");
			policy2.setVehicleYear(2005);
			policy2.setDriver(driver);
			
			Policy policyM = new Policy();
			policyM.setPolicyType("MASTER");
			policyM.setDriver(driver);

			
			// insert objects into working memory
			FactHandle driverFH = ksession.insert(driver);
			FactHandle policyFH = ksession.insert(policy);
			FactHandle policy2FH = ksession.insert(policy2);
			FactHandle policyMFH = ksession.insert(policyM);
			ksession.fireAllRules();
			//logger.close();
			ksession.retract(driverFH);
			ksession.retract(policyFH);
			ksession.retract(policy2FH);
			ksession.retract(policyMFH);
			
			assertEquals("Price is 900", 900, policyM.getPrice());
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	

	
	
	private static KnowledgeBase readKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("policyquote.package"), ResourceType.DRL);
		hasErrors(kbuilder);
		kbuilder.add(ResourceFactory.newClassPathResource("riskyadults.drl"), ResourceType.DRL);
		hasErrors(kbuilder);
		kbuilder.add(ResourceFactory.newClassPathResource("safeadults.drl"), ResourceType.DRL);
		hasErrors(kbuilder);
		kbuilder.add(ResourceFactory.newClassPathResource("riskyyouths.drl"), ResourceType.DRL);
		hasErrors(kbuilder);
		kbuilder.add(ResourceFactory.newClassPathResource("safeyouths.drl"), ResourceType.DRL);
		hasErrors(kbuilder);
		kbuilder.add(ResourceFactory.newClassPathResource("PriceMultipleVehicles.drl"), ResourceType.DRL);
		hasErrors(kbuilder);
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		hasErrors(kbuilder);
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

		return kbase;
	}
		
	private static void hasErrors(KnowledgeBuilder kbuilder) throws Exception {
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error: errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		
	}


}