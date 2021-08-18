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

public class Runner_BPI2017_latency {

	 private static long initTime =0; 

    public static void main(String[] args) throws InterruptedException, IOException {

    	System.out.println(Runtime.getRuntime().maxMemory());
    	//-Xmx5120m (5GB)
    	
    	System.out.println("***** APP START *****"); 
    	String labeledEvents = "C:\\IH\\eclipse-workspace\\CEPForEventCorrelation\\BPI2017_labeled_10_heur2.csv";
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

        String epl_get_case_ID =" @Name('get_case_ID')  insert into Cases  select (select coalesce(max(caseID)+1, 1)  from  Cases )  as caseID from UnlabeledEvent  where  activity in ('a_create application');  ";

        String epl_start_case_a_create_application =" @Name('start_case_a_create_application')  insert into FilterLabeledEvent (caseID, activity, timestamp, probability, initTime)  select (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0, UE.sysTime as initTime  from UnlabeledEvent as UE where UE.activity = 'a_create application'; ";

        String epl_TE_a_submitted =" @Priority(5) @Name('TE_a_submitted') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_submitted' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 'a_create application') -> every succ=UnlabeledEvent(activity='a_submitted')) where timer:within(1 sec)]#time(1 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=60;";

        String epl_TE_w_complete_application =" @Priority(5) @Name('TE_w_complete_application') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'w_complete application' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='w_complete application')) where timer:within(1922 sec)]#time(1922 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=1921920;";

        String epl_TE_w_handle_leads =" @Priority(5) @Name('TE_w_handle_leads') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'w_handle leads' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='w_handle leads')) where timer:within(252 sec)]#time(252 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=251340;";

        String epl_TE_a_concept =" @Priority(5) @Name('TE_a_concept') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_concept' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='a_concept')) where timer:within(411 sec)]#time(411 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=410760;";

        String epl_TE_a_accepted =" @Priority(5) @Name('TE_a_accepted') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_accepted' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='a_accepted')) where timer:within(2626 sec)]#time(2626 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=2625960;";

        String epl_TE_a_complete =" @Priority(5) @Name('TE_a_complete') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_complete' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='a_complete')) where timer:within(1 sec)]#time(1 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=60;";

        String epl_TE_w_assess_potential_fraud =" @Priority(5) @Name('TE_w_assess_potential_fraud') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'w_assess potential fraud' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='w_assess potential fraud')) where timer:within(7621 sec)]#time(7621 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=7620420;";

        String epl_TE_o_refused =" @Priority(5) @Name('TE_o_refused') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'o_refused' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='o_refused')) where timer:within(1 sec)]#time(1 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=60;";

        String epl_TE_w_validate_application =" @Priority(5) @Name('TE_w_validate_application') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'w_validate application' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='w_validate application')) where timer:within(7954 sec)]#time(7954 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=7953720;";

        String epl_TE_w_call_after_offers =" @Priority(5) @Name('TE_w_call_after_offers') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'w_call after offers' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='w_call after offers')) where timer:within(6389 sec)]#time(6389 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=6388680;";

        String epl_TE_a_cancelled =" @Priority(5) @Name('TE_a_cancelled') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_cancelled' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='a_cancelled')) where timer:within(12105 sec)]#time(12105 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=12104760;";

        String epl_TE_o_returned =" @Priority(5) @Name('TE_o_returned') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'o_returned' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='o_returned')) where timer:within(3019 sec)]#time(3019 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=3018060;";

        String epl_TE_a_incomplete =" @Priority(5) @Name('TE_a_incomplete') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_incomplete' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='a_incomplete')) where timer:within(1053 sec)]#time(1053 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=1052460;";

        String epl_TE_o_created =" @Priority(5) @Name('TE_o_created') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'o_created' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='o_created')) where timer:within(1 sec)]#time(1 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=60;";

        String epl_TE_a_pending =" @Priority(5) @Name('TE_a_pending') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_pending' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='a_pending')) where timer:within(1 sec)]#time(1 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=60;";

        String epl_TE_w_call_incomplete_files =" @Priority(5) @Name('TE_w_call_incomplete_files') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'w_call incomplete files' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='w_call incomplete files')) where timer:within(10553 sec)]#time(10553 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=10552200;";

        String epl_TE_w_shortened_completion =" @Priority(5) @Name('TE_w_shortened_completion') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'w_shortened completion' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='w_shortened completion')) where timer:within(262 sec)]#time(262 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=261120;";

        String epl_TE_o_sent__mail_and_online_ =" @Priority(5) @Name('TE_o_sent__mail_and_online_') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'o_sent (mail and online)' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='o_sent (mail and online)')) where timer:within(1313 sec)]#time(1313 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=1312200;";

        String epl_TE_a_validating =" @Priority(5) @Name('TE_a_validating') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_validating' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='a_validating')) where timer:within(7949 sec)]#time(7949 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=7948080;";

        String epl_TE_o_create_offer =" @Priority(5) @Name('TE_o_create_offer') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'o_create offer' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='o_create offer')) where timer:within(9683 sec)]#time(9683 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=9682440;";

        String epl_TE_o_sent__online_only_ =" @Priority(5) @Name('TE_o_sent__online_only_') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'o_sent (online only)' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='o_sent (online only)')) where timer:within(769 sec)]#time(769 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=768960;";

        String epl_TE_a_denied =" @Priority(5) @Name('TE_a_denied') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'a_denied' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='a_denied')) where timer:within(2938 sec)]#time(2938 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=2937960;";

        String epl_TE_o_accepted =" @Priority(5) @Name('TE_o_accepted') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'o_accepted' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='o_accepted')) where timer:within(4474 sec)]#time(4474 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=4473600;";

        String epl_TE_o_cancelled =" @Priority(5) @Name('TE_o_cancelled') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'o_cancelled' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='o_cancelled')) where timer:within(5860 sec)]#time(5860 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=5859420;";

        String epl_TE_w_personal_loan_collection =" @Priority(5) @Name('TE_w_personal_loan_collection') insert into TempEvent (caseID, activity, timestamp) select distinct pred.caseID, 'w_personal loan collection' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='a_create application' or activity='a_submitted' or activity='w_complete application' or activity='w_handle leads' or activity='a_concept' or activity='a_accepted' or activity='a_complete' or activity='w_assess potential fraud' or activity='o_refused' or activity='w_validate application' or activity='w_call after offers' or activity='a_cancelled' or activity='o_returned' or activity='a_incomplete' or activity='o_created' or activity='a_pending' or activity='w_call incomplete files' or activity='w_shortened completion' or activity='o_sent (mail and online)' or activity='a_validating' or activity='o_create offer' or activity='o_sent (online only)' or activity='a_denied' or activity='o_accepted' or activity='o_cancelled' or activity='w_personal loan collection )') -> every succ=UnlabeledEvent(activity='w_personal loan collection')) where timer:within(11526 sec)]#time(11526 sec)  where (succ.timestamp - pred.timestamp) >= 0 and (succ.timestamp - pred.timestamp) <=11525820;";

        String epl_Correlate_a_submitted =" @Priority(20) @Name('Correlate_a_submitted') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_submitted'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_w_complete_application =" @Priority(20) @Name('Correlate_w_complete_application') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'w_complete application'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_w_handle_leads =" @Priority(20) @Name('Correlate_w_handle_leads') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'w_handle leads'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_a_concept =" @Priority(20) @Name('Correlate_a_concept') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_concept'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_a_accepted =" @Priority(20) @Name('Correlate_a_accepted') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_accepted'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_a_complete =" @Priority(20) @Name('Correlate_a_complete') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_complete'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_w_assess_potential_fraud =" @Priority(20) @Name('Correlate_w_assess_potential_fraud') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'w_assess potential fraud'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_o_refused =" @Priority(20) @Name('Correlate_o_refused') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'o_refused'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_w_validate_application =" @Priority(20) @Name('Correlate_w_validate_application') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'w_validate application'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_w_call_after_offers =" @Priority(20) @Name('Correlate_w_call_after_offers') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'w_call after offers'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_a_cancelled =" @Priority(20) @Name('Correlate_a_cancelled') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_cancelled'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_o_returned =" @Priority(20) @Name('Correlate_o_returned') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'o_returned'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_a_incomplete =" @Priority(20) @Name('Correlate_a_incomplete') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_incomplete'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_o_created =" @Priority(20) @Name('Correlate_o_created') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'o_created'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_a_pending =" @Priority(20) @Name('Correlate_a_pending') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_pending'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_w_call_incomplete_files =" @Priority(20) @Name('Correlate_w_call_incomplete_files') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'w_call incomplete files'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_w_shortened_completion =" @Priority(20) @Name('Correlate_w_shortened_completion') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'w_shortened completion'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_o_sent__mail_and_online_ =" @Priority(20) @Name('Correlate_o_sent__mail_and_online_') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'o_sent (mail and online)'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_a_validating =" @Priority(20) @Name('Correlate_a_validating') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_validating'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_o_create_offer =" @Priority(20) @Name('Correlate_o_create_offer') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'o_create offer'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_o_sent__online_only_ =" @Priority(20) @Name('Correlate_o_sent__online_only_') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'o_sent (online only)'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_a_denied =" @Priority(20) @Name('Correlate_a_denied') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'a_denied'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_o_accepted =" @Priority(20) @Name('Correlate_o_accepted') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'o_accepted'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_o_cancelled =" @Priority(20) @Name('Correlate_o_cancelled') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'o_cancelled'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_w_personal_loan_collection =" @Priority(20) @Name('Correlate_w_personal_loan_collection') insert into LabeledEvent (caseID, activity , timestamp, probability) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 'w_personal loan collection'  group by caseID order by timestamp desc limit 3 ; ";

        String epl_Filter_a_submitted =" @Priority(50) @Name('Filter_a_submitted') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_submitted'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_w_complete_application =" @Priority(50) @Name('Filter_w_complete_application') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'w_complete application'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_w_handle_leads =" @Priority(50) @Name('Filter_w_handle_leads') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'w_handle leads'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_a_concept =" @Priority(50) @Name('Filter_a_concept') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_concept'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_a_accepted =" @Priority(50) @Name('Filter_a_accepted') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_accepted'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_a_complete =" @Priority(50) @Name('Filter_a_complete') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_complete'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_w_assess_potential_fraud =" @Priority(50) @Name('Filter_w_assess_potential_fraud') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'w_assess potential fraud'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_o_refused =" @Priority(50) @Name('Filter_o_refused') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'o_refused'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_w_validate_application =" @Priority(50) @Name('Filter_w_validate_application') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'w_validate application'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_w_call_after_offers =" @Priority(50) @Name('Filter_w_call_after_offers') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'w_call after offers'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_a_cancelled =" @Priority(50) @Name('Filter_a_cancelled') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_cancelled'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_o_returned =" @Priority(50) @Name('Filter_o_returned') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'o_returned'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_a_incomplete =" @Priority(50) @Name('Filter_a_incomplete') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_incomplete'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_o_created =" @Priority(50) @Name('Filter_o_created') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'o_created'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_a_pending =" @Priority(50) @Name('Filter_a_pending') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_pending'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_w_call_incomplete_files =" @Priority(50) @Name('Filter_w_call_incomplete_files') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'w_call incomplete files'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_w_shortened_completion =" @Priority(50) @Name('Filter_w_shortened_completion') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'w_shortened completion'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_o_sent__mail_and_online_ =" @Priority(50) @Name('Filter_o_sent__mail_and_online_') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'o_sent (mail and online)'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_a_validating =" @Priority(50) @Name('Filter_a_validating') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_validating'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_o_create_offer =" @Priority(50) @Name('Filter_o_create_offer') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'o_create offer'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_o_sent__online_only_ =" @Priority(50) @Name('Filter_o_sent__online_only_') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'o_sent (online only)'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_a_denied =" @Priority(50) @Name('Filter_a_denied') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'a_denied'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_o_accepted =" @Priority(50) @Name('Filter_o_accepted') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'o_accepted'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_o_cancelled =" @Priority(50) @Name('Filter_o_cancelled') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'o_cancelled'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

        String epl_Filter_w_personal_loan_collection =" @Priority(50) @Name('Filter_w_personal_loan_collection') insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  "+initTime+" from LabeledEvent.win:time(1 sec) as T where activity = 'w_personal loan collection'  and probability >= 0.0 group by caseID order by probability desc limit 3 ;";

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

        EPCompiled epCompiled_start_case_a_create_application;
        EPDeployment deployment_start_case_a_create_application;

        EPCompiled epCompiled_TE_a_submitted;
        EPDeployment deployment_TE_a_submitted;

        EPCompiled epCompiled_TE_w_complete_application;
        EPDeployment deployment_TE_w_complete_application;

        EPCompiled epCompiled_TE_w_handle_leads;
        EPDeployment deployment_TE_w_handle_leads;

        EPCompiled epCompiled_TE_a_concept;
        EPDeployment deployment_TE_a_concept;

        EPCompiled epCompiled_TE_a_accepted;
        EPDeployment deployment_TE_a_accepted;

        EPCompiled epCompiled_TE_a_complete;
        EPDeployment deployment_TE_a_complete;

        EPCompiled epCompiled_TE_w_assess_potential_fraud;
        EPDeployment deployment_TE_w_assess_potential_fraud;

        EPCompiled epCompiled_TE_o_refused;
        EPDeployment deployment_TE_o_refused;

        EPCompiled epCompiled_TE_w_validate_application;
        EPDeployment deployment_TE_w_validate_application;

        EPCompiled epCompiled_TE_w_call_after_offers;
        EPDeployment deployment_TE_w_call_after_offers;

        EPCompiled epCompiled_TE_a_cancelled;
        EPDeployment deployment_TE_a_cancelled;

        EPCompiled epCompiled_TE_o_returned;
        EPDeployment deployment_TE_o_returned;

        EPCompiled epCompiled_TE_a_incomplete;
        EPDeployment deployment_TE_a_incomplete;

        EPCompiled epCompiled_TE_o_created;
        EPDeployment deployment_TE_o_created;

        EPCompiled epCompiled_TE_a_pending;
        EPDeployment deployment_TE_a_pending;

        EPCompiled epCompiled_TE_w_call_incomplete_files;
        EPDeployment deployment_TE_w_call_incomplete_files;

        EPCompiled epCompiled_TE_w_shortened_completion;
        EPDeployment deployment_TE_w_shortened_completion;

        EPCompiled epCompiled_TE_o_sent__mail_and_online_;
        EPDeployment deployment_TE_o_sent__mail_and_online_;

        EPCompiled epCompiled_TE_a_validating;
        EPDeployment deployment_TE_a_validating;

        EPCompiled epCompiled_TE_o_create_offer;
        EPDeployment deployment_TE_o_create_offer;

        EPCompiled epCompiled_TE_o_sent__online_only_;
        EPDeployment deployment_TE_o_sent__online_only_;

        EPCompiled epCompiled_TE_a_denied;
        EPDeployment deployment_TE_a_denied;

        EPCompiled epCompiled_TE_o_accepted;
        EPDeployment deployment_TE_o_accepted;

        EPCompiled epCompiled_TE_o_cancelled;
        EPDeployment deployment_TE_o_cancelled;

        EPCompiled epCompiled_TE_w_personal_loan_collection;
        EPDeployment deployment_TE_w_personal_loan_collection;

        EPCompiled epCompiled_Correlate_a_submitted;
        EPDeployment deployment_Correlate_a_submitted;

        EPCompiled epCompiled_Correlate_w_complete_application;
        EPDeployment deployment_Correlate_w_complete_application;

        EPCompiled epCompiled_Correlate_w_handle_leads;
        EPDeployment deployment_Correlate_w_handle_leads;

        EPCompiled epCompiled_Correlate_a_concept;
        EPDeployment deployment_Correlate_a_concept;

        EPCompiled epCompiled_Correlate_a_accepted;
        EPDeployment deployment_Correlate_a_accepted;

        EPCompiled epCompiled_Correlate_a_complete;
        EPDeployment deployment_Correlate_a_complete;

        EPCompiled epCompiled_Correlate_w_assess_potential_fraud;
        EPDeployment deployment_Correlate_w_assess_potential_fraud;

        EPCompiled epCompiled_Correlate_o_refused;
        EPDeployment deployment_Correlate_o_refused;

        EPCompiled epCompiled_Correlate_w_validate_application;
        EPDeployment deployment_Correlate_w_validate_application;

        EPCompiled epCompiled_Correlate_w_call_after_offers;
        EPDeployment deployment_Correlate_w_call_after_offers;

        EPCompiled epCompiled_Correlate_a_cancelled;
        EPDeployment deployment_Correlate_a_cancelled;

        EPCompiled epCompiled_Correlate_o_returned;
        EPDeployment deployment_Correlate_o_returned;

        EPCompiled epCompiled_Correlate_a_incomplete;
        EPDeployment deployment_Correlate_a_incomplete;

        EPCompiled epCompiled_Correlate_o_created;
        EPDeployment deployment_Correlate_o_created;

        EPCompiled epCompiled_Correlate_a_pending;
        EPDeployment deployment_Correlate_a_pending;

        EPCompiled epCompiled_Correlate_w_call_incomplete_files;
        EPDeployment deployment_Correlate_w_call_incomplete_files;

        EPCompiled epCompiled_Correlate_w_shortened_completion;
        EPDeployment deployment_Correlate_w_shortened_completion;

        EPCompiled epCompiled_Correlate_o_sent__mail_and_online_;
        EPDeployment deployment_Correlate_o_sent__mail_and_online_;

        EPCompiled epCompiled_Correlate_a_validating;
        EPDeployment deployment_Correlate_a_validating;

        EPCompiled epCompiled_Correlate_o_create_offer;
        EPDeployment deployment_Correlate_o_create_offer;

        EPCompiled epCompiled_Correlate_o_sent__online_only_;
        EPDeployment deployment_Correlate_o_sent__online_only_;

        EPCompiled epCompiled_Correlate_a_denied;
        EPDeployment deployment_Correlate_a_denied;

        EPCompiled epCompiled_Correlate_o_accepted;
        EPDeployment deployment_Correlate_o_accepted;

        EPCompiled epCompiled_Correlate_o_cancelled;
        EPDeployment deployment_Correlate_o_cancelled;

        EPCompiled epCompiled_Correlate_w_personal_loan_collection;
        EPDeployment deployment_Correlate_w_personal_loan_collection;

        EPCompiled epCompiled_Filter_a_submitted;
        EPDeployment deployment_Filter_a_submitted;

        EPCompiled epCompiled_Filter_w_complete_application;
        EPDeployment deployment_Filter_w_complete_application;

        EPCompiled epCompiled_Filter_w_handle_leads;
        EPDeployment deployment_Filter_w_handle_leads;

        EPCompiled epCompiled_Filter_a_concept;
        EPDeployment deployment_Filter_a_concept;

        EPCompiled epCompiled_Filter_a_accepted;
        EPDeployment deployment_Filter_a_accepted;

        EPCompiled epCompiled_Filter_a_complete;
        EPDeployment deployment_Filter_a_complete;

        EPCompiled epCompiled_Filter_w_assess_potential_fraud;
        EPDeployment deployment_Filter_w_assess_potential_fraud;

        EPCompiled epCompiled_Filter_o_refused;
        EPDeployment deployment_Filter_o_refused;

        EPCompiled epCompiled_Filter_w_validate_application;
        EPDeployment deployment_Filter_w_validate_application;

        EPCompiled epCompiled_Filter_w_call_after_offers;
        EPDeployment deployment_Filter_w_call_after_offers;

        EPCompiled epCompiled_Filter_a_cancelled;
        EPDeployment deployment_Filter_a_cancelled;

        EPCompiled epCompiled_Filter_o_returned;
        EPDeployment deployment_Filter_o_returned;

        EPCompiled epCompiled_Filter_a_incomplete;
        EPDeployment deployment_Filter_a_incomplete;

        EPCompiled epCompiled_Filter_o_created;
        EPDeployment deployment_Filter_o_created;

        EPCompiled epCompiled_Filter_a_pending;
        EPDeployment deployment_Filter_a_pending;

        EPCompiled epCompiled_Filter_w_call_incomplete_files;
        EPDeployment deployment_Filter_w_call_incomplete_files;

        EPCompiled epCompiled_Filter_w_shortened_completion;
        EPDeployment deployment_Filter_w_shortened_completion;

        EPCompiled epCompiled_Filter_o_sent__mail_and_online_;
        EPDeployment deployment_Filter_o_sent__mail_and_online_;

        EPCompiled epCompiled_Filter_a_validating;
        EPDeployment deployment_Filter_a_validating;

        EPCompiled epCompiled_Filter_o_create_offer;
        EPDeployment deployment_Filter_o_create_offer;

        EPCompiled epCompiled_Filter_o_sent__online_only_;
        EPDeployment deployment_Filter_o_sent__online_only_;

        EPCompiled epCompiled_Filter_a_denied;
        EPDeployment deployment_Filter_a_denied;

        EPCompiled epCompiled_Filter_o_accepted;
        EPDeployment deployment_Filter_o_accepted;

        EPCompiled epCompiled_Filter_o_cancelled;
        EPDeployment deployment_Filter_o_cancelled;

        EPCompiled epCompiled_Filter_w_personal_loan_collection;
        EPDeployment deployment_Filter_w_personal_loan_collection;

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

            epCompiled_start_case_a_create_application = compiler.compile(epl_start_case_a_create_application, arguments);
            deployment_start_case_a_create_application = deploymentService.deploy(epCompiled_start_case_a_create_application);

            epCompiled_TE_a_submitted = compiler.compile(epl_TE_a_submitted, arguments);
            deployment_TE_a_submitted = deploymentService.deploy(epCompiled_TE_a_submitted);

            epCompiled_TE_w_complete_application = compiler.compile(epl_TE_w_complete_application, arguments);
            deployment_TE_w_complete_application = deploymentService.deploy(epCompiled_TE_w_complete_application);

            epCompiled_TE_w_handle_leads = compiler.compile(epl_TE_w_handle_leads, arguments);
            deployment_TE_w_handle_leads = deploymentService.deploy(epCompiled_TE_w_handle_leads);

            epCompiled_TE_a_concept = compiler.compile(epl_TE_a_concept, arguments);
            deployment_TE_a_concept = deploymentService.deploy(epCompiled_TE_a_concept);

            epCompiled_TE_a_accepted = compiler.compile(epl_TE_a_accepted, arguments);
            deployment_TE_a_accepted = deploymentService.deploy(epCompiled_TE_a_accepted);

            epCompiled_TE_a_complete = compiler.compile(epl_TE_a_complete, arguments);
            deployment_TE_a_complete = deploymentService.deploy(epCompiled_TE_a_complete);

            epCompiled_TE_w_assess_potential_fraud = compiler.compile(epl_TE_w_assess_potential_fraud, arguments);
            deployment_TE_w_assess_potential_fraud = deploymentService.deploy(epCompiled_TE_w_assess_potential_fraud);

            epCompiled_TE_o_refused = compiler.compile(epl_TE_o_refused, arguments);
            deployment_TE_o_refused = deploymentService.deploy(epCompiled_TE_o_refused);

            epCompiled_TE_w_validate_application = compiler.compile(epl_TE_w_validate_application, arguments);
            deployment_TE_w_validate_application = deploymentService.deploy(epCompiled_TE_w_validate_application);

            epCompiled_TE_w_call_after_offers = compiler.compile(epl_TE_w_call_after_offers, arguments);
            deployment_TE_w_call_after_offers = deploymentService.deploy(epCompiled_TE_w_call_after_offers);

            epCompiled_TE_a_cancelled = compiler.compile(epl_TE_a_cancelled, arguments);
            deployment_TE_a_cancelled = deploymentService.deploy(epCompiled_TE_a_cancelled);

            epCompiled_TE_o_returned = compiler.compile(epl_TE_o_returned, arguments);
            deployment_TE_o_returned = deploymentService.deploy(epCompiled_TE_o_returned);

            epCompiled_TE_a_incomplete = compiler.compile(epl_TE_a_incomplete, arguments);
            deployment_TE_a_incomplete = deploymentService.deploy(epCompiled_TE_a_incomplete);

            epCompiled_TE_o_created = compiler.compile(epl_TE_o_created, arguments);
            deployment_TE_o_created = deploymentService.deploy(epCompiled_TE_o_created);

            epCompiled_TE_a_pending = compiler.compile(epl_TE_a_pending, arguments);
            deployment_TE_a_pending = deploymentService.deploy(epCompiled_TE_a_pending);

            epCompiled_TE_w_call_incomplete_files = compiler.compile(epl_TE_w_call_incomplete_files, arguments);
            deployment_TE_w_call_incomplete_files = deploymentService.deploy(epCompiled_TE_w_call_incomplete_files);

            epCompiled_TE_w_shortened_completion = compiler.compile(epl_TE_w_shortened_completion, arguments);
            deployment_TE_w_shortened_completion = deploymentService.deploy(epCompiled_TE_w_shortened_completion);

            epCompiled_TE_o_sent__mail_and_online_ = compiler.compile(epl_TE_o_sent__mail_and_online_, arguments);
            deployment_TE_o_sent__mail_and_online_ = deploymentService.deploy(epCompiled_TE_o_sent__mail_and_online_);

            epCompiled_TE_a_validating = compiler.compile(epl_TE_a_validating, arguments);
            deployment_TE_a_validating = deploymentService.deploy(epCompiled_TE_a_validating);

            epCompiled_TE_o_create_offer = compiler.compile(epl_TE_o_create_offer, arguments);
            deployment_TE_o_create_offer = deploymentService.deploy(epCompiled_TE_o_create_offer);

            epCompiled_TE_o_sent__online_only_ = compiler.compile(epl_TE_o_sent__online_only_, arguments);
            deployment_TE_o_sent__online_only_ = deploymentService.deploy(epCompiled_TE_o_sent__online_only_);

            epCompiled_TE_a_denied = compiler.compile(epl_TE_a_denied, arguments);
            deployment_TE_a_denied = deploymentService.deploy(epCompiled_TE_a_denied);

            epCompiled_TE_o_accepted = compiler.compile(epl_TE_o_accepted, arguments);
            deployment_TE_o_accepted = deploymentService.deploy(epCompiled_TE_o_accepted);

            epCompiled_TE_o_cancelled = compiler.compile(epl_TE_o_cancelled, arguments);
            deployment_TE_o_cancelled = deploymentService.deploy(epCompiled_TE_o_cancelled);

            epCompiled_TE_w_personal_loan_collection = compiler.compile(epl_TE_w_personal_loan_collection, arguments);
            deployment_TE_w_personal_loan_collection = deploymentService.deploy(epCompiled_TE_w_personal_loan_collection);

            CompilerArguments arguments2 = new CompilerArguments(configuration);
            arguments2.getPath().add(runtime.getRuntimePath());

            epCompiled_Correlate_a_submitted = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_submitted, arguments2);
            deployment_Correlate_a_submitted = deploymentService.deploy(epCompiled_Correlate_a_submitted);

