package com.sample;


import static org.junit.Assert.assertEquals;

import org.acme.insurance.Driver;
import org.acme.insurance.Policy;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
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

/**
 * This is a sample class to launch a rule.
 */


public class PolicyQuoteRulesTest {

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
	public void riskyAdultsTest() {
		
		try {
			//now create some test data
			Driver driver= new Driver();
			driver.setAge(30);
			driver.setCreditScore(500);
			driver.setNumberOfAccidents(1);
			driver.setNumberOfTickets(0);
			
			Policy policy = new Policy();
			policy.setPolicyType("AUTO");
			policy.setVehicleYear(2004);
			
			// insert objects into working memory
			FactHandle driverFH = ksession.insert(driver);
			FactHandle policyFH = ksession.insert(policy);
			ksession.fireAllRules();
			//logger.close();
			ksession.retract(driverFH);
			ksession.retract(policyFH);
			
			assertEquals("Price is 300", 300, policy.getPrice());
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@Test
	public void riskyYouthsTest() {
		
		try {
			//now create some test data
			Driver driver= new Driver();
			driver.setAge(20);
			driver.setCreditScore(500);
			driver.setNumberOfAccidents(1);
			driver.setNumberOfTickets(0);
			
			Policy policy = new Policy();
			policy.setPolicyType("AUTO");
			policy.setVehicleYear(2004);
			
			// insert objects into working memory
			FactHandle driverFH = ksession.insert(driver);
			FactHandle policyFH = ksession.insert(policy);
			ksession.fireAllRules();
			//logger.close();
			ksession.retract(driverFH);
			ksession.retract(policyFH);
			
			assertEquals("Price is 700", 700, policy.getPrice());
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Test
	public void safeAdultsTest() {
		
		try {
			//now create some test data
			Driver driver= new Driver();
			driver.setAge(30);
			driver.setCreditScore(500);
			driver.setNumberOfAccidents(0);
			driver.setNumberOfTickets(1);
			
			Policy policy = new Policy();
			policy.setPolicyType("AUTO");
			policy.setVehicleYear(2004);
			
			// insert objects into working memory
			FactHandle driverFH = ksession.insert(driver);
			FactHandle policyFH = ksession.insert(policy);
			ksession.fireAllRules();
			//logger.close();
			ksession.retract(driverFH);
			ksession.retract(policyFH);
			
			assertEquals("Price is 120", 120, policy.getPrice());
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@Test
	public void safeYouthsTest() {
		
		try {
			//now create some test data
			Driver driver= new Driver();
			driver.setAge(20);
			driver.setCreditScore(500);
			driver.setNumberOfAccidents(0);
			driver.setNumberOfTickets(0);
			
			Policy policy = new Policy();
			policy.setPolicyType("AUTO");
			policy.setVehicleYear(2004);
			
			// insert objects into working memory
			FactHandle driverFH = ksession.insert(driver);
			FactHandle policyFH = ksession.insert(policy);
			ksession.fireAllRules();
			//logger.close();
			ksession.retract(driverFH);
			ksession.retract(policyFH);
			
			assertEquals("Price is 450", 450, policy.getPrice());
			
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