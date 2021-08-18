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

public class Runner_loop1000_1R {
	
	private static long initTime =0;

    public static void main(String[] args) throws InterruptedException, IOException {


    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\loop1000_1R_labeled_top1_latency.csv";
        FileWriter frLabeled = new FileWriter(labeledEvents,true);
        long countLines  = Files.lines(Paths.get(labeledEvents), Charset.defaultCharset()).count(); 
        if(countLines == 0)
        {
        	        	frLabeled.append("CaseID,Activity,Timestamp,Probability,initTime,sysTime\n");
        	frLabeled.flush(); 
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

        String epl_Unlabeled =" @Name('Unlabeled') select * from UnlabeledEvent; ";

        String epl_FilterLabeledEvent ="  @Name('FilterLabeledEvent') select * from FilterLabeledEvent; ";

        String epl_Labeled =" @Name('Labeled') select * from LabeledEvent; ";

        String epl_Temp =" @Name('Temp') select * from TempEvent ; ";

        String epl_get_case_ID =" @Name('get_case_ID')  insert into Cases  select (select coalesce(max(caseID)+1, 1)  from  Cases )  as caseID from UnlabeledEvent  where  activity in ('a');  ";

        String epl_start_case_a =" @Name('start_case_a')  insert into FilterLabeledEvent (caseID, activity, timestamp, probability, initTime)  select (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0, UE.sysTime as initTime  from UnlabeledEvent as UE where UE.activity = 'a'; ";

        String epl_TE_d =" @Priority(5) @Name('TE_d') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'd' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a' or activity='e') -> every succ=UnlabeledEvent(activity='d')) where timer:within(61 sec)]#time(61 sec)  where (succ.timestamp - pred.timestamp) >= 60000 and (succ.timestamp - pred.timestamp) <=60000;";

        String epl_Correlate_d =" @Priority(20) @Name('Correlate_d') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'd'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Filter_d =" @Priority(50) @Name('Filter_d') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'd'  and probability >= 0.0 group by caseID order by probability desc limit 1 ;";

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

        EPCompiled epCompiled_start_case_a;
        EPDeployment deployment_start_case_a;

        EPCompiled epCompiled_TE_d;
        EPDeployment deployment_TE_d;

        EPCompiled epCompiled_Correlate_d;
        EPDeployment deployment_Correlate_d;

        EPCompiled epCompiled_Filter_d;
        EPDeployment deployment_Filter_d;

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

            epCompiled_start_case_a = compiler.compile(epl_start_case_a, arguments);
            deployment_start_case_a = deploymentService.deploy(epCompiled_start_case_a);

            epCompiled_TE_d = compiler.compile(epl_TE_d, arguments);
            deployment_TE_d = deploymentService.deploy(epCompiled_TE_d);

            CompilerArguments arguments2 = new CompilerArguments(configuration);
            arguments2.getPath().add(runtime.getRuntimePath());

            epCompiled_Correlate_d = EPCompilerProvider.getCompiler().compile(epl_Correlate_d, arguments2);
            deployment_Correlate_d = deploymentService.deploy(epCompiled_Correlate_d);


            CompilerArguments arguments3 = new CompilerArguments(configuration);
            arguments3.getPath().add(runtime.getRuntimePath());

            epCompiled_Filter_d = EPCompilerProvider.getCompiler().compile(epl_Filter_d, arguments3);
            deployment_Filter_d = deploymentService.deploy(epCompiled_Filter_d);


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
        EPStatement statement_Unlabeled = runtime.getDeploymentService().getStatement(deployment_Unlabeled.getDeploymentId(), "Unlabeled");
        EPStatement statement_FilterLabeledEvent = runtime.getDeploymentService().getStatement(deployment_FilterLabeledEvent.getDeploymentId(), "FilterLabeledEvent");
        EPStatement statement_Labeled = runtime.getDeploymentService().getStatement(deployment_Labeled.getDeploymentId(), "Labeled");
        EPStatement statement_Temp = runtime.getDeploymentService().getStatement(deployment_Temp.getDeploymentId(), "Temp");
        EPStatement statement_get_case_ID = runtime.getDeploymentService().getStatement(deployment_get_case_ID.getDeploymentId(), "get_case_ID");
        EPStatement statement_start_case_a = runtime.getDeploymentService().getStatement(deployment_start_case_a.getDeploymentId(), "start_case_a");
        EPStatement statement_TE_d = runtime.getDeploymentService().getStatement(deployment_TE_d.getDeploymentId(), "TE_d");

        EPStatement statement_Correlate_d = runtime.getDeploymentService().getStatement(deployment_Correlate_d.getDeploymentId(), "Correlate_d");

        EPStatement statement_Filter_d = runtime.getDeploymentService().getStatement(deployment_Filter_d.getDeploymentId(), "Filter_d");

        statement_Unlabeled.addListener((newData, oldData, s, r) -> {
//          String activity = (String) newData[0].get("activity");
//          long timestamp = (long) newData[0].get("timestamp");
//      	initTime = (long) newData[0].get("sysTime");
//          long sysTime = (long) newData[0].get("sysTime");
          initTime = (long) newData[0].get("sysTime");
//          initTime = sysTime;
//          Date ts = new Date(timestamp);
//          Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//          System.out.println("UnlabeledEvent ["+ activity+" , "+format.format(ts)+","+sysTime+"]");//+caseID + " , "
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
//
//        statement_Temp.addListener((newData, oldData, s, r) -> {
//            long caseID = (long) newData[0].get("caseID");
//            String activity = (String) newData[0].get("activity");
//            long timestamp = (long) newData[0].get("timestamp");
//            Date ts = new Date(timestamp);
//            Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//            System.out.println("--- TempEvent ["+caseID + " , " + activity+" , "+format.format(ts)+"]");
//        });

/*
        statement_TE_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_d-instance "+newData[0].getUnderlying()); }); 

        statement_Correlate_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_d-instance "+newData[0].getUnderlying()); }); 

        statement_Filter_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_d-instance "+newData[0].getUnderlying()); }); 

*/

        String filePath = "C:\\IH\\eclipse-workspace\\CEPForEventCorrelation\\loop1000_unLabeled.csv";
        new UnlabeledEventSource(filePath, 8540, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent

        frLabeled.close(); 
        date = new Date(); 
        System.out.println("End time "+formatter.format(date));

    }
}