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

public class Runner_ISJeg_Filter2 {

    public static void main(String[] args) throws InterruptedException, IOException {


    	String labeledEvents = "C:\\IH\\eclipse-workspace\\CEPForEventCorrelation\\ISJeg_Filter3_fail_labeled.csv";
        FileWriter frLabeled = new FileWriter(labeledEvents,true);
        long countLines  = Files.lines(Paths.get(labeledEvents), Charset.defaultCharset()).count(); 
        if(countLines == 0)
        {
        	frLabeled.append("CaseID,Activity,Timestamp,Probability\n");
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
        configuration.getCommon().addEventType(FailCorrelation.class);

        String epl_Unlabeled =" @Name('Unlabeled') select * from UnlabeledEvent; ";

        String epl_Labeled =" @Name('Labeled') select * from LabeledEvent; ";

        String epl_FilterLabeledEvent ="  @Name('FilterLabeledEvent') select * from FilterLabeledEvent; ";

        String epl_Temp =" @Name('Temp') select * from TempEvent ; ";
        
        String epl_FailCorrelation =" @Name('FailCorrelation') select * from FailCorrelation; ";

        String epl_get_case_ID =" @Name('get_case_ID')  insert into Cases  select (select coalesce(max(caseID)+1, 1)  from  Cases )  as caseID from UnlabeledEvent  where  activity in ('a');  ";

        String epl_start_case_a =" @Name('start_case_a')  insert into FilterLabeledEvent (caseID, activity, timestamp, probability)  select (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0  from UnlabeledEvent as UE where UE.activity = 'a'; ";

        String epl_TE_b =" @Priority(5) @Name('TE_b') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'b' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a' or activity='n')) -> every succ=UnlabeledEvent(activity='b') ]#time(4 sec)  where succ.timestamp - pred.timestamp >= 1000 and succ.timestamp - pred.timestamp <=4000;";

        String epl_TE_c =" @Priority(5) @Name('TE_c') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'c' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b')) -> every succ=UnlabeledEvent(activity='c') ]#time(2 sec)  where succ.timestamp - pred.timestamp >= 1000 and succ.timestamp - pred.timestamp <=2000;";

        String epl_TE_d =" @Priority(5) @Name('TE_d') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'd' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'b')) -> every succ=UnlabeledEvent(activity='d') ]#time(1 sec)  where succ.timestamp - pred.timestamp >= 1000 and succ.timestamp - pred.timestamp <=1000;";

        String epl_TE_i =" @Priority(5) @Name('TE_i') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'i' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'c')) -> every succ=UnlabeledEvent(activity='i') ]#time(7 sec)  where succ.timestamp - pred.timestamp >= 1000 and succ.timestamp - pred.timestamp <=7000;";

        String epl_TE_j =" @Priority(5) @Name('TE_j') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'j' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'c')) -> every succ=UnlabeledEvent(activity='j') ]#time(4 sec)  where succ.timestamp - pred.timestamp >= 3000 and succ.timestamp - pred.timestamp <=4000;";

        String epl_TE_e =" @Priority(5) @Name('TE_e') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'e' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='d' or activity='h')) -> every succ=UnlabeledEvent(activity='e') ]#time(7 sec)  where succ.timestamp - pred.timestamp >= 1000 and succ.timestamp - pred.timestamp <=7000;";

        String epl_TE_g =" @Priority(5) @Name('TE_g') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'g' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'e')) -> every succ=UnlabeledEvent(activity='g') ]#time(3 sec)  where succ.timestamp - pred.timestamp >= 1000 and succ.timestamp - pred.timestamp <=3000;";

        String epl_TE_f =" @Priority(5) @Name('TE_f') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'f' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'e')) -> every succ=UnlabeledEvent(activity='f') ]#time(2 sec)  where succ.timestamp - pred.timestamp >= 2000 and succ.timestamp - pred.timestamp <=2000;";

        String epl_TE_h =" @Priority(5) @Name('TE_h') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'h' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'f')) -> every succ=UnlabeledEvent(activity='h') ]#time(3 sec)  where succ.timestamp - pred.timestamp >= 3000 and succ.timestamp - pred.timestamp <=3000;";

        String epl_TE_l =//" @Priority(5) @Name('TE_l') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'l' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='i') and every inter=FilterLabeledEvent(activity='j')) -> every succ=UnlabeledEvent(activity='l') ]#time(11 sec)  where pred.caseID = inter.caseID and (succ.timestamp - max(pred.timestamp,inter.timestamp)) >= 2000 and (succ.timestamp - max(pred.timestamp,inter.timestamp)) <=11000;";
        		"@Priority(5) @Name('TE_l') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'l' as activity, succ.timestamp as timestamp from pattern [( every pred=LabeledEvent(activity='g') -> every succ=UnlabeledEvent(activity='l')) or ((every pred=LabeledEvent(activity='i') and every inter=LabeledEvent(activity='j'))  -> every succ=UnlabeledEvent(activity='l')) ]#time(11 sec)  where (pred.caseID = inter.caseID and (succ.timestamp - max(pred.timestamp,inter.timestamp)) >= 2000 and (succ.timestamp - max(pred.timestamp,inter.timestamp)) <=11000) or (succ.timestamp - pred.timestamp >= 2000 and succ.timestamp - pred.timestamp <=11000);";
        
        String epl_TE_n =" @Priority(5) @Name('TE_n') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'n' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'l')) -> every succ=UnlabeledEvent(activity='n') ]#time(1 sec)  where succ.timestamp - pred.timestamp >= 1000 and succ.timestamp - pred.timestamp <=1000;";

        String epl_TE_m =" @Priority(5) @Name('TE_m') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'm' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'l')) -> every succ=UnlabeledEvent(activity='m') ]#time(9 sec)  where succ.timestamp - pred.timestamp >= 1000 and succ.timestamp - pred.timestamp <=9000;";

        String epl_Correlate_b =" @Priority(20) @Name('Correlate_b') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'b'  group by caseID order by timestamp; ";

        String epl_Correlate_c =" @Priority(20) @Name('Correlate_c') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'c'  group by caseID order by timestamp; ";

        String epl_Correlate_d =" @Priority(20) @Name('Correlate_d') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'd'  group by caseID order by timestamp; ";

        String epl_Correlate_i =" @Priority(20) @Name('Correlate_i') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'i'  group by caseID order by timestamp; ";

        String epl_Correlate_j =" @Priority(20) @Name('Correlate_j') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'j'  group by caseID order by timestamp; ";

        String epl_Correlate_e =" @Priority(20) @Name('Correlate_e') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'e'  group by caseID order by timestamp; ";

        String epl_Correlate_g =" @Priority(20) @Name('Correlate_g') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'g'  group by caseID order by timestamp; ";

        String epl_Correlate_f =" @Priority(20) @Name('Correlate_f') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'f'  group by caseID order by timestamp; ";

        String epl_Correlate_h =" @Priority(20) @Name('Correlate_h') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'h'  group by caseID order by timestamp; ";

        String epl_Correlate_l =" @Priority(20) @Name('Correlate_l') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'l'  group by caseID order by timestamp; ";

        String epl_Correlate_n =" @Priority(20) @Name('Correlate_n') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'n'  group by caseID order by timestamp; ";

        String epl_Correlate_m =" @Priority(20) @Name('Correlate_m') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'm'  group by caseID order by timestamp; ";

        String epl_Filter_b =" @Priority(50) @Name('Filter_b') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'b'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_c =" @Priority(50) @Name('Filter_c') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'c'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_d =" @Priority(50) @Name('Filter_d') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'd'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_i =" @Priority(50) @Name('Filter_i') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'i'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_j =" @Priority(50) @Name('Filter_j') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'j'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_e =" @Priority(50) @Name('Filter_e') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'e'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_g =" @Priority(50) @Name('Filter_g') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'g'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_f =" @Priority(50) @Name('Filter_f') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'f'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_h =" @Priority(50) @Name('Filter_h') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'h'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_l =" @Priority(50) @Name('Filter_l') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'l'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_n =" @Priority(50) @Name('Filter_n') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'n'  and probability >= 0.0   limit 5 ;";

        String epl_Filter_m =" @Priority(50) @Name('Filter_m') insert into FilterLabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability from LabeledEvent.win:time(1 sec) as T where activity = 'm'  and probability >= 0.0   limit 5 ;";
        
        String epl_Fail = "@Priority(0) @Name('Fail') insert into FailCorrelation (activity , timestamp) select ue.activity, ue.timestamp from pattern [ ue=UnlabeledEvent and not fe=FilterLabeledEvent(activity=ue.activity and timestamp=ue.timestamp) where timer:within(2 sec)]#time(2 sec); ";
        //"@Priority(0) @Name('Fail') insert into FailCorrelation (activity , timestamp ) select last(un.activity) as activity , max(un.timestamp) as timestamp from UnlabeledEvent as un where un.activity not in (select last(fe.activity) from FilterLabeledEvent as fe) and un.timestamp not in (select max(fe.timestamp) from FilterLabeledEvent as fe); ";
        // Get a runtime
        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
        EPDeploymentService  deploymentService = runtime.getDeploymentService();

        CompilerArguments arguments = new CompilerArguments(configuration);
        arguments.getPath().add(runtime.getRuntimePath());

        EPCompiled epCompiled_Unlabeled;
        EPDeployment deployment_Unlabeled;

        EPCompiled epCompiled_Labeled;
        EPDeployment deployment_Labeled;

        EPCompiled epCompiled_FilterLabeledEvent;
        EPDeployment deployment_FilterLabeledEvent;
        
        EPCompiled epCompiled_Temp;
        EPDeployment deployment_Temp;

        EPCompiled epCompiled_get_case_ID;
        EPDeployment deployment_get_case_ID;

        EPCompiled epCompiled_start_case_a;
        EPDeployment deployment_start_case_a;

        EPCompiled epCompiled_TE_b;
        EPDeployment deployment_TE_b;

        EPCompiled epCompiled_TE_c;
        EPDeployment deployment_TE_c;

        EPCompiled epCompiled_TE_d;
        EPDeployment deployment_TE_d;

        EPCompiled epCompiled_TE_i;
        EPDeployment deployment_TE_i;

        EPCompiled epCompiled_TE_j;
        EPDeployment deployment_TE_j;

        EPCompiled epCompiled_TE_e;
        EPDeployment deployment_TE_e;

        EPCompiled epCompiled_TE_g;
        EPDeployment deployment_TE_g;

        EPCompiled epCompiled_TE_f;
        EPDeployment deployment_TE_f;

        EPCompiled epCompiled_TE_h;
        EPDeployment deployment_TE_h;

        EPCompiled epCompiled_TE_l;
        EPDeployment deployment_TE_l;

        EPCompiled epCompiled_TE_n;
        EPDeployment deployment_TE_n;

        EPCompiled epCompiled_TE_m;
        EPDeployment deployment_TE_m;

        EPCompiled epCompiled_Correlate_b;
        EPDeployment deployment_Correlate_b;

        EPCompiled epCompiled_Correlate_c;
        EPDeployment deployment_Correlate_c;

        EPCompiled epCompiled_Correlate_d;
        EPDeployment deployment_Correlate_d;

        EPCompiled epCompiled_Correlate_i;
        EPDeployment deployment_Correlate_i;

        EPCompiled epCompiled_Correlate_j;
        EPDeployment deployment_Correlate_j;

        EPCompiled epCompiled_Correlate_e;
        EPDeployment deployment_Correlate_e;

        EPCompiled epCompiled_Correlate_g;
        EPDeployment deployment_Correlate_g;

        EPCompiled epCompiled_Correlate_f;
        EPDeployment deployment_Correlate_f;

        EPCompiled epCompiled_Correlate_h;
        EPDeployment deployment_Correlate_h;

        EPCompiled epCompiled_Correlate_l;
        EPDeployment deployment_Correlate_l;

        EPCompiled epCompiled_Correlate_n;
        EPDeployment deployment_Correlate_n;

        EPCompiled epCompiled_Correlate_m;
        EPDeployment deployment_Correlate_m;

        EPCompiled epCompiled_Filter_b;
        EPDeployment deployment_Filter_b;

        EPCompiled epCompiled_Filter_c;
        EPDeployment deployment_Filter_c;

        EPCompiled epCompiled_Filter_d;
        EPDeployment deployment_Filter_d;

        EPCompiled epCompiled_Filter_i;
        EPDeployment deployment_Filter_i;

        EPCompiled epCompiled_Filter_j;
        EPDeployment deployment_Filter_j;

        EPCompiled epCompiled_Filter_e;
        EPDeployment deployment_Filter_e;

        EPCompiled epCompiled_Filter_g;
        EPDeployment deployment_Filter_g;

        EPCompiled epCompiled_Filter_f;
        EPDeployment deployment_Filter_f;

        EPCompiled epCompiled_Filter_h;
        EPDeployment deployment_Filter_h;

        EPCompiled epCompiled_Filter_l;
        EPDeployment deployment_Filter_l;

        EPCompiled epCompiled_Filter_n;
        EPDeployment deployment_Filter_n;

        EPCompiled epCompiled_Filter_m;
        EPDeployment deployment_Filter_m;

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

            epCompiled_start_case_a = compiler.compile(epl_start_case_a, arguments);
            deployment_start_case_a = deploymentService.deploy(epCompiled_start_case_a);

            epCompiled_TE_b = compiler.compile(epl_TE_b, arguments);
            deployment_TE_b = deploymentService.deploy(epCompiled_TE_b);

            epCompiled_TE_c = compiler.compile(epl_TE_c, arguments);
            deployment_TE_c = deploymentService.deploy(epCompiled_TE_c);

            epCompiled_TE_d = compiler.compile(epl_TE_d, arguments);
            deployment_TE_d = deploymentService.deploy(epCompiled_TE_d);

            epCompiled_TE_i = compiler.compile(epl_TE_i, arguments);
            deployment_TE_i = deploymentService.deploy(epCompiled_TE_i);

            epCompiled_TE_j = compiler.compile(epl_TE_j, arguments);
            deployment_TE_j = deploymentService.deploy(epCompiled_TE_j);

            epCompiled_TE_e = compiler.compile(epl_TE_e, arguments);
            deployment_TE_e = deploymentService.deploy(epCompiled_TE_e);

            epCompiled_TE_g = compiler.compile(epl_TE_g, arguments);
            deployment_TE_g = deploymentService.deploy(epCompiled_TE_g);

            epCompiled_TE_f = compiler.compile(epl_TE_f, arguments);
            deployment_TE_f = deploymentService.deploy(epCompiled_TE_f);

            epCompiled_TE_h = compiler.compile(epl_TE_h, arguments);
            deployment_TE_h = deploymentService.deploy(epCompiled_TE_h);

            epCompiled_TE_l = compiler.compile(epl_TE_l, arguments);
            deployment_TE_l = deploymentService.deploy(epCompiled_TE_l);

            epCompiled_TE_n = compiler.compile(epl_TE_n, arguments);
            deployment_TE_n = deploymentService.deploy(epCompiled_TE_n);

            epCompiled_TE_m = compiler.compile(epl_TE_m, arguments);
            deployment_TE_m = deploymentService.deploy(epCompiled_TE_m);

            CompilerArguments arguments2 = new CompilerArguments(configuration);
            arguments2.getPath().add(runtime.getRuntimePath());

            epCompiled_Correlate_b = EPCompilerProvider.getCompiler().compile(epl_Correlate_b, arguments2);
            deployment_Correlate_b = deploymentService.deploy(epCompiled_Correlate_b);

            epCompiled_Correlate_c = EPCompilerProvider.getCompiler().compile(epl_Correlate_c, arguments2);
            deployment_Correlate_c = deploymentService.deploy(epCompiled_Correlate_c);

            epCompiled_Correlate_d = EPCompilerProvider.getCompiler().compile(epl_Correlate_d, arguments2);
            deployment_Correlate_d = deploymentService.deploy(epCompiled_Correlate_d);

            epCompiled_Correlate_i = EPCompilerProvider.getCompiler().compile(epl_Correlate_i, arguments2);
            deployment_Correlate_i = deploymentService.deploy(epCompiled_Correlate_i);

            epCompiled_Correlate_j = EPCompilerProvider.getCompiler().compile(epl_Correlate_j, arguments2);
            deployment_Correlate_j = deploymentService.deploy(epCompiled_Correlate_j);

            epCompiled_Correlate_e = EPCompilerProvider.getCompiler().compile(epl_Correlate_e, arguments2);
            deployment_Correlate_e = deploymentService.deploy(epCompiled_Correlate_e);

            epCompiled_Correlate_g = EPCompilerProvider.getCompiler().compile(epl_Correlate_g, arguments2);
            deployment_Correlate_g = deploymentService.deploy(epCompiled_Correlate_g);

            epCompiled_Correlate_f = EPCompilerProvider.getCompiler().compile(epl_Correlate_f, arguments2);
            deployment_Correlate_f = deploymentService.deploy(epCompiled_Correlate_f);

            epCompiled_Correlate_h = EPCompilerProvider.getCompiler().compile(epl_Correlate_h, arguments2);
            deployment_Correlate_h = deploymentService.deploy(epCompiled_Correlate_h);

            epCompiled_Correlate_l = EPCompilerProvider.getCompiler().compile(epl_Correlate_l, arguments2);
            deployment_Correlate_l = deploymentService.deploy(epCompiled_Correlate_l);

            epCompiled_Correlate_n = EPCompilerProvider.getCompiler().compile(epl_Correlate_n, arguments2);
            deployment_Correlate_n = deploymentService.deploy(epCompiled_Correlate_n);

            epCompiled_Correlate_m = EPCompilerProvider.getCompiler().compile(epl_Correlate_m, arguments2);
            deployment_Correlate_m = deploymentService.deploy(epCompiled_Correlate_m);

            CompilerArguments arguments3 = new CompilerArguments(configuration);
            arguments.getPath().add(runtime.getRuntimePath());

            epCompiled_Filter_b = EPCompilerProvider.getCompiler().compile(epl_Filter_b, arguments3);
            deployment_Filter_b = deploymentService.deploy(epCompiled_Filter_b);

            epCompiled_Filter_c = EPCompilerProvider.getCompiler().compile(epl_Filter_c, arguments3);
            deployment_Filter_c = deploymentService.deploy(epCompiled_Filter_c);

            epCompiled_Filter_d = EPCompilerProvider.getCompiler().compile(epl_Filter_d, arguments3);
            deployment_Filter_d = deploymentService.deploy(epCompiled_Filter_d);

            epCompiled_Filter_i = EPCompilerProvider.getCompiler().compile(epl_Filter_i, arguments3);
            deployment_Filter_i = deploymentService.deploy(epCompiled_Filter_i);

            epCompiled_Filter_j = EPCompilerProvider.getCompiler().compile(epl_Filter_j, arguments3);
            deployment_Filter_j = deploymentService.deploy(epCompiled_Filter_j);

            epCompiled_Filter_e = EPCompilerProvider.getCompiler().compile(epl_Filter_e, arguments3);
            deployment_Filter_e = deploymentService.deploy(epCompiled_Filter_e);

            epCompiled_Filter_g = EPCompilerProvider.getCompiler().compile(epl_Filter_g, arguments3);
            deployment_Filter_g = deploymentService.deploy(epCompiled_Filter_g);

            epCompiled_Filter_f = EPCompilerProvider.getCompiler().compile(epl_Filter_f, arguments3);
            deployment_Filter_f = deploymentService.deploy(epCompiled_Filter_f);

            epCompiled_Filter_h = EPCompilerProvider.getCompiler().compile(epl_Filter_h, arguments3);
            deployment_Filter_h = deploymentService.deploy(epCompiled_Filter_h);

            epCompiled_Filter_l = EPCompilerProvider.getCompiler().compile(epl_Filter_l, arguments3);
            deployment_Filter_l = deploymentService.deploy(epCompiled_Filter_l);

            epCompiled_Filter_n = EPCompilerProvider.getCompiler().compile(epl_Filter_n, arguments3);
            deployment_Filter_n = deploymentService.deploy(epCompiled_Filter_n);

            epCompiled_Filter_m = EPCompilerProvider.getCompiler().compile(epl_Filter_m, arguments3);
            deployment_Filter_m = deploymentService.deploy(epCompiled_Filter_m);

            CompilerArguments arguments4 = new CompilerArguments(configuration);
            arguments.getPath().add(runtime.getRuntimePath());
            
            epCompiled_FailCorrelation = compiler.compile(epl_FailCorrelation, arguments4);
            deployment_FailCorrelation = deploymentService.deploy(epCompiled_FailCorrelation);
            
            epCompiled_Fail = EPCompilerProvider.getCompiler().compile(epl_Fail, arguments4);
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
        
        EPStatement statement_Unlabeled = runtime.getDeploymentService().getStatement(deployment_Unlabeled.getDeploymentId(), "Unlabeled");
        EPStatement statement_FilterLabeledEvent = runtime.getDeploymentService().getStatement(deployment_FilterLabeledEvent.getDeploymentId(), "FilterLabeledEvent");
        EPStatement statement_Labeled = runtime.getDeploymentService().getStatement(deployment_Labeled.getDeploymentId(), "Labeled");
        EPStatement statement_Temp = runtime.getDeploymentService().getStatement(deployment_Temp.getDeploymentId(), "Temp");
        EPStatement statement_FailCorrelation = runtime.getDeploymentService().getStatement(deployment_FailCorrelation.getDeploymentId(), "FailCorrelation");
        
        EPStatement statement_get_case_ID = runtime.getDeploymentService().getStatement(deployment_get_case_ID.getDeploymentId(), "get_case_ID");
        EPStatement statement_start_case_a = runtime.getDeploymentService().getStatement(deployment_start_case_a.getDeploymentId(), "start_case_a");
        
        EPStatement statement_TE_b = runtime.getDeploymentService().getStatement(deployment_TE_b.getDeploymentId(), "TE_b");
        EPStatement statement_TE_c = runtime.getDeploymentService().getStatement(deployment_TE_c.getDeploymentId(), "TE_c");
        EPStatement statement_TE_d = runtime.getDeploymentService().getStatement(deployment_TE_d.getDeploymentId(), "TE_d");
        EPStatement statement_TE_i = runtime.getDeploymentService().getStatement(deployment_TE_i.getDeploymentId(), "TE_i");
        EPStatement statement_TE_j = runtime.getDeploymentService().getStatement(deployment_TE_j.getDeploymentId(), "TE_j");
        EPStatement statement_TE_e = runtime.getDeploymentService().getStatement(deployment_TE_e.getDeploymentId(), "TE_e");
        EPStatement statement_TE_g = runtime.getDeploymentService().getStatement(deployment_TE_g.getDeploymentId(), "TE_g");
        EPStatement statement_TE_f = runtime.getDeploymentService().getStatement(deployment_TE_f.getDeploymentId(), "TE_f");
        EPStatement statement_TE_h = runtime.getDeploymentService().getStatement(deployment_TE_h.getDeploymentId(), "TE_h");
        EPStatement statement_TE_l = runtime.getDeploymentService().getStatement(deployment_TE_l.getDeploymentId(), "TE_l");
        EPStatement statement_TE_n = runtime.getDeploymentService().getStatement(deployment_TE_n.getDeploymentId(), "TE_n");
        EPStatement statement_TE_m = runtime.getDeploymentService().getStatement(deployment_TE_m.getDeploymentId(), "TE_m");

        EPStatement statement_Correlate_b = runtime.getDeploymentService().getStatement(deployment_Correlate_b.getDeploymentId(), "Correlate_b");
        EPStatement statement_Correlate_c = runtime.getDeploymentService().getStatement(deployment_Correlate_c.getDeploymentId(), "Correlate_c");
        EPStatement statement_Correlate_d = runtime.getDeploymentService().getStatement(deployment_Correlate_d.getDeploymentId(), "Correlate_d");
        EPStatement statement_Correlate_i = runtime.getDeploymentService().getStatement(deployment_Correlate_i.getDeploymentId(), "Correlate_i");
        EPStatement statement_Correlate_j = runtime.getDeploymentService().getStatement(deployment_Correlate_j.getDeploymentId(), "Correlate_j");
        EPStatement statement_Correlate_e = runtime.getDeploymentService().getStatement(deployment_Correlate_e.getDeploymentId(), "Correlate_e");
        EPStatement statement_Correlate_g = runtime.getDeploymentService().getStatement(deployment_Correlate_g.getDeploymentId(), "Correlate_g");
        EPStatement statement_Correlate_f = runtime.getDeploymentService().getStatement(deployment_Correlate_f.getDeploymentId(), "Correlate_f");
        EPStatement statement_Correlate_h = runtime.getDeploymentService().getStatement(deployment_Correlate_h.getDeploymentId(), "Correlate_h");
        EPStatement statement_Correlate_l = runtime.getDeploymentService().getStatement(deployment_Correlate_l.getDeploymentId(), "Correlate_l");
        EPStatement statement_Correlate_n = runtime.getDeploymentService().getStatement(deployment_Correlate_n.getDeploymentId(), "Correlate_n");
        EPStatement statement_Correlate_m = runtime.getDeploymentService().getStatement(deployment_Correlate_m.getDeploymentId(), "Correlate_m");

        EPStatement statement_Filter_b = runtime.getDeploymentService().getStatement(deployment_Filter_b.getDeploymentId(), "Filter_b");
        EPStatement statement_Filter_c = runtime.getDeploymentService().getStatement(deployment_Filter_c.getDeploymentId(), "Filter_c");
        EPStatement statement_Filter_d = runtime.getDeploymentService().getStatement(deployment_Filter_d.getDeploymentId(), "Filter_d");
        EPStatement statement_Filter_i = runtime.getDeploymentService().getStatement(deployment_Filter_i.getDeploymentId(), "Filter_i");
        EPStatement statement_Filter_j = runtime.getDeploymentService().getStatement(deployment_Filter_j.getDeploymentId(), "Filter_j");
        EPStatement statement_Filter_e = runtime.getDeploymentService().getStatement(deployment_Filter_e.getDeploymentId(), "Filter_e");
        EPStatement statement_Filter_g = runtime.getDeploymentService().getStatement(deployment_Filter_g.getDeploymentId(), "Filter_g");
        EPStatement statement_Filter_f = runtime.getDeploymentService().getStatement(deployment_Filter_f.getDeploymentId(), "Filter_f");
        EPStatement statement_Filter_h = runtime.getDeploymentService().getStatement(deployment_Filter_h.getDeploymentId(), "Filter_h");
        EPStatement statement_Filter_l = runtime.getDeploymentService().getStatement(deployment_Filter_l.getDeploymentId(), "Filter_l");
        EPStatement statement_Filter_n = runtime.getDeploymentService().getStatement(deployment_Filter_n.getDeploymentId(), "Filter_n");
        EPStatement statement_Filter_m = runtime.getDeploymentService().getStatement(deployment_Filter_m.getDeploymentId(), "Filter_m");

        EPStatement statement_Fail = runtime.getDeploymentService().getStatement(deployment_Fail.getDeploymentId(), "Fail");

        statement_Unlabeled.addListener((newData, oldData, s, r) -> {
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            Date ts = new Date(timestamp);
	        Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            System.out.println("UnlabeledEvent ["+ activity+" , "+format.format(ts)+"]");//+caseID + " , "
        });

//        statement_Labeled.addListener((newData, oldData, s, r) -> {
//            long caseID = (long) newData[0].get("caseID");
//            String activity = (String) newData[0].get("activity");
//            long timestamp = (long) newData[0].get("timestamp");
//            double probability = (double) newData[0].get("probability");
//		     Date ts = new Date(timestamp);
//	         Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
////	         try { 
////		    	frLabeled.append(caseID + "," + activity+","+format.format(ts)+","+probability+"\n");
////		    	frLabeled.flush();
////			} catch (IOException e) {
////				e.printStackTrace();
////			}
//		    System.out.println("*** LabeledEvent ["+caseID + " , " + activity+" , "+format.format(ts)+" , "+probability+"]");
//        });
//
        statement_FilterLabeledEvent.addListener((newData, oldData, s, r) -> {
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            double probability = (double) newData[0].get("probability");
		    Date ts = new Date(timestamp);
	        Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	        try {
		    	frLabeled.append(caseID + "," + activity+","+format.format(ts)+","+probability+"\n");
		    	frLabeled.flush(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
//		    System.out.println("*** FilterLabeledEventEvent ["+caseID + " , " + activity+" , "+format.format(ts)+" , "+probability+"]");
        });
//        statement_Temp.addListener((newData, oldData, s, r) -> {
//            long caseID = (long) newData[0].get("caseID");
//            String activity = (String) newData[0].get("activity");
//            long timestamp = (long) newData[0].get("timestamp");
//            System.out.println("--- TempEvent ["+caseID + " , " + activity+" , "+timestamp+"]");
//        });
        

        statement_FailCorrelation.addListener((newData, oldData, s, r) -> {
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            Date ts = new Date(timestamp);
	        Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            System.out.println("FailCorrelation ["+ activity+" , "+format.format(ts)+"]");//+caseID + " , "
        });

//
//        statement_TE_b.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_b-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_c.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_c-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_d-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_i.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_i-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_j.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_j-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_e.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_e-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_g.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_g-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_f.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_f-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_h.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_h-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_l.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_l-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_n.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_n-instance "+newData[0].getUnderlying()); }); 
//        statement_TE_m.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_m-instance "+newData[0].getUnderlying()); }); 
//
//        statement_Correlate_b.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_b-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_c.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_c-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_d-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_i.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_i-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_j.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_j-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_e.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_e-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_g.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_g-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_f.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_f-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_h.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_h-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_l.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_l-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_n.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_n-instance "+newData[0].getUnderlying()); }); 
//        statement_Correlate_m.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_m-instance "+newData[0].getUnderlying()); }); 
//
//        statement_Filter_b.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_b-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_c.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_c-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_d.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_d-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_i.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_i-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_j.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_j-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_e.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_e-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_g.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_g-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_f.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_f-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_h.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_h-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_l.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_l-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_n.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_n-instance "+newData[0].getUnderlying()); }); 
//        statement_Filter_m.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_m-instance "+newData[0].getUnderlying()); }); 

        String filePath = "C:\\IH\\eclipse-workspace\\CEPForEventCorrelation\\ISJeg_unLabeled.csv";//ISJeg_Filter1_unLabeled.csv
        new UnlabeledEventSource(filePath, 100, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent

        frLabeled.close();        date = new Date();  
        System.out.println("End time "+formatter.format(date));

    }
}