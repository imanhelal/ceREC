package eg.cu.fci.is.correlator;

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

public class Runner_BPI2013ClosedOld {

    public static void main(String[] args) throws InterruptedException {


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

        String epl_get_case_ID =" @Name('get_case_ID')  insert into Cases  select (select coalesce(max(caseID)+1, 1)  from  Cases )  as caseID from UnlabeledEvent  where  activity in ('accepted');  ";

        String epl_start_case_accepted =" @Name('start_case_accepted')  insert into LabeledEvent (caseID, activity, timestamp, probability)  select (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0  from UnlabeledEvent as UE where UE.activity = 'accepted'; ";

        String epl_TE_accepted =" @Priority(5) @Name('TE_accepted') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'accepted' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity='accepted' or activity='queued')) -> every succ=UnlabeledEvent(activity='accepted') ]#time(86398 sec)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86398000 ;";

        String epl_TE_queued =" @Priority(5) @Name('TE_queued') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'queued' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'accepted')) -> every succ=UnlabeledEvent(activity='queued') ]#time(86078 sec)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86078000 ;";

        String epl_TE_unmatched =" @Priority(5) @Name('TE_unmatched') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'unmatched' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity = 'accepted')) -> every succ=UnlabeledEvent(activity='unmatched') ]#time(75866 sec)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=75866000 ;";

        String epl_TE_completed =" @Priority(5) @Name('TE_completed') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'completed' as activity, succ.timestamp as timestamp from pattern [(every pred=LabeledEvent(activity='accepted' or activity='unmatched')) -> every succ=UnlabeledEvent(activity='completed') ]#time(86396 sec)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86396000 ;";

        String epl_Correlate_accepted =" @Priority(20) @Name('Correlate_accepted') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'accepted'  group by caseID order by timestamp; ";

        String epl_Correlate_queued =" @Priority(20) @Name('Correlate_queued') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'queued'  group by caseID order by timestamp; ";

        String epl_Correlate_unmatched =" @Priority(20) @Name('Correlate_unmatched') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'unmatched'  group by caseID order by timestamp; ";

        String epl_Correlate_completed =" @Priority(20) @Name('Correlate_completed') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'completed'  group by caseID order by timestamp; ";

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

        try {
            epCompiled_Unlabeled = compiler.compile(epl_Unlabeled, arguments);
            deployment_Unlabeled = deploymentService.deploy(epCompiled_Unlabeled);

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

        } catch (EPCompileException | EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        EPEventService eventService = runtime.getEventService();
        eventService.clockExternal();
        eventService.advanceTime(0);
        System.out.println("System clock "+eventService.getCurrentTime());

        EPStatement statement_Unlabeled = runtime.getDeploymentService().getStatement(deployment_Unlabeled.getDeploymentId(), "Unlabeled");
        EPStatement statement_Labeled = runtime.getDeploymentService().getStatement(deployment_Labeled.getDeploymentId(), "Labeled");
        EPStatement statement_Temp = runtime.getDeploymentService().getStatement(deployment_Temp.getDeploymentId(), "Temp");
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

        statement_Unlabeled.addListener((newData, oldData, s, r) -> {
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            System.out.println("UnlabeledEvent ["+ activity+" , "+timestamp+"]");//+caseID + " , "
        });


        statement_Labeled.addListener((newData, oldData, s, r) -> {
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            double probability = (double) newData[0].get("probability");
            System.out.println("*** LabeledEvent ["+caseID + " , " + activity+" , "+timestamp+" , "+probability+"]");
        });

        statement_Temp.addListener((newData, oldData, s, r) -> {
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            System.out.println("--- TempEvent ["+caseID + " , " + activity+" , "+timestamp+"]");
        });

        statement_TE_accepted.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_accepted-instance "+newData[0].getUnderlying()); }); 
        statement_TE_queued.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_queued-instance "+newData[0].getUnderlying()); }); 
        statement_TE_unmatched.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_unmatched-instance "+newData[0].getUnderlying()); }); 
        statement_TE_completed.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_completed-instance "+newData[0].getUnderlying()); }); 

        statement_Correlate_accepted.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_accepted-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_queued.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_queued-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_unmatched.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_unmatched-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_completed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_completed-instance "+newData[0].getUnderlying()); }); 

        String filePath = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation\\BPI2013Closed_unLabeled.csv";
        new UnlabeledEventSource(filePath, 50, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent

    }

}