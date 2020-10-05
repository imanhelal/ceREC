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

public class Runner {

    public static void main(String[] args) throws InterruptedException {


        EPCompiler compiler = EPCompilerProvider.getCompiler();

        Configuration configuration = new Configuration();
        configuration.getRuntime().getExecution().setPrioritized(true);//http://esper.espertech.com/release-8.5.0/reference-esper/html_single/index.html#configuration-runtime-execution

        // Here you register your event types
        configuration.getCommon().addEventType(Cases.class);
        configuration.getCommon().addEventType(UnlabeledEvent.class);
        configuration.getCommon().addEventType(LabeledEvent.class);
        configuration.getCommon().addEventType(TempEvent.class);
  //      configuration.getCommon().addEventType(TempEventWin.class);

        //Only a test statement for the input -- not used elsewhere.
        String epl_select_UnlabeledEvent = //"create schema UnlabeledEvent(caseID long, activity string, timestamp long, probability double);\n"+
                " @Name('Unlabeled') select * from UnlabeledEvent;";//caseID, activity, timestamp, probability

        String epl_select_LabeledEvent = "@Name('Labeled') select * from LabeledEvent;";

        //an intermediate stream to check the possible labels for incoming unlabeled events. These should be temporary and need to be deleted after usage.
        String epl_select_TempEvent = "@Name('TempEvent') select * from TempEvent ;";

        String epl_create_named_window_temp_Event = "create window TempEventWin.win:time_batch(1 sec) as TempEvent;";
        //*
        //This query is responsible for retrieving the last caseID in the list of cases. This number+1 is used to initialize the new case with the incoming event 'A'
        String epl_GetCaseID = "@Name('get_case_ID') "+
                " insert into Cases  select (select coalesce(max(caseID)+1, 1) "+
                " from  Cases )  as caseID from UnlabeledEvent "+
                " where  activity = 'A';  \n"+
                " @Audit select max(caseID) from Cases; ";


        //Label the incoming unlabeled event 'A' in a new case with probability 1.0. This is assigned by default. Since this is the entry event of the process.
        String epl_StartCaseA = "@Name('start_case_A') "+
                " insert into LabeledEvent (caseID, activity, timestamp, probability) "+
                " select (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0 "+
                " from UnlabeledEvent as UE where UE.activity = 'A';";

        /*String epl_StartCaseA = "@Name('start_case_A')  "+
        		" insert into LabeledEvent (caseID, activity, timestamp, probability) "+
        		" select (select max(caseID) from Cases) as caseID, UE.activity, UE.timestamp, 1.0 "+
        		" from UnlabeledEvent as UE where UE.activity = 'A';";
        //*/
        /*
        String epl_setStartCaseA = "@Name('set_start_case_A') "+
        		"insert into Cases  select (select coalesce(max(C.caseID)+1, 1) "+
        		" from  Cases as C)  as caseID from UnlabeledEvent "+
        		" where  activity = 'A'; \n"+
        		"@Name('Label_case_A') insert into LabeledEvent (caseID, activity, timestamp, probability) "+
        		" select (select max(caseID) from Cases) as caseID, UE.activity, UE.timestamp, 1.0 "+
        		" from UnlabeledEvent as UE where UE.activity = 'A'; \n"+
        		" select caseID from Cases; \n";
        */

        //set of insert statements into TempEvent
        String epl_TE_B = "@Priority(5) @Name('TE_B') "+
                "insert into TempEvent (caseID, activity, timestamp) "+
                "select distinct pred.caseID, 'B' as activity, succ.timestamp as timestamp"+
                " from pattern [  (every pred=LabeledEvent(activity='A' OR activity='N') ) -> every succ=UnlabeledEvent(activity='B') ]#time(30 sec) "+
                " where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ;";

        String epl_TE_C = "@Priority(90) @Name('TE_C') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'C', succ.timestamp  "+
                "from pattern [ every pred=LabeledEvent(activity='B') ->  every succ=UnlabeledEvent(activity='C') ]#time(30 sec)"+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";

        String epl_Reinsert_C = "@Priority(10) @Name('Reinsert_C') "+
                "insert into UnlabeledEvent (caseID, activity, timestamp, probability) select -1, 'C', succ.timestamp,0  "+
                "from pattern [ every pred=UnlabeledEvent(activity='B') and not lab=LabeledEvent(activity='B', timestamp=pred.timestamp) -> every succ=UnlabeledEvent(activity='C') ]#time(30 sec)  "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";

        String epl_TE_D = "@Priority(5) @Name('TE_D') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'D', succ.timestamp  "+
                "from pattern [ every pred=LabeledEvent(activity='B') -> every succ=UnlabeledEvent(activity='D') ]#time(30 sec) "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";

        String epl_TE_E = "@Priority(5) @Name('TE_E') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'E', succ.timestamp  "+
                "from pattern [ (every pred=LabeledEvent(activity='D' OR activity='H'))-> every succ=UnlabeledEvent(activity='E')  ]#time(30 sec) "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";

        String epl_TE_F = "@Priority(5) @Name('TE_F') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'F', succ.timestamp  "+
                "from pattern [ every pred=LabeledEvent(activity='E') -> every succ=UnlabeledEvent(activity='F') ]#time(30 sec)  "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";

        String epl_TE_G = "@Priority(5) @Name('TE_G') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'G', succ.timestamp  "+
                "from pattern [ every pred=LabeledEvent(activity='E') -> every succ=UnlabeledEvent(activity='G')  ]#time(30 sec)  "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";

        String epl_TE_H = "@Priority(5) @Name('TE_H') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'H', succ.timestamp  "+
                "from pattern [  every pred=LabeledEvent(activity='F') -> every succ=UnlabeledEvent(activity='H') ]#time(30 sec)  "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";

        String epl_TE_I = "@Priority(5) @Name('TE_I') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'I', succ.timestamp  "+
                "from pattern [ (every pred=LabeledEvent(activity='C') -> every succ=UnlabeledEvent(activity='I') ) where timer:within(30 sec) ]  "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=90000 ; ";

        String epl_TE_J = "@Priority(5) @Name('TE_J') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'J', succ.timestamp  "+
                "from pattern [ every pred=LabeledEvent(activity='C') -> every succ=UnlabeledEvent(activity='J') ]#time(30 sec)  "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=90000 ; ";

        String epl_TE_L = "@Priority(5) @Name('TE_L') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'L', succ.timestamp  "+
                "from pattern [ (every pred=LabeledEvent(activity='I') AND every inter=LabeledEvent(activity='J') ) -> every succ=UnlabeledEvent(activity='L')  ]#time(30 sec)   "+
                "where pred.caseID = inter.caseID and (succ.timestamp - max(pred.timestamp,inter.timestamp)) >= 1 and (succ.timestamp - max(pred.timestamp,inter.timestamp)) <=30000 ; ";

        String epl_TE_M = "@Priority(5) @Name('TE_M') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'M', succ.timestamp   "+
                "from pattern [ every pred=LabeledEvent(activity='L') -> every succ=UnlabeledEvent(activity='M') ]#time(30 sec)  "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";

        String epl_TE_N = "@Priority(5) @Name('TE_N') "+
                "insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'N', succ.timestamp  "+
                "from pattern [ every pred=LabeledEvent(activity='L') -> every succ=UnlabeledEvent(activity='N') ]#time(30 sec)  "+
                "where succ.timestamp - pred.timestamp >= 1 and succ.timestamp - pred.timestamp <=30000 ; ";


//        String epl_Correlate_B = "@Priority(20) @Name('Correlate_B') "+
//                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
//                "select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  " +
//                "from TempEvent.win:ext_timed_batch(timestamp,1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probability "+
//                "from TempEvent.win:ext_timed_batch(timestamp,1 sec) as T  "+
//                "where activity = 'B'  "+
//                "group by caseID "+
//                "order by timestamp;";

        String epl_Correlate_B = "@Priority(20) @Name('Correlate_B') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ((select count(caseID)  " +
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probability "+
                "from TempEvent.win:time_batch(1 sec) as T  "+
                "where activity = 'B'  "+
                "group by caseID "+
                "order by timestamp;";


        //(1 /count(all caseID, group_by:timestamp))

//        String epl_Correlate_B = "@Name('Correlate_B') "+
//        		"insert into LabeledEvent (caseID, activity , timestamp, probability) "+
//        		"select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , 1 as probability "+
//        		"from TempEvent.win:time_batch(1 sec)   as T        "+
//        		"where activity = 'B'  "+
//        		"group by caseID "+
//        		"order by timestamp; ";

        String epl_Correlate_C = "@Priority(20) @Name('Correlate_C') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID)  "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T        "+
                "where activity = 'C'  "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_D = "@Priority(20) @Name('Correlate_D') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID)  "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T        "+
                "where activity = 'D' "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_E = "@Priority(20) @Name('Correlate_E') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID)  "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T        "+
                "where activity = 'E'  "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_F = "@Priority(20) @Name('Correlate_F') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID) "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T        "+
                "where activity = 'F'  "+
                "group by caseID "+
                "order by timestamp;  ";

        String epl_Correlate_G = "@Priority(20) @Name('Correlate_G') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID) "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T       "+
                "where activity = 'G' "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_H = "@Priority(20) @Name('Correlate_H') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID) "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T       "+
                "where activity = 'H' "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_I = "@Priority(20) @Name('Correlate_I') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID) "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T       "+
                "where activity = 'I' "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_J = "@Priority(20) @Name('Correlate_J') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID) "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T       "+
                "where activity = 'J' "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_L = "@Priority(20) @Name('Correlate_L') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID) "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T       "+
                "where activity = 'L' "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_M = "@Priority(20) @Name('Correlate_M') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID) "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T       "+
                "where activity = 'M' "+
                "group by caseID "+
                "order by timestamp; ";

        String epl_Correlate_N = "@Priority(20) @Name('Correlate_N') "+
                "insert into LabeledEvent (caseID, activity , timestamp, probability) "+
                "select distinct caseID as c, last(activity) as act, max(timestamp) as ts , ((select count(caseID) "+
                "from TempEvent.win:time_batch(1 sec) where caseID = T.caseID) /count(all caseID, group_by:timestamp)) as probX "+
                "from TempEvent.win:time_batch(1 sec)   as T   "+
                "where activity = 'N' "+
                "group by caseID "+
                "order by timestamp; ";



        // Get a runtime
        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
        EPDeploymentService  deploymentService = runtime.getDeploymentService();




        // By Ahmed Awad
        CompilerArguments arguments = new CompilerArguments(configuration);
        arguments.getPath().add(runtime.getRuntimePath());



        EPCompiled epCompiled_unlabeled;
        //*
        EPCompiled epCompiled_getCaseID;
        EPCompiled epCompiled_TempEvent_Named_Window;
        EPCompiled epCompiled_startCaseA;
        //*/
        EPCompiled epCompiled_labeled;
        EPCompiled epCompiled_Temp;

        //EPCompiled epComiled_setStartCaseA;


        EPCompiled epCompiled_TE_B;
        //*
        EPCompiled epCompiled_TE_C;
        EPCompiled epCompiled_Reinsert_C;
        EPCompiled epCompiled_TE_D;
        EPCompiled epCompiled_TE_E;
        EPCompiled epCompiled_TE_F;
        EPCompiled epCompiled_TE_G;
        EPCompiled epCompiled_TE_H;
        EPCompiled epCompiled_TE_I;
        EPCompiled epCompiled_TE_J;
        EPCompiled epCompiled_TE_L;
        EPCompiled epCompiled_TE_M;
        EPCompiled epCompiled_TE_N;

        EPCompiled epCompiled_Correlate_B;
        EPCompiled epCompiled_Correlate_C;
        EPCompiled epCompiled_Correlate_D;
        EPCompiled epCompiled_Correlate_E;
        EPCompiled epCompiled_Correlate_F;
        EPCompiled epCompiled_Correlate_G;
        EPCompiled epCompiled_Correlate_H;
        EPCompiled epCompiled_Correlate_I;
        EPCompiled epCompiled_Correlate_J;
        EPCompiled epCompiled_Correlate_L;
        EPCompiled epCompiled_Correlate_M;
        EPCompiled epCompiled_Correlate_N;

        EPDeployment deployment_unlabeled;
        //*
        EPDeployment deployment_getCaseID;
        EPDeployment deployment_create_window_epl;
        EPDeployment deployment_startCaseA;
        //*/
        EPDeployment deployment_labeled;
        EPDeployment deployment_Temp;

        //EPDeployment deployment_setStartCaseA;

        EPDeployment deployment_TE_B ;

        //*
        EPDeployment deployment_TE_C ;
        EPDeployment deployment_Reinsert_C;
        EPDeployment deployment_TE_D ;
        EPDeployment deployment_TE_E ;
        EPDeployment deployment_TE_F ;
        EPDeployment deployment_TE_G ;
        EPDeployment deployment_TE_H ;
        EPDeployment deployment_TE_I ;
        EPDeployment deployment_TE_J ;
        EPDeployment deployment_TE_L ;
        EPDeployment deployment_TE_M ;
        EPDeployment deployment_TE_N ;

        EPDeployment deployment_Correlate_B ;
        EPDeployment deployment_Correlate_C ;
        EPDeployment deployment_Correlate_D ;
        EPDeployment deployment_Correlate_E ;
        EPDeployment deployment_Correlate_F ;
        EPDeployment deployment_Correlate_G ;
        EPDeployment deployment_Correlate_H ;
        EPDeployment deployment_Correlate_I ;
        EPDeployment deployment_Correlate_J ;
        EPDeployment deployment_Correlate_L ;
        EPDeployment deployment_Correlate_M ;
        EPDeployment deployment_Correlate_N ;
        //*/
        try {
            epCompiled_unlabeled = compiler.compile(epl_select_UnlabeledEvent, arguments);
            deployment_unlabeled = deploymentService.deploy(epCompiled_unlabeled);

            epCompiled_getCaseID = compiler.compile(epl_GetCaseID, arguments);
            deployment_getCaseID = deploymentService.deploy(epCompiled_getCaseID);

            epCompiled_TempEvent_Named_Window = compiler.compile(epl_create_named_window_temp_Event,arguments);
            deployment_create_window_epl = runtime.getDeploymentService().deploy(epCompiled_TempEvent_Named_Window);

            epCompiled_startCaseA = compiler.compile(epl_StartCaseA, arguments);
            deployment_startCaseA = deploymentService.deploy(epCompiled_startCaseA);


            epCompiled_labeled = compiler.compile(epl_select_LabeledEvent, arguments);
            deployment_labeled = deploymentService.deploy(epCompiled_labeled);

            epCompiled_Temp = compiler.compile(epl_select_TempEvent, arguments);
            deployment_Temp = deploymentService.deploy(epCompiled_Temp);

            epCompiled_TE_B = compiler.compile(epl_TE_B, arguments);
            deployment_TE_B = deploymentService.deploy(epCompiled_TE_B);


            epCompiled_TE_C = compiler.compile(epl_TE_C, arguments);
            deployment_TE_C = deploymentService.deploy(epCompiled_TE_C);

            //epCompiled_Reinsert_C = compiler.compile(epl_Reinsert_C, args_TE_C);

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

           // System.out.println(runtime.getRuntimePath().toString());
            CompilerArguments arguments2 = new CompilerArguments(configuration);
            arguments2.getPath().add(runtime.getRuntimePath());

            epCompiled_Correlate_B = EPCompilerProvider.getCompiler().compile(epl_Correlate_B, arguments2);
            deployment_Correlate_B = deploymentService.deploy(epCompiled_Correlate_B);

            epCompiled_Correlate_C = compiler.compile(epl_Correlate_C, arguments);
            deployment_Correlate_C = deploymentService.deploy(epCompiled_Correlate_C);

            epCompiled_Correlate_D = compiler.compile(epl_Correlate_D, arguments);
            deployment_Correlate_D = deploymentService.deploy(epCompiled_Correlate_D);

            epCompiled_Correlate_E = compiler.compile(epl_Correlate_E, arguments);
            deployment_Correlate_E = deploymentService.deploy(epCompiled_Correlate_E);

            epCompiled_Correlate_F = compiler.compile(epl_Correlate_F, arguments);
            deployment_Correlate_F = deploymentService.deploy(epCompiled_Correlate_F);

            epCompiled_Correlate_G = compiler.compile(epl_Correlate_G, arguments);
            deployment_Correlate_G = deploymentService.deploy(epCompiled_Correlate_G);

            epCompiled_Correlate_H = compiler.compile(epl_Correlate_H, arguments);
            deployment_Correlate_H = deploymentService.deploy(epCompiled_Correlate_H);

            epCompiled_Correlate_I = compiler.compile(epl_Correlate_I, arguments);
            deployment_Correlate_I = deploymentService.deploy(epCompiled_Correlate_I);

            epCompiled_Correlate_J = compiler.compile(epl_Correlate_J, arguments);
            deployment_Correlate_J = deploymentService.deploy(epCompiled_Correlate_J);

            epCompiled_Correlate_L = compiler.compile(epl_Correlate_L, arguments);
            deployment_Correlate_L = deploymentService.deploy(epCompiled_Correlate_L);

            epCompiled_Correlate_M = compiler.compile(epl_Correlate_M, arguments);
            deployment_Correlate_M = deploymentService.deploy(epCompiled_Correlate_M);

            epCompiled_Correlate_N = compiler.compile(epl_Correlate_N, arguments);
            deployment_Correlate_N = deploymentService.deploy(epCompiled_Correlate_N);
            //*/
        } catch (EPCompileException | EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        EPEventService eventService = runtime.getEventService();
        eventService.clockExternal();

        eventService.advanceTime(0);
        System.out.println("System clock "+eventService.getCurrentTime());




        EPStatement statement_unlabeled = runtime.getDeploymentService().getStatement(deployment_unlabeled.getDeploymentId(), "Unlabeled");
        //*
        EPStatement statement_get_case_ID = runtime.getDeploymentService().getStatement(deployment_getCaseID.getDeploymentId(), "get_case_ID");
        EPStatement statement_StartCaseA = runtime.getDeploymentService().getStatement(deployment_startCaseA.getDeploymentId(), "start_case_A");
        //*/
        EPStatement statement_labeled = runtime.getDeploymentService().getStatement(deployment_labeled.getDeploymentId(), "Labeled");
        EPStatement statement_Temp = runtime.getDeploymentService().getStatement(deployment_Temp.getDeploymentId(), "TempEvent");

        //EPStatement statement_SetStartCaseA = runtime.getDeploymentService().getStatement(deployment_setStartCaseA.getDeploymentId(), "set_start_case_A");



        EPStatement statement_TE_B = runtime.getDeploymentService().getStatement(deployment_TE_B.getDeploymentId(), "TE_B");
        //*
        EPStatement statement_TE_C = runtime.getDeploymentService().getStatement(deployment_TE_C.getDeploymentId(), "TE_C");
    //    EPStatement statement_Reinsert_C = runtime.getDeploymentService().getStatement(deployment_Reinsert_C.getDeploymentId(),"Reinsert_C");
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
        //*/

        statement_unlabeled.addListener((newData, oldData, s, r) -> {
            //long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            System.out.println("UnlabeledEvent ["+ activity+" , "+timestamp+"]");//+caseID + " , "
        });


        statement_labeled.addListener((newData, oldData, s, r) -> {
            //System.out.println("Epl labeled "+epl_select_LabeledEvent);
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            double probability = (double) newData[0].get("probability");
            System.out.println("*** LabeledEvent ["+caseID + " , " + activity+" , "+timestamp+" , "+probability+"]");
        });

        statement_Temp.addListener((newData, oldData, s, r) -> {
            //System.out.println("Epl temp "+epl_select_TempEvent);
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            System.out.println("--- TempEvent ["+caseID + " , " + activity+" , "+timestamp+"]");
        });
        /*
        statement_SetStartCaseA.addListener((newData, oldData, s, r) -> {
            long caseID = (long) newData[0].get("caseID");
            System.out.println("Create new case = ["+caseID +"]");
        });
        //*/
        /*
        statement_get_case_ID.addListener((newData, oldData, s, r) -> {
            long caseID = (long) newData[0].get("caseID");
            System.out.println("Create new case = ["+caseID +"]");
        });
        //*/
        //statement_get_case_ID.addListener((newData, oldData, s, r) -> {System.out.println("Open-new-case------- "+newData[0].getUnderlying()); });

        statement_StartCaseA.addListener((newData, oldData, s, r) -> {
            //System.out.println("Case-A-statement "+epl_StartCaseA);
            //System.out.println("Case-A-instance "+newData[0].getUnderlying());
        });
        /**/


        //*

        //statement_TE_B.addListener((newData, oldData, s, r) -> {System.out.println("Event-B-instance "+newData[0].getUnderlying()); });

        statement_TE_B.addListener((newData, oldData, s, r) -> {
            //System.out.println("Epl temp "+epl_select_TempEvent);
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            double probability = (double) newData[0].get("probability");
            System.out.println("--- TE_B ["+caseID + " , " + activity+" , "+timestamp+" , "+probability+"]");
        });

        statement_TE_C.addListener((newData, oldData, s, r) -> {System.out.println("Event-C-instance "+newData[0].getUnderlying()); });

 //       statement_Reinsert_C.addListener((newData, oldData,s, r) ->{System.out.println("Reinserting unlabeled event C");});
        statement_TE_D.addListener((newData, oldData, s, r) -> {System.out.println("Event-D-instance "+newData[0].getUnderlying()); });
        statement_TE_E.addListener((newData, oldData, s, r) -> {System.out.println("Event-E-instance "+newData[0].getUnderlying()); });
        statement_TE_F.addListener((newData, oldData, s, r) -> {System.out.println("Event-F-instance "+newData[0].getUnderlying()); });
        statement_TE_G.addListener((newData, oldData, s, r) -> {System.out.println("Event-G-instance "+newData[0].getUnderlying()); });
        statement_TE_H.addListener((newData, oldData, s, r) -> {System.out.println("Event-H-instance "+newData[0].getUnderlying()); });
        statement_TE_I.addListener((newData, oldData, s, r) -> {System.out.println("Event-I-instance "+newData[0].getUnderlying()); });
        statement_TE_J.addListener((newData, oldData, s, r) -> {System.out.println("Event-J-instance "+newData[0].getUnderlying()); });
        statement_TE_L.addListener((newData, oldData, s, r) -> {System.out.println("Event-L-instance "+newData[0].getUnderlying()); });
        statement_TE_M.addListener((newData, oldData, s, r) -> {System.out.println("Event-M-instance "+newData[0].getUnderlying()); });
        statement_TE_N.addListener((newData, oldData, s, r) -> {System.out.println("Event-N-instance "+newData[0].getUnderlying()); });

        //statement_Correlate_B.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-B "+newData[0].getUnderlying()); });
        statement_Correlate_B.addListener((newData, oldData, s, r) -> {
            //System.out.println("Epl temp "+epl_select_TempEvent);
            long caseID = (long) newData[0].get("caseID");
            String activity = (String) newData[0].get("activity");
            long timestamp = (long) newData[0].get("timestamp");
            double probability = (double) newData[0].get("probability");
            System.out.println("--- Correlate_B ["+caseID + " , " + activity+" , "+timestamp+" , "+probability+"]");
        });

        statement_Correlate_C.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-C "+newData[0].getUnderlying()); });
        statement_Correlate_D.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-D "+newData[0].getUnderlying()); });
        statement_Correlate_E.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-E "+newData[0].getUnderlying()); });
        statement_Correlate_F.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-F "+newData[0].getUnderlying()); });
        statement_Correlate_G.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-G "+newData[0].getUnderlying()); });
        statement_Correlate_H.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-H "+newData[0].getUnderlying()); });
        statement_Correlate_I.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-I "+newData[0].getUnderlying()); });
        statement_Correlate_J.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-J "+newData[0].getUnderlying()); });
        statement_Correlate_L.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-L "+newData[0].getUnderlying()); });
        statement_Correlate_M.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-M "+newData[0].getUnderlying()); });
        statement_Correlate_N.addListener((newData, oldData, s, r) -> {System.out.println("Labeled-N "+newData[0].getUnderlying()); });
        //*/



        //get generic reference to the record.
        //statement.addListener((newData, oldData, s, r) -> {System.out.println(newData[0].getUnderlying()); });



        String filePath = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation\\input2.csv";
        new UnlabeledEventSource(filePath, 35, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent

    }

}