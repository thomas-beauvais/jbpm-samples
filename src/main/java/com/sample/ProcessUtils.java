package com.sample;

import java.io.IOException;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.ruleflow.core.RuleFlowProcess;

public class ProcessUtils {
	public static StatefulKnowledgeSession serializeProcess(KnowledgeBuilder kbuilder, RuleFlowProcess process) throws IOException {
        // Serialize the process
        final Resource xmlFileBytes = ResourceFactory.newByteArrayResource(XmlBPMNProcessDumper.INSTANCE.dump(process).getBytes());
		kbuilder.add( xmlFileBytes, ResourceType.BPMN2);

		// Create a new stateful session
		return kbuilder.newKnowledgeBase().newStatefulKnowledgeSession();

		// Print the BPM2 XML
//        final Reader reader = xmlFileBytes.getReader();
//        char ch;
//        while ( ( ch = (char) reader.read()) > -1 ) {
//            System.out.print(ch);
//        }
	}
}
