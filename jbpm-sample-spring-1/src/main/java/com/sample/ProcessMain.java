package com.sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.h2.tools.Server;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.TaskService;
import org.jbpm.task.User;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * This is a sample file to launch a process.
 */
public class ProcessMain {
	
	private static final boolean useSpring = true;
	private static BeanFactory appContextFactory;
	
	private static Server server;
	private static PoolingDataSource pds;
	private static EntityManagerFactory emf;
	private static TaskService taskService;
	private static KnowledgeRuntimeLogger logger;

	
	public static final void main(String[] args) throws Exception {
		startUp();

		// load up the knowledge base
		KnowledgeBase kbase = readKnowledgeBase();
		StatefulKnowledgeSession ksession = newStatefulKnowledgeSession(kbase);
		// start a new process instance
		Map<String, Object> params = new HashMap<String, Object>();	
		params.put("decision", "search");
		params.put("ssn", "123456789");
		params.put("lastName", "Smith");
		params.put("firstName", "John");
		params.put("userId", "maker1");
		ksession.startProcess("com.citi.kyc.addNewClient", params);
		
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("makerAdmin", "en-UK");
        TaskSummary task = list.get(0);
        taskService.start(task.getId(), "makerAdmin");
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("decision", "nameScreening");
        completeTask(task.getId(), "makerAdmin", results);
        
        logger.close();
        ksession.dispose();
        dispose();
	}
	
	private static void completeTask(long taskId, String userId, Map<String, Object> results) {
		ContentData contentData = null;
        if (results != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(results);
                out.close();
                contentData = new ContentData();
                contentData.setContent(bos.toByteArray());
                contentData.setAccessType(AccessType.Inline);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        taskService.complete(taskId, userId, contentData);
	}
	
	private static StatefulKnowledgeSession newStatefulKnowledgeSession(KnowledgeBase kbase) {
	    StatefulKnowledgeSession newKsession;
	    if( useSpring ) { 
	        newKsession = (StatefulKnowledgeSession) appContextFactory.getBean("jbpmKsession");
	    }
	    else { 
	        Environment env = EnvironmentFactory.newEnvironment();
	        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
	        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
	        newKsession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
	    }
	        
	    SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(taskService, newKsession);
	    humanTaskHandler.setLocal(true);
	    humanTaskHandler.connect();
	    newKsession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);
	    
	    logger = KnowledgeRuntimeLoggerFactory.newFileLogger(newKsession, "audit");
	    return newKsession;
	}
	
	private static KnowledgeBase readKnowledgeBase() throws Exception {
	    KnowledgeBase kbase;
	    if( useSpring ) { 
	        kbase = (KnowledgeBase) appContextFactory.getBean("jbpmKbase");
	    } else { 
	        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
	        kbuilder.add(ResourceFactory.newClassPathResource("newClient2.bpmn"), ResourceType.BPMN2);
	        kbase =  kbuilder.newKnowledgeBase();
	    }
	    return kbase;
	}
	
	private static void startUp() {
		startH2Server();
		setupDataSource();
		if( useSpring ) { 
		    appContextFactory = (BeanFactory) new ClassPathXmlApplicationContext( new String [] {"spring-context.xml"});
		}
		if( ! useSpring ) { 
		    emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
		}
		createTaskService();
	}
	
	private static void dispose() {
	    if( emf != null && emf.isOpen() ) { 
	        emf.close();
	    }
	    if( pds != null ) { 
	        pds.close();
	    }
		server.stop();
	}
	
	private static void startH2Server() {
		try {
			// start h2 in memory database
			server = Server.createTcpServer(new String[0]);
	        server.start();
		} catch (Throwable t) {
			throw new RuntimeException("Could not start H2 server", t);
		}
	}
	
	public static PoolingDataSource setupDataSource() {
		pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/jbpm-ds");
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", "sa");
        pds.getDriverProperties().put("password","");
        pds.getDriverProperties().put("url", "jdbc:h2:tcp://localhost/jbpm-task");
        pds.getDriverProperties().put("driverClassName", "org.h2.Driver");
        pds.init();
        return pds;
	}
	
	private static void createTaskService() {
		org.jbpm.task.service.TaskService taskServiceImpl;
		if( useSpring ) { 
		    taskServiceImpl = (org.jbpm.task.service.TaskService) appContextFactory.getBean("taskService");
		}
		else { 
		    taskServiceImpl = new org.jbpm.task.service.TaskService(
		            emf, SystemEventListenerFactory.getSystemEventListener());
		}
		
		TaskServiceSession taskSession = taskServiceImpl.createSession();
		taskSession.addUser(new User("Administrator"));
		taskSession.addUser(new User("makerAdmin"));
		taskSession.addUser(new User("nameScreenerAdmin"));
		taskSession.dispose();
		
		taskService = new LocalTaskService(taskServiceImpl);
	}

}