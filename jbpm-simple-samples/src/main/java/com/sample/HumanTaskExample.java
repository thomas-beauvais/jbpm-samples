package com.sample;

import java.io.IOException;

import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.process.workitem.wsht.WSHumanTaskHandler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.TaskClientConnector;
import org.jbpm.task.service.mina.BaseMinaHandler;
import org.jbpm.task.service.mina.MinaTaskClientConnector;
import org.jbpm.task.service.mina.MinaTaskClientHandler;

/**
 * This is a sample file to launch a process.
 */
public class HumanTaskExample {

	private static final String processId = "org.jbpm.HumanTaskExample";

	public static void main(String[] args) throws IOException {
		RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess(processId);

		SystemEventListener systemEventListener 	= SystemEventListenerFactory.getSystemEventListener();
		BaseMinaHandler taskClientHandler 			= new MinaTaskClientHandler( systemEventListener );
		TaskClientConnector taskClientConnector 	= new MinaTaskClientConnector( "JBPM Interface", taskClientHandler );
		TaskClient client 							= new TaskClient( taskClientConnector );
		client.connect("localhost", 9123);
				
		factory
        // Header
                .name("HumanTaskProcess")
                .version("1.0")
                .packageName("org.jbpm")
		// Nodes
				.startNode(1).name("Start").done()
				.humanTaskNode(2).name("HumanTask")
				.actorId( "admin" ).content("content").done()
				.endNode(3).name("End").done()
        // Connections
				.connection(1, 2)
				.connection(2, 3);

		RuleFlowProcess process = factory.validate().getProcess();

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

		StatefulKnowledgeSession ksession = ProcessUtils.serializeProcess( kbuilder, process );
		
//		CommandBasedWSHumanTaskHandler taskHandler = new CommandBasedWSHumanTaskHandler(ksession);
//		taskHandler.setConnection("localhost", 9123);
//		taskHandler.connect();
		
		WSHumanTaskHandler taskHandler = new WSHumanTaskHandler( ksession );
		taskHandler.setClient(client);
		
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", taskHandler );
		
		ksession.startProcess( processId );
	}
}