package com.sample;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.process.Connection;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.test.JbpmJUnitTestCase.TestWorkItemHandler;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.HumanTaskNode;

/**
 * This is a sample that shows how to modify a process that is running.  So, you can dynamically add/remove nodes
 * while the process instance is executing.
 */
public class DynamicProcess {

	private static final String processId = "org.jbpm.DynamicProcess";
	
    public static final void main(String[] args) throws Exception {
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess(processId);

        // Creating the initial process instance
        factory
         // Header
            .name("HelloWorldProcess")
            .version("1.0")
            .packageName("org.jbpm")
         // Nodes
            .startNode(1).name("Start").done()
            .humanTaskNode(2).name("Task1").actorId("krisv").taskName("MyTask").done()
            .endNode(3).name("End").done()
         // Connections
            .connection(1, 2)
            .connection(2, 3);

        RuleFlowProcess process     = factory.validate().getProcess();
        KnowledgeBuilder kbuilder   = KnowledgeBuilderFactory.newKnowledgeBuilder();

        StatefulKnowledgeSession ksession 	= ProcessUtils.serializeProcess(kbuilder, process);
        
        TestWorkItemHandler testHandler     = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);

        KnowledgeRuntimeLogger logger       = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "audit");

        // Down casting?  Really really bad..
        ProcessInstanceImpl processInstance = (ProcessInstanceImpl) ksession.startProcess(processId);

        // Inserting a new HumanTaskNode inbetween t
        HumanTaskNode node = new HumanTaskNode();
        node.setName("Task2");
        insertNodeInBetween(process, 2, 3, node);
        processInstance.setProcess(process);

        ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);

        ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);

        logger.close();
    }

    private static void insertNodeInBetween(RuleFlowProcess process, long startNodeId, long endNodeId, NodeImpl node) {
        if (process == null) {
            throw new IllegalArgumentException("Process may not be null");
        }

        NodeImpl selectedNode = (NodeImpl) process.getNode(startNodeId);
        if (selectedNode == null) {
            throw new IllegalArgumentException("Node " + startNodeId + " not found in process " + process.getId());
        }

        // Walk through the connections of the process until finding the endNodeId
        // Add the connection inbetween the startNode and endNode
        for (Connection connection: selectedNode.getDefaultOutgoingConnections()) {
            if (connection.getTo().getId() == endNodeId) {
                process.addNode(node);
                NodeImpl endNode = (NodeImpl) connection.getTo();
                ((ConnectionImpl) connection).terminate();
                new ConnectionImpl(selectedNode, NodeImpl.CONNECTION_DEFAULT_TYPE, node, NodeImpl.CONNECTION_DEFAULT_TYPE);
                new ConnectionImpl(node, NodeImpl.CONNECTION_DEFAULT_TYPE, endNode, NodeImpl.CONNECTION_DEFAULT_TYPE);
                return;
            }
        }

        throw new IllegalArgumentException("Connection to node " + endNodeId + " not found in process " + process.getId());
    }

}