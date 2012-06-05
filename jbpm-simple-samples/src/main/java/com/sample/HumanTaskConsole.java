package com.sample;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;


public class HumanTaskConsole implements WorkItemHandler {
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		// Do some work
		System.out.println("Do some work for the HumanTaskConsole" );
				
		// notify manager that work item has been completed
		manager.completeWorkItem(workItem.getId(), null);
	}
	
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) { 
		
	}
	
}