            epCompiled_Correlate_w_complete_application = EPCompilerProvider.getCompiler().compile(epl_Correlate_w_complete_application, arguments2);
            deployment_Correlate_w_complete_application = deploymentService.deploy(epCompiled_Correlate_w_complete_application);

            epCompiled_Correlate_w_handle_leads = EPCompilerProvider.getCompiler().compile(epl_Correlate_w_handle_leads, arguments2);
            deployment_Correlate_w_handle_leads = deploymentService.deploy(epCompiled_Correlate_w_handle_leads);

            epCompiled_Correlate_a_concept = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_concept, arguments2);
            deployment_Correlate_a_concept = deploymentService.deploy(epCompiled_Correlate_a_concept);

            epCompiled_Correlate_a_accepted = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_accepted, arguments2);
            deployment_Correlate_a_accepted = deploymentService.deploy(epCompiled_Correlate_a_accepted);

            epCompiled_Correlate_a_complete = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_complete, arguments2);
            deployment_Correlate_a_complete = deploymentService.deploy(epCompiled_Correlate_a_complete);

            epCompiled_Correlate_w_assess_potential_fraud = EPCompilerProvider.getCompiler().compile(epl_Correlate_w_assess_potential_fraud, arguments2);
            deployment_Correlate_w_assess_potential_fraud = deploymentService.deploy(epCompiled_Correlate_w_assess_potential_fraud);

            epCompiled_Correlate_o_refused = EPCompilerProvider.getCompiler().compile(epl_Correlate_o_refused, arguments2);
            deployment_Correlate_o_refused = deploymentService.deploy(epCompiled_Correlate_o_refused);

            epCompiled_Correlate_w_validate_application = EPCompilerProvider.getCompiler().compile(epl_Correlate_w_validate_application, arguments2);
            deployment_Correlate_w_validate_application = deploymentService.deploy(epCompiled_Correlate_w_validate_application);

            epCompiled_Correlate_w_call_after_offers = EPCompilerProvider.getCompiler().compile(epl_Correlate_w_call_after_offers, arguments2);
            deployment_Correlate_w_call_after_offers = deploymentService.deploy(epCompiled_Correlate_w_call_after_offers);

            epCompiled_Correlate_a_cancelled = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_cancelled, arguments2);
            deployment_Correlate_a_cancelled = deploymentService.deploy(epCompiled_Correlate_a_cancelled);

            epCompiled_Correlate_o_returned = EPCompilerProvider.getCompiler().compile(epl_Correlate_o_returned, arguments2);
            deployment_Correlate_o_returned = deploymentService.deploy(epCompiled_Correlate_o_returned);

            epCompiled_Correlate_a_incomplete = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_incomplete, arguments2);
            deployment_Correlate_a_incomplete = deploymentService.deploy(epCompiled_Correlate_a_incomplete);

            epCompiled_Correlate_o_created = EPCompilerProvider.getCompiler().compile(epl_Correlate_o_created, arguments2);
            deployment_Correlate_o_created = deploymentService.deploy(epCompiled_Correlate_o_created);

            epCompiled_Correlate_a_pending = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_pending, arguments2);
            deployment_Correlate_a_pending = deploymentService.deploy(epCompiled_Correlate_a_pending);

            epCompiled_Correlate_w_call_incomplete_files = EPCompilerProvider.getCompiler().compile(epl_Correlate_w_call_incomplete_files, arguments2);
            deployment_Correlate_w_call_incomplete_files = deploymentService.deploy(epCompiled_Correlate_w_call_incomplete_files);

            epCompiled_Correlate_w_shortened_completion = EPCompilerProvider.getCompiler().compile(epl_Correlate_w_shortened_completion, arguments2);
            deployment_Correlate_w_shortened_completion = deploymentService.deploy(epCompiled_Correlate_w_shortened_completion);

            epCompiled_Correlate_o_sent__mail_and_online_ = EPCompilerProvider.getCompiler().compile(epl_Correlate_o_sent__mail_and_online_, arguments2);
            deployment_Correlate_o_sent__mail_and_online_ = deploymentService.deploy(epCompiled_Correlate_o_sent__mail_and_online_);

            epCompiled_Correlate_a_validating = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_validating, arguments2);
            deployment_Correlate_a_validating = deploymentService.deploy(epCompiled_Correlate_a_validating);

            epCompiled_Correlate_o_create_offer = EPCompilerProvider.getCompiler().compile(epl_Correlate_o_create_offer, arguments2);
            deployment_Correlate_o_create_offer = deploymentService.deploy(epCompiled_Correlate_o_create_offer);

            epCompiled_Correlate_o_sent__online_only_ = EPCompilerProvider.getCompiler().compile(epl_Correlate_o_sent__online_only_, arguments2);
            deployment_Correlate_o_sent__online_only_ = deploymentService.deploy(epCompiled_Correlate_o_sent__online_only_);

            epCompiled_Correlate_a_denied = EPCompilerProvider.getCompiler().compile(epl_Correlate_a_denied, arguments2);
            deployment_Correlate_a_denied = deploymentService.deploy(epCompiled_Correlate_a_denied);

            epCompiled_Correlate_o_accepted = EPCompilerProvider.getCompiler().compile(epl_Correlate_o_accepted, arguments2);
            deployment_Correlate_o_accepted = deploymentService.deploy(epCompiled_Correlate_o_accepted);

            epCompiled_Correlate_o_cancelled = EPCompilerProvider.getCompiler().compile(epl_Correlate_o_cancelled, arguments2);
            deployment_Correlate_o_cancelled = deploymentService.deploy(epCompiled_Correlate_o_cancelled);

            epCompiled_Correlate_w_personal_loan_collection = EPCompilerProvider.getCompiler().compile(epl_Correlate_w_personal_loan_collection, arguments2);
            deployment_Correlate_w_personal_loan_collection = deploymentService.deploy(epCompiled_Correlate_w_personal_loan_collection);


            CompilerArguments arguments3 = new CompilerArguments(configuration);
            arguments3.getPath().add(runtime.getRuntimePath());

            epCompiled_Filter_a_submitted = EPCompilerProvider.getCompiler().compile(epl_Filter_a_submitted, arguments3);
            deployment_Filter_a_submitted = deploymentService.deploy(epCompiled_Filter_a_submitted);

            epCompiled_Filter_w_complete_application = EPCompilerProvider.getCompiler().compile(epl_Filter_w_complete_application, arguments3);
            deployment_Filter_w_complete_application = deploymentService.deploy(epCompiled_Filter_w_complete_application);

            epCompiled_Filter_w_handle_leads = EPCompilerProvider.getCompiler().compile(epl_Filter_w_handle_leads, arguments3);
            deployment_Filter_w_handle_leads = deploymentService.deploy(epCompiled_Filter_w_handle_leads);

            epCompiled_Filter_a_concept = EPCompilerProvider.getCompiler().compile(epl_Filter_a_concept, arguments3);
            deployment_Filter_a_concept = deploymentService.deploy(epCompiled_Filter_a_concept);

            epCompiled_Filter_a_accepted = EPCompilerProvider.getCompiler().compile(epl_Filter_a_accepted, arguments3);
            deployment_Filter_a_accepted = deploymentService.deploy(epCompiled_Filter_a_accepted);

            epCompiled_Filter_a_complete = EPCompilerProvider.getCompiler().compile(epl_Filter_a_complete, arguments3);
            deployment_Filter_a_complete = deploymentService.deploy(epCompiled_Filter_a_complete);

            epCompiled_Filter_w_assess_potential_fraud = EPCompilerProvider.getCompiler().compile(epl_Filter_w_assess_potential_fraud, arguments3);
            deployment_Filter_w_assess_potential_fraud = deploymentService.deploy(epCompiled_Filter_w_assess_potential_fraud);

            epCompiled_Filter_o_refused = EPCompilerProvider.getCompiler().compile(epl_Filter_o_refused, arguments3);
            deployment_Filter_o_refused = deploymentService.deploy(epCompiled_Filter_o_refused);

            epCompiled_Filter_w_validate_application = EPCompilerProvider.getCompiler().compile(epl_Filter_w_validate_application, arguments3);
            deployment_Filter_w_validate_application = deploymentService.deploy(epCompiled_Filter_w_validate_application);

            epCompiled_Filter_w_call_after_offers = EPCompilerProvider.getCompiler().compile(epl_Filter_w_call_after_offers, arguments3);
            deployment_Filter_w_call_after_offers = deploymentService.deploy(epCompiled_Filter_w_call_after_offers);

            epCompiled_Filter_a_cancelled = EPCompilerProvider.getCompiler().compile(epl_Filter_a_cancelled, arguments3);
            deployment_Filter_a_cancelled = deploymentService.deploy(epCompiled_Filter_a_cancelled);

            epCompiled_Filter_o_returned = EPCompilerProvider.getCompiler().compile(epl_Filter_o_returned, arguments3);
            deployment_Filter_o_returned = deploymentService.deploy(epCompiled_Filter_o_returned);

            epCompiled_Filter_a_incomplete = EPCompilerProvider.getCompiler().compile(epl_Filter_a_incomplete, arguments3);
            deployment_Filter_a_incomplete = deploymentService.deploy(epCompiled_Filter_a_incomplete);

            epCompiled_Filter_o_created = EPCompilerProvider.getCompiler().compile(epl_Filter_o_created, arguments3);
            deployment_Filter_o_created = deploymentService.deploy(epCompiled_Filter_o_created);

            epCompiled_Filter_a_pending = EPCompilerProvider.getCompiler().compile(epl_Filter_a_pending, arguments3);
            deployment_Filter_a_pending = deploymentService.deploy(epCompiled_Filter_a_pending);

            epCompiled_Filter_w_call_incomplete_files = EPCompilerProvider.getCompiler().compile(epl_Filter_w_call_incomplete_files, arguments3);
            deployment_Filter_w_call_incomplete_files = deploymentService.deploy(epCompiled_Filter_w_call_incomplete_files);

            epCompiled_Filter_w_shortened_completion = EPCompilerProvider.getCompiler().compile(epl_Filter_w_shortened_completion, arguments3);
            deployment_Filter_w_shortened_completion = deploymentService.deploy(epCompiled_Filter_w_shortened_completion);

            epCompiled_Filter_o_sent__mail_and_online_ = EPCompilerProvider.getCompiler().compile(epl_Filter_o_sent__mail_and_online_, arguments3);
            deployment_Filter_o_sent__mail_and_online_ = deploymentService.deploy(epCompiled_Filter_o_sent__mail_and_online_);

            epCompiled_Filter_a_validating = EPCompilerProvider.getCompiler().compile(epl_Filter_a_validating, arguments3);
            deployment_Filter_a_validating = deploymentService.deploy(epCompiled_Filter_a_validating);

            epCompiled_Filter_o_create_offer = EPCompilerProvider.getCompiler().compile(epl_Filter_o_create_offer, arguments3);
            deployment_Filter_o_create_offer = deploymentService.deploy(epCompiled_Filter_o_create_offer);

            epCompiled_Filter_o_sent__online_only_ = EPCompilerProvider.getCompiler().compile(epl_Filter_o_sent__online_only_, arguments3);
            deployment_Filter_o_sent__online_only_ = deploymentService.deploy(epCompiled_Filter_o_sent__online_only_);

            epCompiled_Filter_a_denied = EPCompilerProvider.getCompiler().compile(epl_Filter_a_denied, arguments3);
            deployment_Filter_a_denied = deploymentService.deploy(epCompiled_Filter_a_denied);

            epCompiled_Filter_o_accepted = EPCompilerProvider.getCompiler().compile(epl_Filter_o_accepted, arguments3);
            deployment_Filter_o_accepted = deploymentService.deploy(epCompiled_Filter_o_accepted);

            epCompiled_Filter_o_cancelled = EPCompilerProvider.getCompiler().compile(epl_Filter_o_cancelled, arguments3);
            deployment_Filter_o_cancelled = deploymentService.deploy(epCompiled_Filter_o_cancelled);

            epCompiled_Filter_w_personal_loan_collection = EPCompilerProvider.getCompiler().compile(epl_Filter_w_personal_loan_collection, arguments3);
            deployment_Filter_w_personal_loan_collection = deploymentService.deploy(epCompiled_Filter_w_personal_loan_collection);


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
        EPStatement statement_start_case_a_create_application = runtime.getDeploymentService().getStatement(deployment_start_case_a_create_application.getDeploymentId(), "start_case_a_create_application");
        EPStatement statement_TE_a_submitted = runtime.getDeploymentService().getStatement(deployment_TE_a_submitted.getDeploymentId(), "TE_a_submitted");
        EPStatement statement_TE_w_complete_application = runtime.getDeploymentService().getStatement(deployment_TE_w_complete_application.getDeploymentId(), "TE_w_complete_application");
        EPStatement statement_TE_w_handle_leads = runtime.getDeploymentService().getStatement(deployment_TE_w_handle_leads.getDeploymentId(), "TE_w_handle_leads");
        EPStatement statement_TE_a_concept = runtime.getDeploymentService().getStatement(deployment_TE_a_concept.getDeploymentId(), "TE_a_concept");
        EPStatement statement_TE_a_accepted = runtime.getDeploymentService().getStatement(deployment_TE_a_accepted.getDeploymentId(), "TE_a_accepted");
        EPStatement statement_TE_a_complete = runtime.getDeploymentService().getStatement(deployment_TE_a_complete.getDeploymentId(), "TE_a_complete");
        EPStatement statement_TE_w_assess_potential_fraud = runtime.getDeploymentService().getStatement(deployment_TE_w_assess_potential_fraud.getDeploymentId(), "TE_w_assess_potential_fraud");
        EPStatement statement_TE_o_refused = runtime.getDeploymentService().getStatement(deployment_TE_o_refused.getDeploymentId(), "TE_o_refused");
        EPStatement statement_TE_w_validate_application = runtime.getDeploymentService().getStatement(deployment_TE_w_validate_application.getDeploymentId(), "TE_w_validate_application");
        EPStatement statement_TE_w_call_after_offers = runtime.getDeploymentService().getStatement(deployment_TE_w_call_after_offers.getDeploymentId(), "TE_w_call_after_offers");
        EPStatement statement_TE_a_cancelled = runtime.getDeploymentService().getStatement(deployment_TE_a_cancelled.getDeploymentId(), "TE_a_cancelled");
        EPStatement statement_TE_o_returned = runtime.getDeploymentService().getStatement(deployment_TE_o_returned.getDeploymentId(), "TE_o_returned");
        EPStatement statement_TE_a_incomplete = runtime.getDeploymentService().getStatement(deployment_TE_a_incomplete.getDeploymentId(), "TE_a_incomplete");
        EPStatement statement_TE_o_created = runtime.getDeploymentService().getStatement(deployment_TE_o_created.getDeploymentId(), "TE_o_created");
        EPStatement statement_TE_a_pending = runtime.getDeploymentService().getStatement(deployment_TE_a_pending.getDeploymentId(), "TE_a_pending");
        EPStatement statement_TE_w_call_incomplete_files = runtime.getDeploymentService().getStatement(deployment_TE_w_call_incomplete_files.getDeploymentId(), "TE_w_call_incomplete_files");
        EPStatement statement_TE_w_shortened_completion = runtime.getDeploymentService().getStatement(deployment_TE_w_shortened_completion.getDeploymentId(), "TE_w_shortened_completion");
        EPStatement statement_TE_o_sent__mail_and_online_ = runtime.getDeploymentService().getStatement(deployment_TE_o_sent__mail_and_online_.getDeploymentId(), "TE_o_sent__mail_and_online_");
        EPStatement statement_TE_a_validating = runtime.getDeploymentService().getStatement(deployment_TE_a_validating.getDeploymentId(), "TE_a_validating");
        EPStatement statement_TE_o_create_offer = runtime.getDeploymentService().getStatement(deployment_TE_o_create_offer.getDeploymentId(), "TE_o_create_offer");
        EPStatement statement_TE_o_sent__online_only_ = runtime.getDeploymentService().getStatement(deployment_TE_o_sent__online_only_.getDeploymentId(), "TE_o_sent__online_only_");
        EPStatement statement_TE_a_denied = runtime.getDeploymentService().getStatement(deployment_TE_a_denied.getDeploymentId(), "TE_a_denied");
        EPStatement statement_TE_o_accepted = runtime.getDeploymentService().getStatement(deployment_TE_o_accepted.getDeploymentId(), "TE_o_accepted");
        EPStatement statement_TE_o_cancelled = runtime.getDeploymentService().getStatement(deployment_TE_o_cancelled.getDeploymentId(), "TE_o_cancelled");
        EPStatement statement_TE_w_personal_loan_collection = runtime.getDeploymentService().getStatement(deployment_TE_w_personal_loan_collection.getDeploymentId(), "TE_w_personal_loan_collection");

        EPStatement statement_Correlate_a_submitted = runtime.getDeploymentService().getStatement(deployment_Correlate_a_submitted.getDeploymentId(), "Correlate_a_submitted");
        EPStatement statement_Correlate_w_complete_application = runtime.getDeploymentService().getStatement(deployment_Correlate_w_complete_application.getDeploymentId(), "Correlate_w_complete_application");
        EPStatement statement_Correlate_w_handle_leads = runtime.getDeploymentService().getStatement(deployment_Correlate_w_handle_leads.getDeploymentId(), "Correlate_w_handle_leads");
        EPStatement statement_Correlate_a_concept = runtime.getDeploymentService().getStatement(deployment_Correlate_a_concept.getDeploymentId(), "Correlate_a_concept");
        EPStatement statement_Correlate_a_accepted = runtime.getDeploymentService().getStatement(deployment_Correlate_a_accepted.getDeploymentId(), "Correlate_a_accepted");
        EPStatement statement_Correlate_a_complete = runtime.getDeploymentService().getStatement(deployment_Correlate_a_complete.getDeploymentId(), "Correlate_a_complete");
        EPStatement statement_Correlate_w_assess_potential_fraud = runtime.getDeploymentService().getStatement(deployment_Correlate_w_assess_potential_fraud.getDeploymentId(), "Correlate_w_assess_potential_fraud");
        EPStatement statement_Correlate_o_refused = runtime.getDeploymentService().getStatement(deployment_Correlate_o_refused.getDeploymentId(), "Correlate_o_refused");
        EPStatement statement_Correlate_w_validate_application = runtime.getDeploymentService().getStatement(deployment_Correlate_w_validate_application.getDeploymentId(), "Correlate_w_validate_application");
        EPStatement statement_Correlate_w_call_after_offers = runtime.getDeploymentService().getStatement(deployment_Correlate_w_call_after_offers.getDeploymentId(), "Correlate_w_call_after_offers");
        EPStatement statement_Correlate_a_cancelled = runtime.getDeploymentService().getStatement(deployment_Correlate_a_cancelled.getDeploymentId(), "Correlate_a_cancelled");
        EPStatement statement_Correlate_o_returned = runtime.getDeploymentService().getStatement(deployment_Correlate_o_returned.getDeploymentId(), "Correlate_o_returned");
        EPStatement statement_Correlate_a_incomplete = runtime.getDeploymentService().getStatement(deployment_Correlate_a_incomplete.getDeploymentId(), "Correlate_a_incomplete");
        EPStatement statement_Correlate_o_created = runtime.getDeploymentService().getStatement(deployment_Correlate_o_created.getDeploymentId(), "Correlate_o_created");
        EPStatement statement_Correlate_a_pending = runtime.getDeploymentService().getStatement(deployment_Correlate_a_pending.getDeploymentId(), "Correlate_a_pending");
        EPStatement statement_Correlate_w_call_incomplete_files = runtime.getDeploymentService().getStatement(deployment_Correlate_w_call_incomplete_files.getDeploymentId(), "Correlate_w_call_incomplete_files");
        EPStatement statement_Correlate_w_shortened_completion = runtime.getDeploymentService().getStatement(deployment_Correlate_w_shortened_completion.getDeploymentId(), "Correlate_w_shortened_completion");
        EPStatement statement_Correlate_o_sent__mail_and_online_ = runtime.getDeploymentService().getStatement(deployment_Correlate_o_sent__mail_and_online_.getDeploymentId(), "Correlate_o_sent__mail_and_online_");
        EPStatement statement_Correlate_a_validating = runtime.getDeploymentService().getStatement(deployment_Correlate_a_validating.getDeploymentId(), "Correlate_a_validating");
        EPStatement statement_Correlate_o_create_offer = runtime.getDeploymentService().getStatement(deployment_Correlate_o_create_offer.getDeploymentId(), "Correlate_o_create_offer");
        EPStatement statement_Correlate_o_sent__online_only_ = runtime.getDeploymentService().getStatement(deployment_Correlate_o_sent__online_only_.getDeploymentId(), "Correlate_o_sent__online_only_");
        EPStatement statement_Correlate_a_denied = runtime.getDeploymentService().getStatement(deployment_Correlate_a_denied.getDeploymentId(), "Correlate_a_denied");
        EPStatement statement_Correlate_o_accepted = runtime.getDeploymentService().getStatement(deployment_Correlate_o_accepted.getDeploymentId(), "Correlate_o_accepted");
        EPStatement statement_Correlate_o_cancelled = runtime.getDeploymentService().getStatement(deployment_Correlate_o_cancelled.getDeploymentId(), "Correlate_o_cancelled");
        EPStatement statement_Correlate_w_personal_loan_collection = runtime.getDeploymentService().getStatement(deployment_Correlate_w_personal_loan_collection.getDeploymentId(), "Correlate_w_personal_loan_collection");

        EPStatement statement_Filter_a_submitted = runtime.getDeploymentService().getStatement(deployment_Filter_a_submitted.getDeploymentId(), "Filter_a_submitted");
        EPStatement statement_Filter_w_complete_application = runtime.getDeploymentService().getStatement(deployment_Filter_w_complete_application.getDeploymentId(), "Filter_w_complete_application");
        EPStatement statement_Filter_w_handle_leads = runtime.getDeploymentService().getStatement(deployment_Filter_w_handle_leads.getDeploymentId(), "Filter_w_handle_leads");
        EPStatement statement_Filter_a_concept = runtime.getDeploymentService().getStatement(deployment_Filter_a_concept.getDeploymentId(), "Filter_a_concept");
        EPStatement statement_Filter_a_accepted = runtime.getDeploymentService().getStatement(deployment_Filter_a_accepted.getDeploymentId(), "Filter_a_accepted");
        EPStatement statement_Filter_a_complete = runtime.getDeploymentService().getStatement(deployment_Filter_a_complete.getDeploymentId(), "Filter_a_complete");
        EPStatement statement_Filter_w_assess_potential_fraud = runtime.getDeploymentService().getStatement(deployment_Filter_w_assess_potential_fraud.getDeploymentId(), "Filter_w_assess_potential_fraud");
        EPStatement statement_Filter_o_refused = runtime.getDeploymentService().getStatement(deployment_Filter_o_refused.getDeploymentId(), "Filter_o_refused");
        EPStatement statement_Filter_w_validate_application = runtime.getDeploymentService().getStatement(deployment_Filter_w_validate_application.getDeploymentId(), "Filter_w_validate_application");
        EPStatement statement_Filter_w_call_after_offers = runtime.getDeploymentService().getStatement(deployment_Filter_w_call_after_offers.getDeploymentId(), "Filter_w_call_after_offers");
        EPStatement statement_Filter_a_cancelled = runtime.getDeploymentService().getStatement(deployment_Filter_a_cancelled.getDeploymentId(), "Filter_a_cancelled");
        EPStatement statement_Filter_o_returned = runtime.getDeploymentService().getStatement(deployment_Filter_o_returned.getDeploymentId(), "Filter_o_returned");
        EPStatement statement_Filter_a_incomplete = runtime.getDeploymentService().getStatement(deployment_Filter_a_incomplete.getDeploymentId(), "Filter_a_incomplete");
        EPStatement statement_Filter_o_created = runtime.getDeploymentService().getStatement(deployment_Filter_o_created.getDeploymentId(), "Filter_o_created");
        EPStatement statement_Filter_a_pending = runtime.getDeploymentService().getStatement(deployment_Filter_a_pending.getDeploymentId(), "Filter_a_pending");
        EPStatement statement_Filter_w_call_incomplete_files = runtime.getDeploymentService().getStatement(deployment_Filter_w_call_incomplete_files.getDeploymentId(), "Filter_w_call_incomplete_files");
        EPStatement statement_Filter_w_shortened_completion = runtime.getDeploymentService().getStatement(deployment_Filter_w_shortened_completion.getDeploymentId(), "Filter_w_shortened_completion");
        EPStatement statement_Filter_o_sent__mail_and_online_ = runtime.getDeploymentService().getStatement(deployment_Filter_o_sent__mail_and_online_.getDeploymentId(), "Filter_o_sent__mail_and_online_");
        EPStatement statement_Filter_a_validating = runtime.getDeploymentService().getStatement(deployment_Filter_a_validating.getDeploymentId(), "Filter_a_validating");
        EPStatement statement_Filter_o_create_offer = runtime.getDeploymentService().getStatement(deployment_Filter_o_create_offer.getDeploymentId(), "Filter_o_create_offer");
        EPStatement statement_Filter_o_sent__online_only_ = runtime.getDeploymentService().getStatement(deployment_Filter_o_sent__online_only_.getDeploymentId(), "Filter_o_sent__online_only_");
        EPStatement statement_Filter_a_denied = runtime.getDeploymentService().getStatement(deployment_Filter_a_denied.getDeploymentId(), "Filter_a_denied");
        EPStatement statement_Filter_o_accepted = runtime.getDeploymentService().getStatement(deployment_Filter_o_accepted.getDeploymentId(), "Filter_o_accepted");
        EPStatement statement_Filter_o_cancelled = runtime.getDeploymentService().getStatement(deployment_Filter_o_cancelled.getDeploymentId(), "Filter_o_cancelled");
        EPStatement statement_Filter_w_personal_loan_collection = runtime.getDeploymentService().getStatement(deployment_Filter_w_personal_loan_collection.getDeploymentId(), "Filter_w_personal_loan_collection");

        statement_Unlabeled.addListener((newData, oldData, s, r) -> {
//            String activity = (String) newData[0].get("activity");
//            long timestamp = (long) newData[0].get("timestamp");
//      	initTime = (long) newData[0].get("sysTime");
//          long sysTime = (long) newData[0].get("sysTime");
          initTime = (long) newData[0].get("sysTime");
//          initTime = sysTime;
//            Date ts = new Date(timestamp);
//            Format format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
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
        statement_TE_a_submitted.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_submitted-instance "+newData[0].getUnderlying()); }); 
        statement_TE_w_complete_application.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_w_complete_application-instance "+newData[0].getUnderlying()); }); 
        statement_TE_w_handle_leads.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_w_handle_leads-instance "+newData[0].getUnderlying()); }); 
        statement_TE_a_concept.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_concept-instance "+newData[0].getUnderlying()); }); 
        statement_TE_a_accepted.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_accepted-instance "+newData[0].getUnderlying()); }); 
        statement_TE_a_complete.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_complete-instance "+newData[0].getUnderlying()); }); 
        statement_TE_w_assess_potential_fraud.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_w_assess_potential_fraud-instance "+newData[0].getUnderlying()); }); 
        statement_TE_o_refused.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_o_refused-instance "+newData[0].getUnderlying()); }); 
        statement_TE_w_validate_application.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_w_validate_application-instance "+newData[0].getUnderlying()); }); 
        statement_TE_w_call_after_offers.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_w_call_after_offers-instance "+newData[0].getUnderlying()); }); 
        statement_TE_a_cancelled.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_cancelled-instance "+newData[0].getUnderlying()); }); 
        statement_TE_o_returned.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_o_returned-instance "+newData[0].getUnderlying()); }); 
        statement_TE_a_incomplete.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_incomplete-instance "+newData[0].getUnderlying()); }); 
        statement_TE_o_created.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_o_created-instance "+newData[0].getUnderlying()); }); 
        statement_TE_a_pending.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_pending-instance "+newData[0].getUnderlying()); }); 
        statement_TE_w_call_incomplete_files.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_w_call_incomplete_files-instance "+newData[0].getUnderlying()); }); 
        statement_TE_w_shortened_completion.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_w_shortened_completion-instance "+newData[0].getUnderlying()); }); 
        statement_TE_o_sent__mail_and_online_.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_o_sent__mail_and_online_-instance "+newData[0].getUnderlying()); }); 
        statement_TE_a_validating.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_validating-instance "+newData[0].getUnderlying()); }); 
        statement_TE_o_create_offer.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_o_create_offer-instance "+newData[0].getUnderlying()); }); 
        statement_TE_o_sent__online_only_.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_o_sent__online_only_-instance "+newData[0].getUnderlying()); }); 
        statement_TE_a_denied.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_a_denied-instance "+newData[0].getUnderlying()); }); 
        statement_TE_o_accepted.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_o_accepted-instance "+newData[0].getUnderlying()); }); 
        statement_TE_o_cancelled.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_o_cancelled-instance "+newData[0].getUnderlying()); }); 
        statement_TE_w_personal_loan_collection.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_w_personal_loan_collection-instance "+newData[0].getUnderlying()); }); 

        statement_Correlate_a_submitted.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_submitted-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_w_complete_application.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_w_complete_application-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_w_handle_leads.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_w_handle_leads-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_a_concept.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_concept-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_a_accepted.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_accepted-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_a_complete.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_complete-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_w_assess_potential_fraud.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_w_assess_potential_fraud-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_o_refused.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_o_refused-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_w_validate_application.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_w_validate_application-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_w_call_after_offers.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_w_call_after_offers-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_a_cancelled.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_cancelled-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_o_returned.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_o_returned-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_a_incomplete.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_incomplete-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_o_created.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_o_created-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_a_pending.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_pending-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_w_call_incomplete_files.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_w_call_incomplete_files-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_w_shortened_completion.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_w_shortened_completion-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_o_sent__mail_and_online_.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_o_sent__mail_and_online_-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_a_validating.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_validating-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_o_create_offer.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_o_create_offer-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_o_sent__online_only_.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_o_sent__online_only_-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_a_denied.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_a_denied-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_o_accepted.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_o_accepted-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_o_cancelled.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_o_cancelled-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_w_personal_loan_collection.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_w_personal_loan_collection-instance "+newData[0].getUnderlying()); }); 

        statement_Filter_a_submitted.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_submitted-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_w_complete_application.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_w_complete_application-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_w_handle_leads.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_w_handle_leads-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_a_concept.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_concept-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_a_accepted.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_accepted-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_a_complete.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_complete-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_w_assess_potential_fraud.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_w_assess_potential_fraud-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_o_refused.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_o_refused-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_w_validate_application.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_w_validate_application-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_w_call_after_offers.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_w_call_after_offers-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_a_cancelled.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_cancelled-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_o_returned.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_o_returned-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_a_incomplete.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_incomplete-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_o_created.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_o_created-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_a_pending.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_pending-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_w_call_incomplete_files.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_w_call_incomplete_files-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_w_shortened_completion.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_w_shortened_completion-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_o_sent__mail_and_online_.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_o_sent__mail_and_online_-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_a_validating.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_validating-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_o_create_offer.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_o_create_offer-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_o_sent__online_only_.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_o_sent__online_only_-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_a_denied.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_a_denied-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_o_accepted.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_o_accepted-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_o_cancelled.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_o_cancelled-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_w_personal_loan_collection.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_w_personal_loan_collection-instance "+newData[0].getUnderlying()); }); 

*/

        String filePath = "C:\\IH\\eclipse-workspace\\CEPForEventCorrelation\\BPIC2017-clean-mod-ordered-noLC-noR-unLabeled.csv";
        new UnlabeledEventSource(filePath, 561672, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent

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