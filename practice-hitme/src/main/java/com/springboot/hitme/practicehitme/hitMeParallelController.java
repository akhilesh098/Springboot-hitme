package com.springboot.hitme.practicehitme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class hitMeParallelController {

	@Autowired
	private Executor executor;

	@PostMapping("/hitme")
	public HashMap<String, Object> getAllLinks(@RequestBody Req[] req) {
		long startTime = System.currentTimeMillis();
		List<GetRequestTask> tasks = new ArrayList<GetRequestTask>();
		Map<String, Integer> numberMapping = new HashMap<>();
		HashMap<String, Object> map = new HashMap<>();
		Logger logger = LoggerFactory.getLogger(hitMeParallelController.class);
		Integer responseTime = 0;
		
		for(Req r : req) {
			logger.info(r.getUrl());
			logger.info("isParallel: "+r.getIsParallel()+ " Count: "+ r.getCount());
			
			if(!r.getIsParallel()) {
				for(int i=0;i<r.getCount();i++){
					URL url;
					try {
						url = new URL(r.getUrl());
						HttpURLConnection con = (HttpURLConnection) url.openConnection();
						con.setRequestMethod("GET");
						con.setRequestProperty("Content-Type", "application/json");
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuffer content = new StringBuffer();
						while ( (inputLine = in.readLine()) != null) {
							content.append(inputLine);
						}
					    responseTime = responseTime + Integer.parseInt(content.toString());
						in.close();
						con.disconnect();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
			}else {
				for(int i=0;i<r.getCount();i++){
				tasks.add(new GetRequestTask(r.getUrl(), this.executor));
				}
				 Integer parallelReqResponseTime = 0;
				 
				 while(!tasks.isEmpty()) {
					
		            for(Iterator<GetRequestTask> it = tasks.iterator(); it.hasNext();) {
		                GetRequestTask task = it.next();
		                
		                
		                if(task.isDone()) {
		                    String request = task.getRequest();
		                    String response = task.getResponse();

		                    //logger.info(response);
		                    numberMapping.put(r.getUrl(), Integer.parseInt(response));      
		                    
		                    it.remove();
		                }
		            }
		              //avoid tight loop in "main" thread
		            if(!tasks.isEmpty()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		            } 
		        }
				 responseTime = responseTime + numberMapping.get(r.getUrl());
			}	
		}
		
		long duration = System.currentTimeMillis() - startTime;
		map.put("Theoretical_ResponseTime", responseTime);
		map.put("Actual_ResponseTime", duration);
			
		return map;
	}

	    class GetRequestTask{
	    	
		        private GetRequestWork work;
		        private FutureTask<String> task;
		        public GetRequestTask(String url, Executor executor) {
		            this.work = new GetRequestWork(url);
		            this.task = new FutureTask<String>(work);
		            executor.execute(this.task);
		        }
		        public String getRequest() {
		            return this.work.getUrl();
		        }
		        public boolean isDone() {
		            return this.task.isDone();
		        }
		        public String getResponse() {
		            try {
		                return this.task.get();
		            } catch(Exception e) {
		                throw new RuntimeException(e);
		            }
		        }
	    	
	    }
	    
	    class GetRequestWork implements Callable<String> {
	        private final String url;
	        public GetRequestWork(String url) {
	            this.url = url;
	        }
	        public String getUrl() {
	        	
	            return this.url;
	        }
			public String call() throws Exception {
				HttpURLConnection con = (HttpURLConnection) (new URL(this.url).openConnection());
				con.setRequestMethod("GET");
				con.setRequestProperty("Content-Type", "application/json");
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				con.disconnect();
				return content.toString();
	        }
	    }
	
}
