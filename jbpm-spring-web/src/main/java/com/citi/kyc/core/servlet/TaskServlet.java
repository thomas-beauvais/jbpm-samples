package com.citi.kyc.core.servlet;


import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.TaskService;
import org.jbpm.task.User;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tbeauvais
 * Date: 6/8/12
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class TaskServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(req.getSession().getServletContext());

//        final KnowledgeBase knowledgeBase = applicationContext.getBean(KnowledgeBase.class);
//        final StatefulKnowledgeSession knowledgeSession = knowledgeBase.newStatefulKnowledgeSession();

        final StatefulKnowledgeSession knowledgeSession = applicationContext.getBean("jbpmSession", StatefulKnowledgeSession.class);

        final TaskService taskService = applicationContext.getBean(TaskService.class);
        setupTaskService(applicationContext);

        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(taskService, knowledgeSession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        knowledgeSession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);

        // start a new process instance
        Map<String, Object> inputParams = new HashMap<String, Object>();
        inputParams.put("decision", "search");
        inputParams.put("ssn", "123456789");
        inputParams.put("lastName", "Smith");
        inputParams.put("firstName", "John");
        inputParams.put("userId", "maker1");

//        ProcessInstance task = knowledgeSession.startProcess("com.citi.kyc.addNewClient", inputParams);

//        Map<String, Object> results = new HashMap<String, Object>();
//        results.put("decision", "nameScreening");

//        completeTask(taskService, task.getId(), "makerAdmin", results );

        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("makerAdmin", "en-UK");
        TaskSummary task = list.get(0);
        taskService.start(task.getId(), "makerAdmin");
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("decision", "nameScreening");

        completeTask(taskService, task.getId(), "makerAdmin", results);

        knowledgeSession.dispose();
    }

    private void setupTaskService(ApplicationContext applicationContext) {
        org.jbpm.task.service.TaskService taskServiceImpl = applicationContext.getBean("taskService", org.jbpm.task.service.TaskService.class);

        TaskServiceSession taskSession = taskServiceImpl.createSession();

        taskSession.addUser(new User("Administrator"));
        taskSession.addUser(new User("makerAdmin"));
        taskSession.addUser(new User("nameScreenerAdmin"));
        taskSession.dispose();
    }

    private void completeTask(TaskService taskService, long taskId, String userId, Map<String, Object> results) {
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

//    private static StatefulKnowledgeSession newStatefulKnowledgeSession(KnowledgeBase kbase) {
//        StatefulKnowledgeSession newKsession;
//        if( useSpring ) {
//            newKsession = (StatefulKnowledgeSession) appContextFactory.getBean("jbpmKsession");
//        }
//        else {
//            Environment env = EnvironmentFactory.newEnvironment();
//            env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
//            env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
//            newKsession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
//        }
//
//        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(taskService, newKsession);
//        humanTaskHandler.setLocal(true);
//        humanTaskHandler.connect();
//        newKsession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);
//
//        logger = KnowledgeRuntimeLoggerFactory.newFileLogger(newKsession, "audit");
//        return newKsession;
//    }
//
//    private static KnowledgeBase readKnowledgeBase() throws Exception {
//        KnowledgeBase kbase;
//        if( useSpring ) {
//            kbase = (KnowledgeBase) appContextFactory.getBean("jbpmKbase");
//        } else {
//            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//            kbuilder.add(ResourceFactory.newClassPathResource("newClient2.bpmn"), ResourceType.BPMN2);
//            kbase =  kbuilder.newKnowledgeBase();
//        }
//        return kbase;
//    }

//    private static void startUp() {
//        startH2Server();
//        setupDataSource();
//        if( useSpring ) {
//            appContextFactory = (BeanFactory) new ClassPathXmlApplicationContext( new String [] {"spring-context.xml"});
//        }
//        if( ! useSpring ) {
//            emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.META-INF");
//        }
//        createTaskService();
//    }
//
//    private void dispose() {
//        if( emf != null && emf.isOpen() ) {
//            emf.close();
//        }
//        if( pds != null ) {
//            pds.close();
//        }
//        server.stop();
//    }
//
//    private static void startH2Server() {
//        try {
//            // start h2 in memory database
//            server = Server.createTcpServer(new String[0]);
//            server.start();
//        } catch (Throwable t) {
//            throw new RuntimeException("Could not start H2 server", t);
//        }
//    }
//
//    public static PoolingDataSource setupDataSource() {
//        pds = new PoolingDataSource();
//        pds.setUniqueName("jdbc/jbpm-ds");
//        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
//        pds.setMaxPoolSize(5);
//        pds.setAllowLocalTransactions(true);
//        pds.getDriverProperties().put("user", "sa");
//        pds.getDriverProperties().put("password","");
//        pds.getDriverProperties().put("url", "jdbc:h2:tcp://localhost/jbpm-task");
//        pds.getDriverProperties().put("driverClassName", "org.h2.Driver");
//        pds.init();
//        return pds;
//    }
//
//    private static void createTaskService() {
//        org.jbpm.task.service.TaskService taskServiceImpl;
//        if( useSpring ) {
//            taskServiceImpl = (org.jbpm.task.service.TaskService) appContextFactory.getBean("taskService");
//        }
//        else {
//            taskServiceImpl = new org.jbpm.task.service.TaskService(
//                    emf, SystemEventListenerFactory.getSystemEventListener());
//        }
//
//        TaskServiceSession taskSession = taskServiceImpl.createSession();
//        taskSession.addUser(new User("Administrator"));
//        taskSession.addUser(new User("makerAdmin"));
//        taskSession.addUser(new User("nameScreenerAdmin"));
//        taskSession.dispose();


}
