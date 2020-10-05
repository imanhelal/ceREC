package eg.cu.fci.is.correlator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class Runner_Paper2 {

    public static void main(String[] args) throws InterruptedException, IOException{
    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation\\Paper2_labeled.csv";
        FileWriter frLabeled = new FileWriter(labeledEvents,true);
        long countLines  = Files.lines(Paths.get(labeledEvents), Charset.defaultCharset()).count(); 
        if(countLines == 0)
        	frLabeled.append("CaseID,Activity,Timestamp,Probability\n");



        EPCompiler compiler = EPCompilerProvider.getCompiler();

        Configuration configuration = new Configuration();
        configuration.getRuntime().getExecution().setPrioritized(true);

        // Here you register your event types
        configuration.getCommon().addEventType(Cases.class);
        configuration.getCommon().addEventType(UnlabeledEvent.class);
        configuration.getCommon().addEventType(LabeledEvent.class);
        configuration.getCommon().addEventType(TempEvent.class);

        String epl_Unlabeled =" @Name('Unlabeled') select * from UnlabeledEvent; ";

        String epl_Labeled =" @Name('Labeled') select * from LabeledEvent; ";

        String epl_Temp =" @Name('Temp') select * from TempEvent ; ";

        String epl_get_case_ID =" @Name('get_case_ID')  insert into Cases  select (select coalesce(max(caseID)+1, 1)  from  Cases )  as caseID from UnlabeledEvent  where  activity in ('A');  ";

        String epl_start_case_A =" @Name('start_case_A')  insert into LabeledEvent (caseID, activity, timestamp, probability)  select (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0  from UnlabeledEvent as UE where UE.activity = 'A'; ";

        String epl_TE_B =" @Priority(5) @Name('TE_B') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'B' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity='A' or activity='N')) -> every succ=UnlabeledEvent(activity='B') ]#time(30 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ;";

        String epl_TE_C =" @Priority(5) @Name('TE_C') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'C' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'B')) -> every succ=UnlabeledEvent(activity='C') ]#time(30 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ;";

        String epl_TE_D =" @Priority(5) @Name('TE_D') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'D' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'B')) -> every succ=UnlabeledEvent(activity='D') ]#time(60 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=60000 ;";

        String epl_TE_E =" @Priority(5) @Name('TE_E') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'E' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity='D' or activity='H')) -> every succ=UnlabeledEvent(activity='E') ]#time(30 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ;";

        String epl_TE_F =" @Priority(5) @Name('TE_F') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'F' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'E')) -> every succ=UnlabeledEvent(activity='F') ]#time(30 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ;";

        String epl_TE_G =" @Priority(5) @Name('TE_G') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'G' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'E')) -> every succ=UnlabeledEvent(activity='G') ]#time(30 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ;";

        String epl_TE_H =" @Priority(5) @Name('TE_H') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'H' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'F')) -> every succ=UnlabeledEvent(activity='H') ]#time(30 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ;";

        String epl_TE_I =" @Priority(5) @Name('TE_I') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'I' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'C')) -> every succ=UnlabeledEvent(activity='I') ]#time(90 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=90000 ;";

        String epl_TE_J =" @Priority(5) @Name('TE_J') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'J' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'C')) -> every succ=UnlabeledEvent(activity='J') ]#time(90 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=90000 ;";

        String epl_TE_L =" @Priority(5) @Name('TE_L') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'L' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity='I') and every inter=LabeledEvent(activity='J')) -> every succ=UnlabeledEvent(activity='L') ]#time(30 sec)  where pred.caseID = inter.caseID and (succ.timestamp - max(pred.timestamp,inter.timestamp)) >= 1 and (succ.timestamp - max(pred.timestamp,inter.timestamp)) <=30000 ; ";

        String epl_TE_M =" @Priority(5) @Name('TE_M') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'M' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'L')) -> every succ=UnlabeledEvent(activity='M') ]#time(30 sec)  where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ;";

        String epl_TE_N =" @Priority(5) @Name('TE_N') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'N' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'L')) -> every succ=UnlabeledEvent(activity='N') ]#time(50 sec)  where succ.timestamp - pred.timestamp >= 2 and succ.timestamp - pred.timestamp <=50000 ;";

        String epl_Correlate_B =" @Priority(20) @Name('Correlate_B') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'B'  group by caseID order by timestamp; ";

        String epl_Correlate_C =" @Priority(20) @Name('Correlate_C') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'C'  group by caseID order by timestamp; ";

        String epl_Correlate_D =" @Priority(20) @Name('Correlate_D') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'D'  group by caseID order by timestamp; ";

        String epl_Correlate_E =" @Priority(20) @Name('Correlate_E') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'E'  group by caseID order by timestamp; ";

        String epl_Correlate_F =" @Priority(20) @Name('Correlate_F') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'F'  group by caseID order by timestamp; ";

        String epl_Correlate_G =" @Priority(20) @Name('Correlate_G') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'G'  group by caseID order by timestamp; ";

        String epl_Correlate_H =" @Priority(20) @Name('Correlate_H') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'H'  group by caseID order by timestamp; ";

        String epl_Correlate_I =" @Priority(20) @Name('Correlate_I') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'I'  group by caseID order by timestamp; ";

        String epl_Correlate_J =" @Priority(20) @Name('Correlate_J') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'J'  group by caseID order by timestamp; ";

        String epl_Correlate_L =" @Priority(20) @Name('Correlate_L') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'L'  group by caseID order by timestamp; ";

        String epl_Correlate_M =" @Priority(20) @Name('Correlate_M') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'M'  group by caseID order by timestamp; ";

        String epl_Correlate_N =" @Priority(20) @Name('Correlate_N') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'N'  group by caseID order by timestamp; ";

        // Get a runtime
        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
        EPDeploymentService  deploymentService = runtime.getDeploymentService();

        CompilerArguments arguments = new CompilerArguments(configuration);
        arguments.getPath().add(runtime.getRuntimePath());

        EPCompiled epCompiled_Unlabeled;
        EPDeployment deployment_Unlabeled;

        EPCompiled epCompiled_Labeled;
        EPDeployment deployment_Labeled;

        EPCompiled epCompiled_Temp;
        EPDeployment deployment_Temp;

        EPCompiled epCompiled_get_case_ID;
        EPDeployment deployment_get_case_ID;

        EPCompiled epCompiled_start_case_A;
        EPDeployment deployment_start_case_A;

        EPCompiled epCompiled_TE_B;
        EPDeployment deployment_TE_B;

        EPCompiled epCompiled_TE_C;
        EPDeployment deployment_TE_C;

        EPCompiled epCompiled_TE_D;
        EPDeployment deployment_TE_D;

        EPCompiled epCompiled_TE_E;
        EPDeployment deployment_TE_E;

        EPCompiled epCompiled_TE_F;
        EPDeployment deployment_TE_F;

        EPCompiled epCompiled_TE_G;
        EPDeployment deployment_TE_G;

        EPCompiled epCompiled_TE_H;
        EPDeployment deployment_TE_H;

        EPCompiled epCompiled_TE_I;
        EPDeployment deployment_TE_I;

        EPCompiled epCompiled_TE_J;
        EPDeployment deployment_TE_J;

        EPCompiled epCompiled_TE_L;
        EPDeployment deployment_TE_L;

        EPCompiled epCompiled_TE_M;
        EPDeployment deployment_TE_M;

        EPCompiled epCompiled_TE_N;
        EPDeployment deployment_TE_N;

        EPCompiled epCompiled_Correlate_B;
        EPDeployment deployment_Correlate_B;

        EPCompiled epCompiled_Correlate_C;
        EPDeployment deployment_Correlate_C;

        EPCompiled epCompiled_Correlate_D;
        EPDeployment deployment_Correlate_D;

        EPCompiled epCompiled_Correlate_E;
        EPDeployment deployment_Correlate_E;

        EPCompiled epCompiled_Correlate_F;
        EPDeployment deployment_Correlate_F;

        EPCompiled epCompiled_Correlate_G;
        EPDeployment deployment_Correlate_G;

        EPCompiled epCompiled_Correlate_H;
        EPDeployment deployment_Correlate_H;

        EPCompiled epCompiled_Correlate_I;
        EPDeployment deployment_Correlate_I;

        EPCompiled epCompiled_Correlate_J;
        EPDeployment deployment_Correlate_J;

        EPCompiled epCompiled_Correlate_L;
        EPDeployment deployment_Correlate_L;

        EPCompiled epCompiled_Correlate_M;
        EPDeployment deployment_Correlate_M;

        EPCompiled epCompiled_Correlate_N;
        EPDeployment deployment_Correlate_N;

        try {
            epCompiled_Unlabeled = compiler.compile(epl_Unlabeled, arguments);
            deployment_Unlabeled = deploymentService.deploy(epCompiled_Unlabeled);

            epCompiled_Labeled = compiler.compile(epl_Labeled, arguments);
            deployment_Labeled = deploymentService.deploy(epCompiled_Labeled);

            epCompiled_Temp = compiler.compile(epl_Temp, arguments);
            deployment_Temp = deploymentService.deploy(epCompiled_Temp);

            epCompiled_get_case_ID = compiler.compile(epl_get_case_ID, arguments);
            deployment_get_case_ID = deploymentService.deploy(epCompiled_get_case_ID);

            epCompiled_start_case_A = compiler.compile(epl_start_case_A, arguments);
            deployment_start_case_A = deploymentService.deploy(epCompiled_start_case_A);

            epCompiled_TE_B = compiler.compile(epl_TE_B, arguments);
            deployment_TE_B = deploymentService.deploy(epCompiled_TE_B);

            epCompiled_TE_C = compiler.compile(epl_TE_C, arguments);
            deployment_TE_C = deploymentService.deploy(epCompiled_TE_C);

            epCompiled_TE_D = compiler.compile(epl_TE_D, arguments);
            deployment_TE_D = deploymentService.deploy(epCompiled_TE_D);

            epCompiled_TE_E = compiler.compile(epl_TE_E, arguments);
            deployment_TE_E = deploymentService.deploy(epCompiled_TE_E);

            epCompiled_TE_F = compiler.compile(epl_TE_F, arguments);
            deployment_TE_F = deploymentService.deploy(epCompiled_TE_F);

            epCompiled_TE_G = compiler.compile(epl_TE_G, arguments);
            deployment_TE_G = deploymentService.deploy(epCompiled_TE_G);

            epCompiled_TE_H = compiler.compile(epl_TE_H, arguments);
            deployment_TE_H = deploymentService.deploy(epCompiled_TE_H);

            epCompiled_TE_I = compiler.compile(epl_TE_I, arguments);
            deployment_TE_I = deploymentService.deploy(epCompiled_TE_I);

            epCompiled_TE_J = compiler.compile(epl_TE_J, arguments);
            deployment_TE_J = deploymentService.deploy(epCompiled_TE_J);

            epCompiled_TE_L = compiler.compile(epl_TE_L, arguments);
            deployment_TE_L = deploymentService.deploy(epCompiled_TE_L);

            epCompiled_TE_M = compiler.compile(epl_TE_M, arguments);
            deployment_TE_M = deploymentService.deploy(epCompiled_TE_M);

            epCompiled_TE_N = compiler.compile(epl_TE_N, arguments);
            deployment_TE_N = deploymentService.deploy(epCompiled_TE_N);

            CompilerArguments arguments2 = new CompilerArguments(configuration);
            arguments2.getPath().add(runtime.getRuntimePath());

            epCompiled_Correlate_B = EPCompilerProvider.getCompiler().compile(epl_Correlate_B, arguments2);
            deployment_Correlate_B = deploymentService.deploy(epCompiled_Correlate_B);

            epCompiled_Correlate_C = EPCompilerProvider.getCompiler().compile(epl_Correlate_C, arguments2);
            deployment_Correlate_C = deploymentService.deploy(epCompiled_Correlate_C);

            epCompiled_Correlate_D = EPCompilerProvider.getCompiler().compile(epl_Correlate_D, arguments2);
            deployment_Correlate_D = deploymentService.deploy(epCompiled_Correlate_D);

            epCompiled_Correlate_E = EPCompilerProvider.getCompiler().compile(epl_Correlate_E, arguments2);
            deployment_Correlate_E = deploymentService.deploy(epCompiled_Correlate_E);

            epCompiled_Correlate_F = EPCompilerProvider.getCompiler().compile(epl_Correlate_F, arguments2);
            deployment_Correlate_F = deploymentService.deploy(epCompiled_Correlate_F);

            epCompiled_Correlate_G = EPCompilerProvider.getCompiler().compile(epl_Correlate_G, arguments2);
            deployment_Correlate_G = deploymentService.deploy(epCompiled_Correlate_G);

            epCompiled_Correlate_H = EPCompilerProvider.getCompiler().compile(epl_Correlate_H, arguments2);
            deployment_Correlate_H = deploymentService.deploy(epCompiled_Correlate_H);

            epCompiled_Correlate_I = EPCompilerProvider.getCompiler().compile(epl_Correlate_I, arguments2);
            deployment_Correlate_I = deploymentService.deploy(epCompiled_Correlate_I);

            epCompiled_Correlate_J = EPCompilerProvider.getCompiler().compile(epl_Correlate_J, arguments2);
            deployment_Correlate_J = deploymentService.deploy(epCompiled_Correlate_J);

            epCompiled_Correlate_L = EPCompilerProvider.getCompiler().compile(epl_Correlate_L, arguments2);
            deployment_Correlate_L = deploymentService.deploy(epCompiled_Correlate_L);

            epCompiled_Correlate_M = EPCompilerProvider.getCompiler().compile(epl_Correlate_M, arguments2);
            deployment_Correlate_M = deploymentService.deploy(epCompiled_Correlate_M);

            epCompiled_Correlate_N = EPCompilerProvider.getCompiler().compile(epl_Correlate_N, arguments2);
            deployment_Correlate_N = deploymentService.deploy(epCompiled_Correlate_N);

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
        

        //EPStatement statement_Unlabeled = runtime.getDeploymentService().getStatement(deployment_Unlabeled.getDeploymentId(), "Unlabeled");
        EPStatement statement_Labeled = runtime.getDeploymentService().getStatement(deployment_Labeled.getDeploymentId(), "Labeled");
        /*
        EPStatement statement_Temp = runtime.getDeploymentService().getStatement(deployment_Temp.getDeploymentId(), "Temp");
        EPStatement statement_get_case_ID = runtime.getDeploymentService().getStatement(deployment_get_case_ID.getDeploymentId(), "get_case_ID");
        EPStatement statement_start_case_A = runtime.getDeploymentService().getStatement(deployment_start_case_A.getDeploymentId(), "start_case_A");
        EPStatement statement_TE_B = runtime.getDeploymentService().getStatement(deployment_TE_B.getDeploymentId(), "TE_B");
        EPStatement statement_TE_C = runtime.getDeploymentService().getStatement(deployment_TE_C.getDeploymentId(), "TE_C");
        EPStatement statement_TE_D = runtime.getDeploymentService().getStatement(deployment_TE_D.getDeploymentId(), "TE_D");
        EPStatement statement_TE_E = runtime.getDeploymentService().getStatement(deployment_TE_E.getDeploymentId(), "TE_E");
        EPStatement statement_TE_F = runtime.getDeploymentService().getStatement(deployment_TE_F.getDeploymentId(), "TE_F");
        EPStatement statement_TE_G = runtime.getDeploymentService().getStatement(deployment_TE_G.getDeploymentId(), "TE_G");
        EPStatement statement_TE_H = runtime.getDeploymentService().getStatement(deployment_TE_H.getDeploymentId(), "TE_H");
        EPStatement statement_TE_I = runtime.getDeploymentService().getStatement(deployment_TE_I.getDeploymentId(), "TE_I");
        EPStatement statement_TE_J = runtime.getDeploymentService().getStatement(deployment_TE_J.getDeploymentId(), "TE_J");
        EPStatement statement_TE_L = runtime.getDeploymentService().getStatement(deployment_TE_L.getDeploymentId(), "TE_L");
        EPStatement statement_TE_M = runtime.getDeploymentService().getStatement(deployment_TE_M.getDeploymentId(), "TE_M");
        EPStatement statement_TE_N = runtime.getDeploymentService().getStatement(deployment_TE_N.getDeploymentId(), "TE_N");

        EPStatement statement_Correlate_B = runtime.getDeploymentService().getStatement(deployment_Correlate_B.getDeploymentId(), "Correlate_B");
        EPStatement statement_Correlate_C = runtime.getDeploymentService().getStatement(deployment_Correlate_C.getDeploymentId(), "Correlate_C");
        EPStatement statement_Correlate_D = runtime.getDeploymentService().getStatement(deployment_Correlate_D.getDeploymentId(), "Correlate_D");
        EPStatement statement_Correlate_E = runtime.getDeploymentService().getStatement(deployment_Correlate_E.getDeploymentId(), "Correlate_E");
        EPStatement statement_Correlate_F = runtime.getDeploymentService().getStatement(deployment_Correlate_F.getDeploymentId(), "Correlate_F");
        EPStatement statement_Correlate_G = runtime.getDeploymentService().getStatement(deployment_Correlate_G.getDeploymentId(), "Correlate_G");
        EPStatement statement_Correlate_H = runtime.getDeploymentService().getStatement(deployment_Correlate_H.getDeploymentId(), "Correlate_H");
        EPStatement statement_Correlate_I = runtime.getDeploymentService().getStatement(deployment_Correlate_I.getDeploymentId(), "Correlate_I");
        EPStatement statement_Correlate_J = runtime.getDeploymentService().getStatement(deployment_Correlate_J.getDeploymentId(), "Correlate_J");
        EPStatement statement_Correlate_L = runtime.getDeploymentService().getStatement(deployment_Correlate_L.getDeploymentId(), "Correlate_L");
        EPStatement statement_Correlate_M = runtime.getDeploymentService().getStatement(deployment_Correlate_M.getDeploymentId(), "Correlate_M");
        EPStatement statement_Correlate_N = runtime.getDeploymentService().getStatement(deployment_Correlate_N.getDeploymentId(), "Correlate_N");

        
        statement_Unlabeled.addListener((newData, oldData, s, r) -> {
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            System.out.println("UnlabeledEvent ["+ activity+" , "+timestamp+"]");//+caseID + " , "
        });
         */

        statement_Labeled.addListener((newData, oldData, s, r) -> {
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            double probability = (double) newData[0].get("probability");
            Date ts = new Date(timestamp);
	        Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	        try {
		    	frLabeled.append(caseID + "," + activity+","+format.format(ts)+","+probability+"\n");
		    	frLabeled.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    //System.out.println("*** LabeledEvent ["+caseID + " , " + activity+" , "+timestamp+" , "+probability+"]");
        });
        /*
        statement_Temp.addListener((newData, oldData, s, r) -> {
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            System.out.println("--- TempEvent ["+caseID + " , " + activity+" , "+timestamp+"]");
        });

        statement_TE_B.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_B-instance "+newData[0].getUnderlying()); }); 
        statement_TE_C.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_C-instance "+newData[0].getUnderlying()); }); 
        statement_TE_D.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_D-instance "+newData[0].getUnderlying()); }); 
        statement_TE_E.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_E-instance "+newData[0].getUnderlying()); }); 
        statement_TE_F.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_F-instance "+newData[0].getUnderlying()); }); 
        statement_TE_G.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_G-instance "+newData[0].getUnderlying()); }); 
        statement_TE_H.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_H-instance "+newData[0].getUnderlying()); }); 
        statement_TE_I.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_I-instance "+newData[0].getUnderlying()); }); 
        statement_TE_J.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_J-instance "+newData[0].getUnderlying()); }); 
        statement_TE_L.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_L-instance "+newData[0].getUnderlying()); }); 
        statement_TE_M.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_M-instance "+newData[0].getUnderlying()); }); 
        statement_TE_N.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_N-instance "+newData[0].getUnderlying()); }); 

        statement_Correlate_B.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_B-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_C.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_C-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_D.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_D-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_E.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_E-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_F.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_F-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_G.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_G-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_H.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_H-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_I.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_I-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_J.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_J-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_L.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_L-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_M.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_M-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_N.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_N-instance "+newData[0].getUnderlying()); }); 
         */
        
        String filePath = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation\\input_ts.csv";
        new UnlabeledEventSource(filePath, 100, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent
        frLabeled.close();
        date = new Date();  
        System.out.println("End time "+formatter.format(date));
    }

}