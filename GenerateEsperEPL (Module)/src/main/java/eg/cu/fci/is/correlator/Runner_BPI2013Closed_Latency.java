package eg.cu.fci.is.correlator;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
//import com.espertech.esper.compiler.client.*;
//import com.espertech.esper.client;
import eg.cu.fci.is.correlator.events.*;
import eg.cu.fci.is.correlator.source.UnlabeledEventSource;
//import eg.cu.fci.is.correlator.source.UnlabeledEventSource_MapEvent1;

public class Runner_BPI2013Closed_Latency {

	private static long initTime =0;
	
    public static void main(String[] args) throws InterruptedException, IOException {
    	//ignored rule: (accepted # queued) -> accepted

    	System.out.println("***** APP START *****");
    	
    	String logName = "BPI2013Closed";
    	String logFailed = "_fail";
    	String logSuffix = "_latency_mem_run";
    	String logProbSuffix = "_prob";
    	double logProb = 0.3;
    	int logRun = 5;
    	
        String filePath = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation\\"+logName+"_unLabeled.csv";
    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\BPI2013-closed\\BPI2013Closed_latency_mem2\\"+logName+logProbSuffix+logProb+logSuffix+logRun+".csv";
    	String failedEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\BPI2013-closed\\BPI2013Closed_latency_mem2\\"+logName+logProbSuffix+logProb+logSuffix+logRun+logFailed+".csv";
        
    	FileWriter frLabeled = new FileWriter(labeledEvents,true);
        long countLines  = Files.lines(Paths.get(labeledEvents), Charset.defaultCharset()).count(); 
        if(countLines == 0)
        {
        	frLabeled.append("CaseID,Activity,Timestamp,Probability,initTime,sysTime\n");
        	frLabeled.flush(); 
        }
        FileWriter frFailed = new FileWriter(failedEvents,true);
        long countLinesFail  = Files.lines(Paths.get(failedEvents), Charset.defaultCharset()).count(); 
        if(countLinesFail == 0)
        {
        	frFailed.append("Activity,Timestamp,initTime,sysTime\n");
        	frFailed.flush(); 
        }

        EPCompiler compiler = EPCompilerProvider.getCompiler();

        Configuration configuration = new Configuration();
        configuration.getRuntime().getExecution().setPrioritized(true);

        // Here you register your event types
        configuration.getCommon().addEventType(Cases.class);        
        configuration.getCommon().addEventType(UnlabeledEvent.class);
        configuration.getCommon().addEventType(FilterLabeledEvent.class);
        configuration.getCommon().addEventType(LabeledEvent.class);
        configuration.getCommon().addEventType(TempEvent.class);
        configuration.getCommon().addEventType(FailCorrelation.class);

        String epl_Unlabeled =" @Name('UnlabeledEvent') select * from UnlabeledEvent; ";

        String epl_FilterLabeledEvent ="  @Name('FilterLabeledEvent') select * from FilterLabeledEvent; ";

        String epl_Labeled =" @Name('Labeled') select * from LabeledEvent; ";

        String epl_Temp =" @Name('Temp') select * from TempEvent ; ";  
        
        String epl_FailCorrelation =" @Name('FailCorrelation') select * from FailCorrelation; ";

        String epl_get_case_ID =" @Name('get_case_ID')  insert into Cases  select (select coalesce(max(caseID)+1, 1)  from  Cases )  as caseID from UnlabeledEvent  where  activity in ('accepted');  ";

        String epl_start_case_accepted ="@Name('start_case_accepted')  insert into FilterLabeledEvent (caseID, activity, timestamp, probability, initTime)  select (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0, UE.sysTime as initTime  from UnlabeledEvent as UE where UE.activity = 'accepted'; ";

        String epl_TE_accepted =" @Priority(5) @Name('TE_accepted') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'accepted' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='accepted' or activity='queued)') -> every succ=UnlabeledEvent(activity='accepted')) where timer:within(86399 sec)]#time(86399 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=86398000;";

        String epl_TE_queued =" @Priority(5) @Name('TE_queued') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'queued' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'accepted')) -> every succ=UnlabeledEvent(activity='queued') ]#time(86078 sec)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86078000;";

        String epl_TE_unmatched =" @Priority(5) @Name('TE_unmatched') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'unmatched' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'accepted')) -> every succ=UnlabeledEvent(activity='unmatched') ]#time(75866 sec)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=75866000;";

        String epl_TE_completed =" @Priority(5) @Name('TE_completed') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'completed' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='accepted' or activity='unmatched')) -> every succ=UnlabeledEvent(activity='completed') ]#time(86396 sec)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86396000;";

        String epl_Correlate_accepted =" @Priority(20) @Name('Correlate_accepted') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability  from TempEvent.win:time_batch(1 sec) as T  where activity = 'accepted'  group by caseID order by timestamp ; ";//desc limit 5 

        String epl_Correlate_queued =" @Priority(20) @Name('Correlate_queued') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct T.caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability  from TempEvent.win:time_batch(1 sec) as T  where activity = 'queued'  group by caseID order by timestamp; ";

        String epl_Correlate_unmatched =" @Priority(20) @Name('Correlate_unmatched') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct T.caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability  from TempEvent.win:time_batch(1 sec) as T  where activity = 'unmatched'  group by caseID order by timestamp; ";

        String epl_Correlate_completed =" @Priority(20) @Name('Correlate_completed') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct T.caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability  from TempEvent.win:time_batch(1 sec) as T  where activity = 'completed'  group by caseID order by timestamp; ";
        /*//old calculations
        String epl_Correlate_queued =" @Priority(20) @Name('Correlate_queued') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'queued'  group by caseID order by timestamp; ";

        String epl_Correlate_unmatched =" @Priority(20) @Name('Correlate_unmatched') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'unmatched'  group by caseID order by timestamp; ";

        String epl_Correlate_completed =" @Priority(20) @Name('Correlate_completed') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'completed'  group by caseID order by timestamp; ";
		//*/
        String epl_Filter_accepted =" @Priority(50) @Name('Filter_accepted') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'accepted'  and probability >= " + logProb + " group by caseID order by probability desc limit 1 ;";

        String epl_Filter_queued =" @Priority(50) @Name('Filter_queued') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from LabeledEvent.win:time(1 sec) as T where activity = 'queued'  and probability >= " + logProb + " group by caseID order by probability desc limit 1 ;";

        String epl_Filter_unmatched =" @Priority(50) @Name('Filter_unmatched') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'unmatched'  and probability >= " + logProb + " group by caseID order by probability desc limit 1 ;";

        String epl_Filter_completed =" @Priority(50) @Name('Filter_completed') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'completed'  and probability >= " + logProb + " group by caseID order by probability desc limit 1 ;";

//        String epl_Fail = "@Priority(0) @Name('Fail') insert into FailCorrelation (activity , timestamp , initTime) select ue.activity, ue.timestamp,  "+initTime+" from pattern [ ue=UnlabeledEvent and not fe=FilterLabeledEvent(activity=ue.activity and timestamp=ue.timestamp) ].win:time_batch(1 sec)";
        String epl_Fail = " @Priority(0) @Name('Fail') insert into FailCorrelation (activity , timestamp , initTime) select ue.activity, ue.timestamp,  "+initTime+" from pattern [ ue=UnlabeledEvent -> (timer:interval(86399 sec) and not fe=FilterLabeledEvent(activity=ue.activity and timestamp=ue.timestamp)) ]";
        
        // Get a runtime
        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
        EPDeploymentService  deploymentService = runtime.getDeploymentService();

        CompilerArguments arguments = new CompilerArguments(configuration);
        arguments.getPath().add(runtime.getRuntimePath());

        EPCompiled epCompiled_Unlabeled;
        EPDeployment deployment_Unlabeled;

        EPCompiled epCompiled_FilterLabeledEvent;
        EPDeployment deployment_FilterLabeledEvent;

        EPCompiled epCompiled_Labeled;
        EPDeployment deployment_Labeled;

        EPCompiled epCompiled_Temp;
        EPDeployment deployment_Temp;

        EPCompiled epCompiled_get_case_ID;
        EPDeployment deployment_get_case_ID;

        EPCompiled epCompiled_start_case_accepted;
        EPDeployment deployment_start_case_accepted;

        EPCompiled epCompiled_TE_accepted;
        EPDeployment deployment_TE_accepted;
        
        EPCompiled epCompiled_TE_queued;
        EPDeployment deployment_TE_queued;

        EPCompiled epCompiled_TE_unmatched;
        EPDeployment deployment_TE_unmatched;

        EPCompiled epCompiled_TE_completed;
        EPDeployment deployment_TE_completed;

        EPCompiled epCompiled_Correlate_accepted;
        EPDeployment deployment_Correlate_accepted;

        EPCompiled epCompiled_Correlate_queued;
        EPDeployment deployment_Correlate_queued;

        EPCompiled epCompiled_Correlate_unmatched;
        EPDeployment deployment_Correlate_unmatched;

        EPCompiled epCompiled_Correlate_completed;
        EPDeployment deployment_Correlate_completed;

        EPCompiled epCompiled_Filter_accepted;
        EPDeployment deployment_Filter_accepted;

        EPCompiled epCompiled_Filter_queued;
        EPDeployment deployment_Filter_queued;

        EPCompiled epCompiled_Filter_unmatched;
        EPDeployment deployment_Filter_unmatched;

        EPCompiled epCompiled_Filter_completed;
        EPDeployment deployment_Filter_completed;

        EPCompiled epCompiled_FailCorrelation;
        EPDeployment deployment_FailCorrelation;

        EPCompiled epCompiled_Fail;
        EPDeployment deployment_Fail;
        
        try {
            epCompiled_Unlabeled = compiler.compile(epl_Unlabeled, arguments);
            deployment_Unlabeled = deploymentService.deploy(epCompiled_Unlabeled);

            epCompiled_FilterLabeledEvent = compiler.compile(epl_FilterLabeledEvent, arguments);
            deployment_FilterLabeledEvent = deploymentService.deploy(epCompiled_FilterLabeledEvent);

            epCompiled_Labeled = compiler.compile(epl_Labeled, arguments);
            deployment_Labeled = deploymentService.deploy(epCompiled_Labeled);

            epCompiled_Temp = compiler.compile(epl_Temp, arguments);
            deployment_Temp = deploymentService.deploy(epCompiled_Temp);

            epCompiled_get_case_ID = compiler.compile(epl_get_case_ID, arguments);
            deployment_get_case_ID = deploymentService.deploy(epCompiled_get_case_ID);

            epCompiled_start_case_accepted = compiler.compile(epl_start_case_accepted, arguments);
            deployment_start_case_accepted = deploymentService.deploy(epCompiled_start_case_accepted);

            epCompiled_TE_accepted = compiler.compile(epl_TE_accepted, arguments);
            deployment_TE_accepted = deploymentService.deploy(epCompiled_TE_accepted);

            epCompiled_TE_queued = compiler.compile(epl_TE_queued, arguments);
            deployment_TE_queued = deploymentService.deploy(epCompiled_TE_queued);

            epCompiled_TE_unmatched = compiler.compile(epl_TE_unmatched, arguments);
            deployment_TE_unmatched = deploymentService.deploy(epCompiled_TE_unmatched);

            epCompiled_TE_completed = compiler.compile(epl_TE_completed, arguments);
            deployment_TE_completed = deploymentService.deploy(epCompiled_TE_completed);

            CompilerArguments arguments2 = new CompilerArguments(configuration);
            arguments2.getPath().add(runtime.getRuntimePath());

            epCompiled_Correlate_accepted = EPCompilerProvider.getCompiler().compile(epl_Correlate_accepted, arguments2);
            deployment_Correlate_accepted = deploymentService.deploy(epCompiled_Correlate_accepted);

            epCompiled_Correlate_queued = EPCompilerProvider.getCompiler().compile(epl_Correlate_queued, arguments2);
            deployment_Correlate_queued = deploymentService.deploy(epCompiled_Correlate_queued);

            epCompiled_Correlate_unmatched = EPCompilerProvider.getCompiler().compile(epl_Correlate_unmatched, arguments2);
            deployment_Correlate_unmatched = deploymentService.deploy(epCompiled_Correlate_unmatched);

            epCompiled_Correlate_completed = EPCompilerProvider.getCompiler().compile(epl_Correlate_completed, arguments2);
            deployment_Correlate_completed = deploymentService.deploy(epCompiled_Correlate_completed);


            CompilerArguments arguments3 = new CompilerArguments(configuration);
            arguments3.getPath().add(runtime.getRuntimePath());

            epCompiled_Filter_accepted = EPCompilerProvider.getCompiler().compile(epl_Filter_accepted, arguments3);
            deployment_Filter_accepted = deploymentService.deploy(epCompiled_Filter_accepted);

            epCompiled_Filter_queued = EPCompilerProvider.getCompiler().compile(epl_Filter_queued, arguments3);
            deployment_Filter_queued = deploymentService.deploy(epCompiled_Filter_queued);

            epCompiled_Filter_unmatched = EPCompilerProvider.getCompiler().compile(epl_Filter_unmatched, arguments3);
            deployment_Filter_unmatched = deploymentService.deploy(epCompiled_Filter_unmatched);

            epCompiled_Filter_completed = EPCompilerProvider.getCompiler().compile(epl_Filter_completed, arguments3);
            deployment_Filter_completed = deploymentService.deploy(epCompiled_Filter_completed);

//            CompilerArguments arguments4 = new CompilerArguments(configuration);
//            arguments.getPath().add(runtime.getRuntimePath());
            
            epCompiled_FailCorrelation = compiler.compile(epl_FailCorrelation, arguments);
            deployment_FailCorrelation = deploymentService.deploy(epCompiled_FailCorrelation);
            
            epCompiled_Fail = EPCompilerProvider.getCompiler().compile(epl_Fail, arguments);
            deployment_Fail = deploymentService.deploy(epCompiled_Fail);

        } catch (EPCompileException | EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        EPEventService eventService = runtime.getEventService();
        eventService.clockExternal();
        eventService.advanceTime(0);
        System.out.println("System clock "+eventService.getCurrentTime());
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
        Date date = new Date();  
        System.out.println("Start time "+formatter.format(date));
        EPStatement statement_Unlabeled = runtime.getDeploymentService().getStatement(deployment_Unlabeled.getDeploymentId(), "UnlabeledEvent");
        EPStatement statement_FilterLabeledEvent = runtime.getDeploymentService().getStatement(deployment_FilterLabeledEvent.getDeploymentId(), "FilterLabeledEvent");
        EPStatement statement_Labeled = runtime.getDeploymentService().getStatement(deployment_Labeled.getDeploymentId(), "Labeled");
        EPStatement statement_Temp = runtime.getDeploymentService().getStatement(deployment_Temp.getDeploymentId(), "Temp");
        EPStatement statement_FailCorrelation = runtime.getDeploymentService().getStatement(deployment_FailCorrelation.getDeploymentId(), "FailCorrelation");
        
        EPStatement statement_get_case_ID = runtime.getDeploymentService().getStatement(deployment_get_case_ID.getDeploymentId(), "get_case_ID");
        EPStatement statement_start_case_accepted = runtime.getDeploymentService().getStatement(deployment_start_case_accepted.getDeploymentId(), "start_case_accepted");
        EPStatement statement_TE_accepted = runtime.getDeploymentService().getStatement(deployment_TE_accepted.getDeploymentId(), "TE_accepted");
        EPStatement statement_TE_queued = runtime.getDeploymentService().getStatement(deployment_TE_queued.getDeploymentId(), "TE_queued");
        EPStatement statement_TE_unmatched = runtime.getDeploymentService().getStatement(deployment_TE_unmatched.getDeploymentId(), "TE_unmatched");
        EPStatement statement_TE_completed = runtime.getDeploymentService().getStatement(deployment_TE_completed.getDeploymentId(), "TE_completed");

        EPStatement statement_Correlate_accepted = runtime.getDeploymentService().getStatement(deployment_Correlate_accepted.getDeploymentId(), "Correlate_accepted");
        EPStatement statement_Correlate_queued = runtime.getDeploymentService().getStatement(deployment_Correlate_queued.getDeploymentId(), "Correlate_queued");
        EPStatement statement_Correlate_unmatched = runtime.getDeploymentService().getStatement(deployment_Correlate_unmatched.getDeploymentId(), "Correlate_unmatched");
        EPStatement statement_Correlate_completed = runtime.getDeploymentService().getStatement(deployment_Correlate_completed.getDeploymentId(), "Correlate_completed");

        EPStatement statement_Filter_accepted = runtime.getDeploymentService().getStatement(deployment_Filter_accepted.getDeploymentId(), "Filter_accepted");
        EPStatement statement_Filter_queued = runtime.getDeploymentService().getStatement(deployment_Filter_queued.getDeploymentId(), "Filter_queued");
        EPStatement statement_Filter_unmatched = runtime.getDeploymentService().getStatement(deployment_Filter_unmatched.getDeploymentId(), "Filter_unmatched");
        EPStatement statement_Filter_completed = runtime.getDeploymentService().getStatement(deployment_Filter_completed.getDeploymentId(), "Filter_completed");

        EPStatement statement_Fail = runtime.getDeploymentService().getStatement(deployment_Fail.getDeploymentId(), "Fail");

        statement_Unlabeled.addListener((newData, oldData, s, r) -> {
//            String activity = (String) newData[0].get("activity");
//            long timestamp = (long) newData[0].get("timestamp");
//        	initTime = (long) newData[0].get("sysTime");
//            long sysTime = (long) newData[0].get("sysTime");
            initTime = (long) newData[0].get("sysTime");
//            initTime = sysTime;
//            Date ts = new Date(timestamp);
//            Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//            System.out.println("UnlabeledEvent ["+ activity+" , "+format.format(ts)+","+sysTime+"]");//+caseID + " , "
        });


        statement_FilterLabeledEvent.addListener((newData, oldData, s, r) -> {
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            double probability = (double) newData[0].get("probability");
            //initTime = (long) newData[0].get("initTime");
            long sysTime = (long) newData[0].get("sysTime");
            Date ts = new Date(timestamp);
            Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            try {
		    	frLabeled.append(caseID + "," + activity+","+format.format(ts)+","+probability+","+initTime+","+sysTime+"\n");
		    	frLabeled.flush(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
//		    System.out.println("*** FilterLabeledEventEvent ["+caseID + " , " + activity+" , "+format.format(ts)+" , "+probability+" , "+initTime+" , "+sysTime+"]");
        });
//        statement_Labeled.addListener((newData, oldData, s, r) -> {
//            long caseID = (long) newData[0].get("caseID");
//            String activity = (String) newData[0].get("activity");
//            long timestamp = (long) newData[0].get("timestamp");
//            double probability = (double) newData[0].get("probability");
//            Date ts = new Date(timestamp);
//            Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//
////	         try { 
////		    	frLabeled.append(caseID + "," + activity+","+format.format(ts)+","+probability+"\n");
////		    	frLabeled.flush();
////			} catch (IOException e) {
////				e.printStackTrace();
////			}
//		    System.out.println("*** LabeledEvent ["+caseID + " , " + activity+" , "+format.format(ts)+" , "+probability+"]");
//        });

//        statement_Temp.addListener((newData, oldData, s, r) -> {
//            long caseID = (long) newData[0].get("caseID");
//            String activity = (String) newData[0].get("activity");
//            long timestamp = (long) newData[0].get("timestamp");
//            Date ts = new Date(timestamp);
//            Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//            System.out.println("--- TempEvent ["+caseID + " , " + activity+" , "+format.format(ts)+"]");
//        });
        

        statement_FailCorrelation.addListener((newData, oldData, s, r) -> {
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            Date ts = new Date(timestamp);
	        Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            long sysTime = (long) newData[0].get("sysTime");
            try {
		    	frFailed.append(activity+","+format.format(ts)+","+initTime+","+sysTime+"\n");
		    	frFailed.flush(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
        });
/*
        statement_TE_queued.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_queued-instance "+newData[0].getUnderlying()); }); 
        statement_TE_unmatched.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_unmatched-instance "+newData[0].getUnderlying()); }); 
        statement_TE_completed.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_completed-instance "+newData[0].getUnderlying()); }); 

        statement_Correlate_queued.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_queued-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_unmatched.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_unmatched-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_completed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_completed-instance "+newData[0].getUnderlying()); }); 

        statement_Filter_queued.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_queued-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_unmatched.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_unmatched-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_completed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_completed-instance "+newData[0].getUnderlying()); }); 

*/


//        new UnlabeledEventSource_MapEvent1(filePath, 6661, eventService, runtime, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent
        new UnlabeledEventSource(filePath, 6661, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent

        frLabeled.close(); 
        frFailed.close(); 
        date = new Date(); 
        System.out.println("End time "+formatter.format(date));
        Runtime rt = Runtime.getRuntime();
        long memoryMax = rt.maxMemory();
        long memoryUsed = rt.totalMemory() - rt.freeMemory();
        System.out.println("memoryUsed "+memoryUsed/(1024*1024)+" MB");
        System.out.println("memoryMax "+memoryMax/(1024*1024)+" MB");
        double memoryUsedPercent = (memoryUsed * 100.0) / memoryMax;
        System.out.println("memoryUsedPercent: " + memoryUsedPercent);

    }
}