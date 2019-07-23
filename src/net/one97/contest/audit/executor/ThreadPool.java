package net.one97.contest.audit.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

	private static ExecutorService  executor= Executors.newFixedThreadPool(10);

	
	public void execute(Runnable job){
		executor.execute(job);
	}
	  
	
}
