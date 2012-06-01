package com.sample;

import java.io.IOException;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;

/**
 * This is a "Hello World" demo making the entire process from scratch and starting it.
 */
public class HelloWorldProcess {

	private static final String processId = "org.jbpm.HelloWorld";

	public static void main(String[] args) throws IOException {
		RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess( processId );

		factory
        // Header
                .name("HelloWorldProcess")
                .version("1.0")
                .packageName("org.jbpm")
		// Nodes
				.startNode(1).name("Start").done()
				.actionNode(2).name("Action")
				.action("java", "System.out.println(\"Hello World\");").done()
				.endNode(3).name("End").done()
        // Connections
				.connection(1, 2)
				.connection(2, 3);

		RuleFlowProcess process = factory.validate().getProcess();

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

		StatefulKnowledgeSession ksession = ProcessUtils.serializeProcess( kbuilder, process );
		
		ksession.startProcess( processId );
	}

}
