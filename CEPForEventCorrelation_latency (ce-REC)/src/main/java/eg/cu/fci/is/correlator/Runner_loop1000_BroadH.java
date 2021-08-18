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
import eg.cu.fci.is.correlator.source.UnlabeledEventSource_eventID;

public class Runner_loop1000_BroadH {
	
	private static long initTime =0;

    public static void main(String[] args) throws InterruptedException, IOException {


//    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\loop1000_BroadH_labeled_5_1_latency.csv";
//    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\loop1000_BroadH_labeled_H++_1_latency.csv"; //incorrect heuristics
    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\loop1000_eventID_BroadH_labeled_H+_latency2.csv"; //max+15000
//    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\loop1000_eventID_BroadH_labeled_H++_latency.csv";//max+30000
//    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\loop1000_eventID_BroadH_labeled_H+++_latency.csv";//max+45000
        FileWriter frLabeled = new FileWriter(labeledEvents,true);
        long countLines  = Files.lines(Paths.get(labeledEvents), Charset.defaultCharset()).count(); 
        if(countLines == 0)
        {
        	        	frLabeled.append("EventID,CaseID,Activity,Timestamp,Probability,initTime,sysTime\n");
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

        String epl_start_case_a =" @Name('start_case_a')  insert into FilterLabeledEvent (eventID, caseID, activity, timestamp, probability, initTime)  select UE.eventID, (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0, UE.sysTime as initTime  from UnlabeledEvent as UE where UE.activity = 'a'; ";

        //*// H+
        String epl_TE_d =" @Priority(5) @Name('TE_d') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'd' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a') or every pred=FilterLabeledEvent(activity='e') -> every succ=UnlabeledEvent(activity='d')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=75000;";

        String epl_TE_b =" @Priority(5) @Name('TE_b') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'b' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'd') -> every succ=UnlabeledEvent(activity='b')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=75000;";

        String epl_TE_c =" @Priority(5) @Name('TE_c') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'c' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='c')) where timer:within(181 sec)]#time(181 sec)  where (succ.timestamp - pred.timestamp) >=  120000 and (succ.timestamp - pred.timestamp) <=135000;";

        String epl_TE_e =" @Priority(5) @Name('TE_e') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'e' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='e')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=75000;";

        String epl_TE_f =" @Priority(5) @Name('TE_f') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'f' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='f')) where timer:within(181 sec)]#time(181 sec)  where (succ.timestamp - pred.timestamp) >=  120000 and (succ.timestamp - pred.timestamp) <=135000;";

        String epl_TE_g =" @Priority(5) @Name('TE_g') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'g' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'c') -> every succ=UnlabeledEvent(activity='g')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=75000;";

        String epl_TE_h =" @Priority(5) @Name('TE_h') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'h' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='g') or every pred=FilterLabeledEvent(activity='f') -> every succ=UnlabeledEvent(activity='h')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=75000;";
		//*/

        /*// H++
        String epl_TE_d =" @Priority(5) @Name('TE_d') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'd' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a') or every pred=FilterLabeledEvent(activity='e') -> every succ=UnlabeledEvent(activity='d')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=90000;";

        String epl_TE_b =" @Priority(5) @Name('TE_b') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'b' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'd') -> every succ=UnlabeledEvent(activity='b')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=90000;";

        String epl_TE_c =" @Priority(5) @Name('TE_c') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'c' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='c')) where timer:within(181 sec)]#time(181 sec)  where (succ.timestamp - pred.timestamp) >=  120000 and (succ.timestamp - pred.timestamp) <=150000;";

        String epl_TE_e =" @Priority(5) @Name('TE_e') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'e' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='e')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=90000;";

        String epl_TE_f =" @Priority(5) @Name('TE_f') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'f' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='f')) where timer:within(181 sec)]#time(181 sec)  where (succ.timestamp - pred.timestamp) >=  120000 and (succ.timestamp - pred.timestamp) <=150000;";

        String epl_TE_g =" @Priority(5) @Name('TE_g') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'g' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'c') -> every succ=UnlabeledEvent(activity='g')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=90000;";

        String epl_TE_h =" @Priority(5) @Name('TE_h') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'h' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='g') or every pred=FilterLabeledEvent(activity='f') -> every succ=UnlabeledEvent(activity='h')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=90000;";
		//*/

        /*// H+++
        String epl_TE_d =" @Priority(5) @Name('TE_d') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'd' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a') or every pred=FilterLabeledEvent(activity='e') -> every succ=UnlabeledEvent(activity='d')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=105000;";

        String epl_TE_b =" @Priority(5) @Name('TE_b') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'b' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'd') -> every succ=UnlabeledEvent(activity='b')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=105000;";

        String epl_TE_c =" @Priority(5) @Name('TE_c') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'c' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='c')) where timer:within(181 sec)]#time(181 sec)  where (succ.timestamp - pred.timestamp) >=  120000 and (succ.timestamp - pred.timestamp) <=165000;";

        String epl_TE_e =" @Priority(5) @Name('TE_e') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'e' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='e')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=105000;";

        String epl_TE_f =" @Priority(5) @Name('TE_f') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'f' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b') -> every succ=UnlabeledEvent(activity='f')) where timer:within(181 sec)]#time(181 sec)  where (succ.timestamp - pred.timestamp) >=  120000 and (succ.timestamp - pred.timestamp) <=165000;";

        String epl_TE_g =" @Priority(5) @Name('TE_g') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'g' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'c') -> every succ=UnlabeledEvent(activity='g')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=105000;";

        String epl_TE_h =" @Priority(5) @Name('TE_h') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 'h' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='g') or every pred=FilterLabeledEvent(activity='f') -> every succ=UnlabeledEvent(activity='h')) where timer:within(121 sec)]#time(121 sec)  where (succ.timestamp - pred.timestamp) >=  60000 and (succ.timestamp - pred.timestamp) <=105000;";
		//*/
        String epl_Correlate_d =" @Priority(20) @Name('Correlate_d') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'd'  group by eventID, caseID order by timestamp desc limit 5 ; ";

        String epl_Correlate_b =" @Priority(20) @Name('Correlate_b') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'b'  group by eventID, caseID order by timestamp desc limit 5 ; ";

        String epl_Correlate_c =" @Priority(20) @Name('Correlate_c') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'c'  group by eventID, caseID order by timestamp desc limit 5 ; ";

        String epl_Correlate_e =" @Priority(20) @Name('Correlate_e') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'e'  group by eventID, caseID order by timestamp desc limit 5 ; ";

        String epl_Correlate_f =" @Priority(20) @Name('Correlate_f') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'f'  group by eventID, caseID order by timestamp desc limit 5 ; ";

        String epl_Correlate_g =" @Priority(20) @Name('Correlate_g') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'g'  group by eventID, caseID order by timestamp desc limit 5 ; ";

        String epl_Correlate_h =" @Priority(20) @Name('Correlate_h') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'h'  group by eventID, caseID order by timestamp desc limit 5 ; ";

        String epl_Filter_d =" @Priority(50) @Name('Filter_d') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'd'  and probability >= 0.0 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_b =" @Priority(50) @Name('Filter_b') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'b'  and probability >= 0.0 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_c =" @Priority(50) @Name('Filter_c') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'c'  and probability >= 0.0 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_e =" @Priority(50) @Name('Filter_e') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'e'  and probability >= 0.0 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_f =" @Priority(50) @Name('Filter_f') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'f'  and probability >= 0.0 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_g =" @Priority(50) @Name('Filter_g') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'g'  and probability >= 0.0 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_h =" @Priority(50) @Name('Filter_h') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"  from LabeledEvent.win:time(1 sec) as T where activity = 'h'  and probability >= 0.0 group by eventID, caseID, probability order by probability desc limit 1 ;";

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

        EPCompiled epCompiled_TE_b;
        EPDeployment deployment_TE_b;

        EPCompiled epCompiled_TE_c;
        EPDeployment deployment_TE_c;

        EPCompiled epCompiled_TE_e;
        EPDeployment deployment_TE_e;

        EPCompiled epCompiled_TE_f;
        EPDeployment deployment_TE_f;

        EPCompiled epCompiled_TE_g;
        EPDeployment deployment_TE_g;

        EPCompiled epCompiled_TE_h;
        EPDeployment deployment_TE_h;

        EPCompiled epCompiled_Correlate_d;
        EPDeployment deployment_Correlate_d;

        EPCompiled epCompiled_Correlate_b;
        EPDeployment deployment_Correlate_b;

        EPCompiled epCompiled_Correlate_c;
        EPDeployment deployment_Correlate_c;

        EPCompiled epCompiled_Correlate_e;
        EPDeployment deployment_Correlate_e;

        EPCompiled epCompiled_Correlate_f;
        EPDeployment deployment_Correlate_f;

        EPCompiled epCompiled_Correlate_g;
        EPDeployment deployment_Correlate_g;

        EPCompiled epCompiled_Correlate_h;
        EPDeployment deployment_Correlate_h;

        EPCompiled epCompiled_Filter_d;
        EPDeployment deployment_Filter_d;

        EPCompiled epCompiled_Filter_b;
        EPDeployment deployment_Filter_b;

        EPCompiled epCompiled_Filter_c;
        EPDeployment deployment_Filter_c;

        EPCompiled epCompiled_Filter_e;
        EPDeployment deployment_Filter_e;

        EPCompiled epCompiled_Filter_f;
        EPDeployment deployment_Filter_f;

        EPCompiled epCompiled_Filter_g;
        EPDeployment deployment_Filter_g;

        EPCompiled epCompiled_Filter_h;
        EPDeployment deployment_Filter_h;

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

            epCompiled_TE_b = compiler.compile(epl_TE_b, arguments);
            deployment_TE_b = deploymentService.deploy(epCompiled_TE_b);

            epCompiled_TE_c = compiler.compile(epl_TE_c, arguments);
            deployment_TE_c = deploymentService.deploy(epCompiled_TE_c);

            epCompiled_TE_e = compiler.compile(epl_TE_e, arguments);
            deployment_TE_e = deploymentService.deploy(epCompiled_TE_e);

            epCompiled_TE_f = compiler.compile(epl_TE_f, arguments);
            deployment_TE_f = deploymentService.deploy(epCompiled_TE_f);

            epCompiled_TE_g = compiler.compile(epl_TE_g, arguments);
            deployment_TE_g = deploymentService.deploy(epCompiled_TE_g);

            epCompiled_TE_h = compiler.compile(epl_TE_h, arguments);
            deployment_TE_h = deploymentService.deploy(epCompiled_TE_h);

            CompilerArguments arguments2 = new CompilerArguments(configuration);
            arguments2.getPath().add(runtime.getRuntimePath());

            epCompiled_Correlate_d = EPCompilerProvider.getCompiler().compile(epl_Correlate_d, arguments2);
            deployment_Correlate_d = deploymentService.deploy(epCompiled_Correlate_d);

            epCompiled_Correlate_b = EPCompilerProvider.getCompiler().compile(epl_Correlate_b, arguments2);
            deployment_Correlate_b = deploymentService.deploy(epCompiled_Correlate_b);

            epCompiled_Correlate_c = EPCompilerProvider.getCompiler().compile(epl_Correlate_c, arguments2);
            deployment_Correlate_c = deploymentService.deploy(epCompiled_Correlate_c);

            epCompiled_Correlate_e = EPCompilerProvider.getCompiler().compile(epl_Correlate_e, arguments2);
            deployment_Correlate_e = deploymentService.deploy(epCompiled_Correlate_e);

            epCompiled_Correlate_f = EPCompilerProvider.getCompiler().compile(epl_Correlate_f, arguments2);
            deployment_Correlate_f = deploymentService.deploy(epCompiled_Correlate_f);

            epCompiled_Correlate_g = EPCompilerProvider.getCompiler().compile(epl_Correlate_g, arguments2);
            deployment_Correlate_g = deploymentService.deploy(epCompiled_Correlate_g);

            epCompiled_Correlate_h = EPCompilerProvider.getCompiler().compile(epl_Correlate_h, arguments2);
            deployment_Correlate_h = deploymentService.deploy(epCompiled_Correlate_h);


            CompilerArguments arguments3 = new CompilerArguments(configuration);
            arguments3.getPath().add(runtime.getRuntimePath());

            epCompiled_Filter_d = EPCompilerProvider.getCompiler().compile(epl_Filter_d, arguments3);
            deployment_Filter_d = deploymentService.deploy(epCompiled_Filter_d);

            epCompiled_Filter_b = EPCompilerProvider.getCompiler().compile(epl_Filter_b, arguments3);
            deployment_Filter_b = deploymentService.deploy(epCompiled_Filter_b);

            epCompiled_Filter_c = EPCompilerProvider.getCompiler().compile(epl_Filter_c, arguments3);
            deployment_Filter_c = deploymentService.deploy(epCompiled_Filter_c);

            epCompiled_Filter_e = EPCompilerProvider.getCompiler().compile(epl_Filter_e, arguments3);
            deployment_Filter_e = deploymentService.deploy(epCompiled_Filter_e);

            epCompiled_Filter_f = EPCompilerProvider.getCompiler().compile(epl_Filter_f, arguments3);
            deployment_Filter_f = deploymentService.deploy(epCompiled_Filter_f);

            epCompiled_Filter_g = EPCompilerProvider.getCompiler().compile(epl_Filter_g, arguments3);
            deployment_Filter_g = deploymentService.deploy(epCompiled_Filter_g);

            epCompiled_Filter_h = EPCompilerProvider.getCompiler().compile(epl_Filter_h, arguments3);
            deployment_Filter_h = deploymentService.deploy(epCompiled_Filter_h);


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
        EPStatement statement_TE_b = runtime.getDeploymentService().getStatement(deployment_TE_b.getDeploymentId(), "TE_b");
        EPStatement statement_TE_c = runtime.getDeploymentService().getStatement(deployment_TE_c.getDeploymentId(), "TE_c");
        EPStatement statement_TE_e = runtime.getDeploymentService().getStatement(deployment_TE_e.getDeploymentId(), "TE_e");
        EPStatement statement_TE_f = runtime.getDeploymentService().getStatement(deployment_TE_f.getDeploymentId(), "TE_f");
        EPStatement statement_TE_g = runtime.getDeploymentService().getStatement(deployment_TE_g.getDeploymentId(), "TE_g");
        EPStatement statement_TE_h = runtime.getDeploymentService().getStatement(deployment_TE_h.getDeploymentId(), "TE_h");

        EPStatement statement_Correlate_d = runtime.getDeploymentService().getStatement(deployment_Correlate_d.getDeploymentId(), "Correlate_d");
        EPStatement statement_Correlate_b = runtime.getDeploymentService().getStatement(deployment_Correlate_b.getDeploymentId(), "Correlate_b");
        EPStatement statement_Correlate_c = runtime.getDeploymentService().getStatement(deployment_Correlate_c.getDeploymentId(), "Correlate_c");
        EPStatement statement_Correlate_e = runtime.getDeploymentService().getStatement(deployment_Correlate_e.getDeploymentId(), "Correlate_e");
        EPStatement statement_Correlate_f = runtime.getDeploymentService().getStatement(deployment_Correlate_f.getDeploymentId(), "Correlate_f");
        EPStatement statement_Correlate_g = runtime.getDeploymentService().getStatement(deployment_Correlate_g.getDeploymentId(), "Correlate_g");
        EPStatement statement_Correlate_h = runtime.getDeploymentService().getStatement(deployment_Correlate_h.getDeploymentId(), "Correlate_h");

        EPStatement statement_Filter_d = runtime.getDeploymentService().getStatement(deployment_Filter_d.getDeploymentId(), "Filter_d");
        EPStatement statement_Filter_b = runtime.getDeploymentService().getStatement(deployment_Filter_b.getDeploymentId(), "Filter_b");
        EPStatement statement_Filter_c = runtime.getDeploymentService().getStatement(deployment_Filter_c.getDeploymentId(), "Filter_c");
        EPStatement statement_Filter_e = runtime.getDeploymentService().getStatement(deployment_Filter_e.getDeploymentId(), "Filter_e");
        EPStatement statement_Filter_f = runtime.getDeploymentService().getStatement(deployment_Filter_f.getDeploymentId(), "Filter_f");
        EPStatement statement_Filter_g = runtime.getDeploymentService().getStatement(deployment_Filter_g.getDeploymentId(), "Filter_g");
        EPStatement statement_Filter_h = runtime.getDeploymentService().getStatement(deployment_Filter_h.getDeploymentId(), "Filter_h");

        statement_Unlabeled.addListener((newData, oldData, s, r) -> {
//        	long eventID = (long) newData[0].get("eventID");
//        	String activity = (String) newData[0].get("activity");
//        	long timestamp = (long) newData[0].get("timestamp");
////      	  initTime = (long) newData[0].get("sysTime");
//        	long sysTime = (long) newData[0].get("sysTime");
        	initTime = (long) newData[0].get("sysTime");
////          initTime = sysTime;
//        	Date ts = new Date(timestamp);
//        	Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//        	System.out.println("UnlabeledEvent ["+eventID+" , "+ activity+" , "+format.format(ts)+","+sysTime+"]");//+caseID + " , "
      });


        statement_FilterLabeledEvent.addListener((newData, oldData, s, r) -> {
        	long eventID = (long) newData[0].get("eventID");
        	long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            double probability = (double) newData[0].get("probability");
            //initTime = (long) newData[0].get("initTime");
            long sysTime = (long) newData[0].get("sysTime");
            Date ts = new Date(timestamp);
            Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            try {
		    	frLabeled.append(eventID + "," + caseID + "," + activity+","+format.format(ts)+","+probability+","+initTime+","+sysTime+"\n");
		    	frLabeled.flush(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
//		    System.out.println("*** FilterLabeledEventEvent ["+eventID + "," +caseID + " , " + activity+" , "+format.format(ts)+" , "+probability+" , "+initTime+" , "+sysTime+"]");
        });

//        statement_Labeled.addListener((newData, oldData, s, r) -> {
//        	long eventID = (long) newData[0].get("eventID");
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
//		    System.out.println("*** LabeledEvent ["+eventID + "," +caseID + " , " + activity+" , "+format.format(ts)+" , "+probability+"]");
//        });

//        statement_Temp.addListener((newData, oldData, s, r) -> {
//        	long eventID = (long) newData[0].get("eventID");
//            long caseID = (long) newData[0].get("caseID");
//            String activity = (String) newData[0].get("activity");
//            long timestamp = (long) newData[0].get("timestamp");
//            Date ts = new Date(timestamp);
//            Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//            System.out.println("--- TempEvent ["+eventID + "," +caseID + " , " + activity+" , "+format.format(ts)+"]");
//        });

/*
        statement_TE_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_d-instance "+newData[0].getUnderlying()); }); 
        statement_TE_b.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_b-instance "+newData[0].getUnderlying()); }); 
        statement_TE_c.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_c-instance "+newData[0].getUnderlying()); }); 
        statement_TE_e.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_e-instance "+newData[0].getUnderlying()); }); 
        statement_TE_f.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_f-instance "+newData[0].getUnderlying()); }); 
        statement_TE_g.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_g-instance "+newData[0].getUnderlying()); }); 
        statement_TE_h.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_h-instance "+newData[0].getUnderlying()); }); 

        statement_Correlate_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_d-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_b.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_b-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_c.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_c-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_e.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_e-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_f.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_f-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_g.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_g-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_h.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_h-instance "+newData[0].getUnderlying()); }); 

        statement_Filter_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_d-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_b.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_b-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_c.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_c-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_e.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_e-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_f.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_f-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_g.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_g-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_h.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_h-instance "+newData[0].getUnderlying()); }); 

*/

        String filePath = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\Loop1000_unLabeled_eventID.csv";//_top100
        new UnlabeledEventSource_eventID(filePath, 8540, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent

        frLabeled.close(); 
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