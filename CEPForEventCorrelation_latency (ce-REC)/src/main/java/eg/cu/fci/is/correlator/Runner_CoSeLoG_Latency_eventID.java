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

public class Runner_CoSeLoG_Latency_eventID {

	private static long initTime =0;
	
    public static void main(String[] args) throws InterruptedException, IOException {

    	System.out.println("***** APP START *****");

    	String labeledEvents = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\CoSeLoG_labeled_latency_eventID_run5.csv";//_prob03_limit1_latency_limit3Correlate
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

        String epl_get_case_ID =" @Name('get_case_ID')  insert into Cases  select (select coalesce(max(caseID)+1, 1)  from  Cases )  as caseID from UnlabeledEvent  where  activity in ('confirmation of receipt');  ";

        String epl_start_case_confirmation_of_receipt =" @Name('start_case_confirmation_of_receipt')  insert into FilterLabeledEvent (eventID, caseID, activity, timestamp, probability, initTime)  select  UE.eventID, (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0, UE.sysTime as initTime  from UnlabeledEvent as UE where UE.activity = 'confirmation of receipt'; ";

        String epl_TE_t15_print_document_x_request_unlicensed =" @Priority(5) @Name('TE_t15_print_document_x_request_unlicensed') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't15 print document x request unlicensed' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 't10 determine necessity to stop indication') -> every succ=UnlabeledEvent(activity='t15 print document x request unlicensed')) where timer:within(85 min)]#time(85 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=84780000;";

        String epl_TE_t14_determine_document_x_request_unlicensed =" @Priority(5) @Name('TE_t14_determine_document_x_request_unlicensed') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't14 determine document x request unlicensed' as activity, succ.timestamp as timestamp from pattern [((every pred=FilterLabeledEvent(activity='t04 determine confirmation of receipt') and every inter1 = FilterLabeledEvent(activity='t02 check confirmation of receipt')) -> every succ=UnlabeledEvent(activity='t14 determine document x request unlicensed')) where timer:within(86 min) ]#time(86 min)  where ( pred.caseID = inter1.caseID and (succ.timestamp - max(pred.timestamp,inter1.timestamp)) >= 0 and (succ.timestamp - max(pred.timestamp,inter1.timestamp)) <=85920000 ) ;";

        String epl_TE_t10_determine_necessity_to_stop_indication =" @Priority(5) @Name('TE_t10_determine_necessity_to_stop_indication') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't10 determine necessity to stop indication' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 't06 determine necessity of stop advice') -> every succ=UnlabeledEvent(activity='t10 determine necessity to stop indication')) where timer:within(87 min)]#time(87 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86160000;";

        String epl_TE_t19_determine_report_y_to_stop_indication =" @Priority(5) @Name('TE_t19_determine_report_y_to_stop_indication') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't19 determine report y to stop indication' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 't17 check report y to stop indication') -> every succ=UnlabeledEvent(activity='t19 determine report y to stop indication')) where timer:within(1 min)]#time(1 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=180000;";

        String epl_TE_t20_print_report_y_to_stop_indication =" @Priority(5) @Name('TE_t20_print_report_y_to_stop_indication') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't20 print report y to stop indication' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 't19 determine report y to stop indication') -> every succ=UnlabeledEvent(activity='t20 print report y to stop indication')) where timer:within(1 min)]#time(1 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=120000;";

        String epl_TE_t18_adjust_report_y_to_stop_indicition =" @Priority(5) @Name('TE_t18_adjust_report_y_to_stop_indicition') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't18 adjust report y to stop indicition' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 't17 check report y to stop indication') -> every succ=UnlabeledEvent(activity='t18 adjust report y to stop indicition')) where timer:within(1 min)]#time(1 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=60000;";

        String epl_TE_t12_check_document_x_request_unlicensed =" @Priority(5) @Name('TE_t12_check_document_x_request_unlicensed') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't12 check document x request unlicensed' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity = 't11 create document x request unlicensed') -> every succ=UnlabeledEvent(activity='t12 check document x request unlicensed')) where timer:within(82 min)]#time(82 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=81540000;";

        String epl_TE_t17_check_report_y_to_stop_indication =" @Priority(5) @Name('TE_t17_check_report_y_to_stop_indication') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't17 check report y to stop indication' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t16 report reasons to hold request') or every pred=FilterLabeledEvent(activity='t18 adjust report y to stop indicition') -> every succ=UnlabeledEvent(activity='t17 check report y to stop indication')) where timer:within(1 min)]#time(1 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=780000;";

        String epl_TE_t03_adjust_confirmation_of_receipt =" @Priority(5) @Name('TE_t03_adjust_confirmation_of_receipt') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't03 adjust confirmation of receipt' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t03 adjust confirmation of receipt')) where timer:within(86 min)]#time(86 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=85920000;";

        String epl_TE_t07_1_draft_intern_advice_aspect_1 =" @Priority(5) @Name('TE_t07_1_draft_intern_advice_aspect_1') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't07-1 draft intern advice aspect 1' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t07-1 draft intern advice aspect 1')) where timer:within(86 min)]#time(86 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=85680000;";

        String epl_TE_t07_2_draft_intern_advice_aspect_2 =" @Priority(5) @Name('TE_t07_2_draft_intern_advice_aspect_2') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't07-2 draft intern advice aspect 2' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t07-2 draft intern advice aspect 2')) where timer:within(86 min)]#time(86 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=85860000;";

        String epl_TE_t07_3_draft_intern_advice_hold_for_aspect_3 =" @Priority(5) @Name('TE_t07_3_draft_intern_advice_hold_for_aspect_3') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't07-3 draft intern advice hold for aspect 3' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t07-3 draft intern advice hold for aspect 3')) where timer:within(3 min)]#time(3 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=2460000;";

        String epl_TE_t07_4_draft_internal_advice_to_hold_for_type_4 =" @Priority(5) @Name('TE_t07_4_draft_internal_advice_to_hold_for_type_4') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't07-4 draft internal advice to hold for type 4' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t07-4 draft internal advice to hold for type 4')) where timer:within(81 min)]#time(81 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=80400000;";

        String epl_TE_t07_5_draft_intern_advice_aspect_5 =" @Priority(5) @Name('TE_t07_5_draft_intern_advice_aspect_5') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't07-5 draft intern advice aspect 5' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t07-5 draft intern advice aspect 5')) where timer:within(83 min)]#time(83 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=82740000;";

        String epl_TE_t08_draft_and_send_request_for_advice =" @Priority(5) @Name('TE_t08_draft_and_send_request_for_advice') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't08 draft and send request for advice' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t08 draft and send request for advice')) where timer:within(73 min)]#time(73 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=72000000;";

        String epl_TE_t09_1_process_or_receive_external_advice_from_party_1 =" @Priority(5) @Name('TE_t09_1_process_or_receive_external_advice_from_party_1') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't09-1 process or receive external advice from party 1' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t09-1 process or receive external advice from party 1')) where timer:within(1 min)]#time(1 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=480000;";

        String epl_TE_t09_2_process_or_receive_external_advice_from_party_2 =" @Priority(5) @Name('TE_t09_2_process_or_receive_external_advice_from_party_2') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't09-2 process or receive external advice from party 2' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t09-2 process or receive external advice from party 2')) where timer:within(1 min)]#time(1 min)  where succ.timestamp - pred.timestamp >= 600 and succ.timestamp - pred.timestamp <=600000;";

        String epl_TE_t09_3_process_or_receive_external_advice_from_party_3 =" @Priority(5) @Name('TE_t09_3_process_or_receive_external_advice_from_party_3') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't09-3 process or receive external advice from party 3' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t09-3 process or receive external advice from party 3')) where timer:within(4 min)]#time(4 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=3540000;";

        String epl_TE_t09_4_process_or_receive_external_advice_from_party_4 =" @Priority(5) @Name('TE_t09_4_process_or_receive_external_advice_from_party_4') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't09-4 process or receive external advice from party 4' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t09-4 process or receive external advice from party 4')) where timer:within(3 min)]#time(3 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=2220000;";

        String epl_TE_t11_create_document_x_request_unlicensed =" @Priority(5) @Name('TE_t11_create_document_x_request_unlicensed') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't11 create document x request unlicensed' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t11 create document x request unlicensed')) where timer:within(84 min)]#time(84 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=83100000;";

        String epl_TE_t16_report_reasons_to_hold_request =" @Priority(5) @Name('TE_t16_report_reasons_to_hold_request') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't16 report reasons to hold request' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='confirmation of receipt') -> every succ=UnlabeledEvent(activity='t16 report reasons to hold request')) where timer:within(83 min)]#time(83 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=82140000;";

        String epl_TE_t02_check_confirmation_of_receipt =" @Priority(5) @Name('TE_t02_check_confirmation_of_receipt') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't02 check confirmation of receipt' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t12 check document x request unlicensed') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t20 print report y to stop indication') -> every succ=UnlabeledEvent(activity='t02 check confirmation of receipt')) where timer:within(87 min)]#time(87 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86340000;";

        String epl_TE_t04_determine_confirmation_of_receipt =" @Priority(5) @Name('TE_t04_determine_confirmation_of_receipt') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't04 determine confirmation of receipt' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t12 check document x request unlicensed') or every pred=FilterLabeledEvent(activity='t08 draft and send request for advice') or every pred=FilterLabeledEvent(activity='t09-1 process or receive external advice from party 1') or every pred=FilterLabeledEvent(activity='t09-2 process or receive external advice from party 2') or every pred=FilterLabeledEvent(activity='t09-3 process or receive external advice from party 3') or every pred=FilterLabeledEvent(activity='t09-4 process or receive external advice from party 4') or every pred=FilterLabeledEvent(activity='t03 adjust confirmation of receipt') or every pred=FilterLabeledEvent(activity='confirmation of receipt') or every pred=FilterLabeledEvent(activity='t07-1 draft intern advice aspect 1') or every pred=FilterLabeledEvent(activity='t07-2 draft intern advice aspect 2') or every pred=FilterLabeledEvent(activity='t07-3 draft intern advice hold for aspect 3') or every pred=FilterLabeledEvent(activity='t07-4 draft internal advice to hold for type 4') or every pred=FilterLabeledEvent(activity='t07-5 draft intern advice aspect 5') or every pred=FilterLabeledEvent(activity='t20 print report y to stop indication') -> every succ=UnlabeledEvent(activity='t04 determine confirmation of receipt')) where timer:within(87 min)]#time(87 min)  where succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86220000;";

        String epl_TE_t05_print_and_send_confirmation_of_receipt =" @Priority(5) @Name('TE_t05_print_and_send_confirmation_of_receipt') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't05 print and send confirmation of receipt' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t14 determine document x request unlicensed') -> every succ=UnlabeledEvent(activity='t05 print and send confirmation of receipt'))  or ((every pred=FilterLabeledEvent(activity='t04 determine confirmation of receipt') and every inter1 = FilterLabeledEvent(activity='t02 check confirmation of receipt')) -> every succ=UnlabeledEvent(activity='t05 print and send confirmation of receipt')) where timer:within(87 min) ]#time(87 min)  where ( pred.caseID = inter1.caseID and (succ.timestamp - max(pred.timestamp,inter1.timestamp)) >= 0 and (succ.timestamp - max(pred.timestamp,inter1.timestamp)) <=86340000 ) or ( succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86340000 )  ;";

        String epl_TE_t06_determine_necessity_of_stop_advice =" @Priority(5) @Name('TE_t06_determine_necessity_of_stop_advice') insert into TempEvent (eventID, caseID, activity, timestamp) select distinct succ.eventID, pred.caseID, 't06 determine necessity of stop advice' as activity, succ.timestamp as timestamp from pattern [(every pred=FilterLabeledEvent(activity='t14 determine document x request unlicensed') or every pred=FilterLabeledEvent(activity='t05 print and send confirmation of receipt') -> every succ=UnlabeledEvent(activity='t06 determine necessity of stop advice'))  or ((every pred=FilterLabeledEvent(activity='t04 determine confirmation of receipt') and every inter1 = FilterLabeledEvent(activity='t02 check confirmation of receipt')) -> every succ=UnlabeledEvent(activity='t06 determine necessity of stop advice')) where timer:within(87 min) ]#time(87 min)  where ( pred.caseID = inter1.caseID and (succ.timestamp - max(pred.timestamp,inter1.timestamp)) >= 0 and (succ.timestamp - max(pred.timestamp,inter1.timestamp)) <=86340000 ) or ( succ.timestamp - pred.timestamp >= 0 and succ.timestamp - pred.timestamp <=86340000 )  ;";

        String epl_Correlate_t15_print_document_x_request_unlicensed =" @Priority(20) @Name('Correlate_t15_print_document_x_request_unlicensed') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't15 print document x request unlicensed'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t14_determine_document_x_request_unlicensed =" @Priority(20) @Name('Correlate_t14_determine_document_x_request_unlicensed') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't14 determine document x request unlicensed'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t10_determine_necessity_to_stop_indication =" @Priority(20) @Name('Correlate_t10_determine_necessity_to_stop_indication') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't10 determine necessity to stop indication'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t19_determine_report_y_to_stop_indication =" @Priority(20) @Name('Correlate_t19_determine_report_y_to_stop_indication') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't19 determine report y to stop indication'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t20_print_report_y_to_stop_indication =" @Priority(20) @Name('Correlate_t20_print_report_y_to_stop_indication') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't20 print report y to stop indication'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t18_adjust_report_y_to_stop_indicition =" @Priority(20) @Name('Correlate_t18_adjust_report_y_to_stop_indicition') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't18 adjust report y to stop indicition'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t12_check_document_x_request_unlicensed =" @Priority(20) @Name('Correlate_t12_check_document_x_request_unlicensed') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't12 check document x request unlicensed'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t17_check_report_y_to_stop_indication =" @Priority(20) @Name('Correlate_t17_check_report_y_to_stop_indication') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't17 check report y to stop indication'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t03_adjust_confirmation_of_receipt =" @Priority(20) @Name('Correlate_t03_adjust_confirmation_of_receipt') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't03 adjust confirmation of receipt'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t07_1_draft_intern_advice_aspect_1 =" @Priority(20) @Name('Correlate_t07_1_draft_intern_advice_aspect_1') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't07-1 draft intern advice aspect 1'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t07_2_draft_intern_advice_aspect_2 =" @Priority(20) @Name('Correlate_t07_2_draft_intern_advice_aspect_2') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't07-2 draft intern advice aspect 2'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3 =" @Priority(20) @Name('Correlate_t07_3_draft_intern_advice_hold_for_aspect_3') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't07-3 draft intern advice hold for aspect 3'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4 =" @Priority(20) @Name('Correlate_t07_4_draft_internal_advice_to_hold_for_type_4') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't07-4 draft internal advice to hold for type 4'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t07_5_draft_intern_advice_aspect_5 =" @Priority(20) @Name('Correlate_t07_5_draft_intern_advice_aspect_5') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't07-5 draft intern advice aspect 5'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t08_draft_and_send_request_for_advice =" @Priority(20) @Name('Correlate_t08_draft_and_send_request_for_advice') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't08 draft and send request for advice'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t09_1_process_or_receive_external_advice_from_party_1 =" @Priority(20) @Name('Correlate_t09_1_process_or_receive_external_advice_from_party_1') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't09-1 process or receive external advice from party 1'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t09_2_process_or_receive_external_advice_from_party_2 =" @Priority(20) @Name('Correlate_t09_2_process_or_receive_external_advice_from_party_2') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't09-2 process or receive external advice from party 2'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t09_3_process_or_receive_external_advice_from_party_3 =" @Priority(20) @Name('Correlate_t09_3_process_or_receive_external_advice_from_party_3') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't09-3 process or receive external advice from party 3'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t09_4_process_or_receive_external_advice_from_party_4 =" @Priority(20) @Name('Correlate_t09_4_process_or_receive_external_advice_from_party_4') insert into LabeledEvent (ceventID, aseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't09-4 process or receive external advice from party 4'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t11_create_document_x_request_unlicensed =" @Priority(20) @Name('Correlate_t11_create_document_x_request_unlicensed') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't11 create document x request unlicensed'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t16_report_reasons_to_hold_request =" @Priority(20) @Name('Correlate_t16_report_reasons_to_hold_request') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't16 report reasons to hold request'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t02_check_confirmation_of_receipt =" @Priority(20) @Name('Correlate_t02_check_confirmation_of_receipt') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't02 check confirmation of receipt'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t04_determine_confirmation_of_receipt =" @Priority(20) @Name('Correlate_t04_determine_confirmation_of_receipt') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't04 determine confirmation of receipt'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t05_print_and_send_confirmation_of_receipt =" @Priority(20) @Name('Correlate_t05_print_and_send_confirmation_of_receipt') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't05 print and send confirmation of receipt'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Correlate_t06_determine_necessity_of_stop_advice =" @Priority(20) @Name('Correlate_t06_determine_necessity_of_stop_advice') insert into LabeledEvent (eventID, caseID, activity , timestamp, probability) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , ( count(caseID, group_by:(timestamp,activity,caseID)) /count(all caseID, group_by:(timestamp,activity))) as probability from TempEvent.win:time_batch(1 sec) as T  where activity = 't06 determine necessity of stop advice'  group by eventID, caseID order by timestamp desc limit 3 ; ";

        String epl_Filter_t15_print_document_x_request_unlicensed =" @Priority(50) @Name('Filter_t15_print_document_x_request_unlicensed') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't15 print document x request unlicensed'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t14_determine_document_x_request_unlicensed =" @Priority(50) @Name('Filter_t14_determine_document_x_request_unlicensed') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't14 determine document x request unlicensed'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t10_determine_necessity_to_stop_indication =" @Priority(50) @Name('Filter_t10_determine_necessity_to_stop_indication') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't10 determine necessity to stop indication'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t19_determine_report_y_to_stop_indication =" @Priority(50) @Name('Filter_t19_determine_report_y_to_stop_indication') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't19 determine report y to stop indication'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t20_print_report_y_to_stop_indication =" @Priority(50) @Name('Filter_t20_print_report_y_to_stop_indication') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't20 print report y to stop indication'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t18_adjust_report_y_to_stop_indicition =" @Priority(50) @Name('Filter_t18_adjust_report_y_to_stop_indicition') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't18 adjust report y to stop indicition'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t12_check_document_x_request_unlicensed =" @Priority(50) @Name('Filter_t12_check_document_x_request_unlicensed') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't12 check document x request unlicensed'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t17_check_report_y_to_stop_indication =" @Priority(50) @Name('Filter_t17_check_report_y_to_stop_indication') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't17 check report y to stop indication'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t03_adjust_confirmation_of_receipt =" @Priority(50) @Name('Filter_t03_adjust_confirmation_of_receipt') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't03 adjust confirmation of receipt'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t07_1_draft_intern_advice_aspect_1 =" @Priority(50) @Name('Filter_t07_1_draft_intern_advice_aspect_1') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't07-1 draft intern advice aspect 1'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t07_2_draft_intern_advice_aspect_2 =" @Priority(50) @Name('Filter_t07_2_draft_intern_advice_aspect_2') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't07-2 draft intern advice aspect 2'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t07_3_draft_intern_advice_hold_for_aspect_3 =" @Priority(50) @Name('Filter_t07_3_draft_intern_advice_hold_for_aspect_3') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't07-3 draft intern advice hold for aspect 3'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t07_4_draft_internal_advice_to_hold_for_type_4 =" @Priority(50) @Name('Filter_t07_4_draft_internal_advice_to_hold_for_type_4') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't07-4 draft internal advice to hold for type 4'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t07_5_draft_intern_advice_aspect_5 =" @Priority(50) @Name('Filter_t07_5_draft_intern_advice_aspect_5') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't07-5 draft intern advice aspect 5'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t08_draft_and_send_request_for_advice =" @Priority(50) @Name('Filter_t08_draft_and_send_request_for_advice') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't08 draft and send request for advice'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t09_1_process_or_receive_external_advice_from_party_1 =" @Priority(50) @Name('Filter_t09_1_process_or_receive_external_advice_from_party_1') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't09-1 process or receive external advice from party 1'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t09_2_process_or_receive_external_advice_from_party_2 =" @Priority(50) @Name('Filter_t09_2_process_or_receive_external_advice_from_party_2') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't09-2 process or receive external advice from party 2'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t09_3_process_or_receive_external_advice_from_party_3 =" @Priority(50) @Name('Filter_t09_3_process_or_receive_external_advice_from_party_3') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't09-3 process or receive external advice from party 3'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t09_4_process_or_receive_external_advice_from_party_4 =" @Priority(50) @Name('Filter_t09_4_process_or_receive_external_advice_from_party_4') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't09-4 process or receive external advice from party 4'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t11_create_document_x_request_unlicensed =" @Priority(50) @Name('Filter_t11_create_document_x_request_unlicensed') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't11 create document x request unlicensed'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t16_report_reasons_to_hold_request =" @Priority(50) @Name('Filter_t16_report_reasons_to_hold_request') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't16 report reasons to hold request'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t02_check_confirmation_of_receipt =" @Priority(50) @Name('Filter_t02_check_confirmation_of_receipt') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't02 check confirmation of receipt'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t04_determine_confirmation_of_receipt =" @Priority(50) @Name('Filter_t04_determine_confirmation_of_receipt') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't04 determine confirmation of receipt'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t05_print_and_send_confirmation_of_receipt =" @Priority(50) @Name('Filter_t05_print_and_send_confirmation_of_receipt') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from  LabeledEvent.win:time(1 sec) as T where activity = 't05 print and send confirmation of receipt'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

        String epl_Filter_t06_determine_necessity_of_stop_advice =" @Priority(50) @Name('Filter_t06_determine_necessity_of_stop_advice') insert into FilterLabeledEvent (eventID, caseID, activity , timestamp, probability, initTime) select distinct T.eventID, caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability, "+initTime+"   from LabeledEvent.win:time(1 sec) as T where activity = 't06 determine necessity of stop advice'  and probability >= 0.3 group by eventID, caseID, probability order by probability desc limit 1 ;";

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

        EPCompiled epCompiled_start_case_confirmation_of_receipt;
        EPDeployment deployment_start_case_confirmation_of_receipt;

        EPCompiled epCompiled_TE_t15_print_document_x_request_unlicensed;
        EPDeployment deployment_TE_t15_print_document_x_request_unlicensed;

        EPCompiled epCompiled_TE_t14_determine_document_x_request_unlicensed;
        EPDeployment deployment_TE_t14_determine_document_x_request_unlicensed;

        EPCompiled epCompiled_TE_t10_determine_necessity_to_stop_indication;
        EPDeployment deployment_TE_t10_determine_necessity_to_stop_indication;

        EPCompiled epCompiled_TE_t19_determine_report_y_to_stop_indication;
        EPDeployment deployment_TE_t19_determine_report_y_to_stop_indication;

        EPCompiled epCompiled_TE_t20_print_report_y_to_stop_indication;
        EPDeployment deployment_TE_t20_print_report_y_to_stop_indication;

        EPCompiled epCompiled_TE_t18_adjust_report_y_to_stop_indicition;
        EPDeployment deployment_TE_t18_adjust_report_y_to_stop_indicition;

        EPCompiled epCompiled_TE_t12_check_document_x_request_unlicensed;
        EPDeployment deployment_TE_t12_check_document_x_request_unlicensed;

        EPCompiled epCompiled_TE_t17_check_report_y_to_stop_indication;
        EPDeployment deployment_TE_t17_check_report_y_to_stop_indication;

        EPCompiled epCompiled_TE_t03_adjust_confirmation_of_receipt;
        EPDeployment deployment_TE_t03_adjust_confirmation_of_receipt;

        EPCompiled epCompiled_TE_t07_1_draft_intern_advice_aspect_1;
        EPDeployment deployment_TE_t07_1_draft_intern_advice_aspect_1;

        EPCompiled epCompiled_TE_t07_2_draft_intern_advice_aspect_2;
        EPDeployment deployment_TE_t07_2_draft_intern_advice_aspect_2;

        EPCompiled epCompiled_TE_t07_3_draft_intern_advice_hold_for_aspect_3;
        EPDeployment deployment_TE_t07_3_draft_intern_advice_hold_for_aspect_3;

        EPCompiled epCompiled_TE_t07_4_draft_internal_advice_to_hold_for_type_4;
        EPDeployment deployment_TE_t07_4_draft_internal_advice_to_hold_for_type_4;

        EPCompiled epCompiled_TE_t07_5_draft_intern_advice_aspect_5;
        EPDeployment deployment_TE_t07_5_draft_intern_advice_aspect_5;

        EPCompiled epCompiled_TE_t08_draft_and_send_request_for_advice;
        EPDeployment deployment_TE_t08_draft_and_send_request_for_advice;

        EPCompiled epCompiled_TE_t09_1_process_or_receive_external_advice_from_party_1;
        EPDeployment deployment_TE_t09_1_process_or_receive_external_advice_from_party_1;

        EPCompiled epCompiled_TE_t09_2_process_or_receive_external_advice_from_party_2;
        EPDeployment deployment_TE_t09_2_process_or_receive_external_advice_from_party_2;

        EPCompiled epCompiled_TE_t09_3_process_or_receive_external_advice_from_party_3;
        EPDeployment deployment_TE_t09_3_process_or_receive_external_advice_from_party_3;

        EPCompiled epCompiled_TE_t09_4_process_or_receive_external_advice_from_party_4;
        EPDeployment deployment_TE_t09_4_process_or_receive_external_advice_from_party_4;

        EPCompiled epCompiled_TE_t11_create_document_x_request_unlicensed;
        EPDeployment deployment_TE_t11_create_document_x_request_unlicensed;

        EPCompiled epCompiled_TE_t16_report_reasons_to_hold_request;
        EPDeployment deployment_TE_t16_report_reasons_to_hold_request;

        EPCompiled epCompiled_TE_t02_check_confirmation_of_receipt;
        EPDeployment deployment_TE_t02_check_confirmation_of_receipt;

        EPCompiled epCompiled_TE_t04_determine_confirmation_of_receipt;
        EPDeployment deployment_TE_t04_determine_confirmation_of_receipt;

        EPCompiled epCompiled_TE_t05_print_and_send_confirmation_of_receipt;
        EPDeployment deployment_TE_t05_print_and_send_confirmation_of_receipt;

        EPCompiled epCompiled_TE_t06_determine_necessity_of_stop_advice;
        EPDeployment deployment_TE_t06_determine_necessity_of_stop_advice;

        EPCompiled epCompiled_Correlate_t15_print_document_x_request_unlicensed;
        EPDeployment deployment_Correlate_t15_print_document_x_request_unlicensed;

        EPCompiled epCompiled_Correlate_t14_determine_document_x_request_unlicensed;
        EPDeployment deployment_Correlate_t14_determine_document_x_request_unlicensed;

        EPCompiled epCompiled_Correlate_t10_determine_necessity_to_stop_indication;
        EPDeployment deployment_Correlate_t10_determine_necessity_to_stop_indication;

        EPCompiled epCompiled_Correlate_t19_determine_report_y_to_stop_indication;
        EPDeployment deployment_Correlate_t19_determine_report_y_to_stop_indication;

        EPCompiled epCompiled_Correlate_t20_print_report_y_to_stop_indication;
        EPDeployment deployment_Correlate_t20_print_report_y_to_stop_indication;

        EPCompiled epCompiled_Correlate_t18_adjust_report_y_to_stop_indicition;
        EPDeployment deployment_Correlate_t18_adjust_report_y_to_stop_indicition;

        EPCompiled epCompiled_Correlate_t12_check_document_x_request_unlicensed;
        EPDeployment deployment_Correlate_t12_check_document_x_request_unlicensed;

        EPCompiled epCompiled_Correlate_t17_check_report_y_to_stop_indication;
        EPDeployment deployment_Correlate_t17_check_report_y_to_stop_indication;

        EPCompiled epCompiled_Correlate_t03_adjust_confirmation_of_receipt;
        EPDeployment deployment_Correlate_t03_adjust_confirmation_of_receipt;

        EPCompiled epCompiled_Correlate_t07_1_draft_intern_advice_aspect_1;
        EPDeployment deployment_Correlate_t07_1_draft_intern_advice_aspect_1;

        EPCompiled epCompiled_Correlate_t07_2_draft_intern_advice_aspect_2;
        EPDeployment deployment_Correlate_t07_2_draft_intern_advice_aspect_2;

        EPCompiled epCompiled_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3;
        EPDeployment deployment_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3;

        EPCompiled epCompiled_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4;
        EPDeployment deployment_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4;

        EPCompiled epCompiled_Correlate_t07_5_draft_intern_advice_aspect_5;
        EPDeployment deployment_Correlate_t07_5_draft_intern_advice_aspect_5;

        EPCompiled epCompiled_Correlate_t08_draft_and_send_request_for_advice;
        EPDeployment deployment_Correlate_t08_draft_and_send_request_for_advice;

        EPCompiled epCompiled_Correlate_t09_1_process_or_receive_external_advice_from_party_1;
        EPDeployment deployment_Correlate_t09_1_process_or_receive_external_advice_from_party_1;

        EPCompiled epCompiled_Correlate_t09_2_process_or_receive_external_advice_from_party_2;
        EPDeployment deployment_Correlate_t09_2_process_or_receive_external_advice_from_party_2;

        EPCompiled epCompiled_Correlate_t09_3_process_or_receive_external_advice_from_party_3;
        EPDeployment deployment_Correlate_t09_3_process_or_receive_external_advice_from_party_3;

        EPCompiled epCompiled_Correlate_t09_4_process_or_receive_external_advice_from_party_4;
        EPDeployment deployment_Correlate_t09_4_process_or_receive_external_advice_from_party_4;

        EPCompiled epCompiled_Correlate_t11_create_document_x_request_unlicensed;
        EPDeployment deployment_Correlate_t11_create_document_x_request_unlicensed;

        EPCompiled epCompiled_Correlate_t16_report_reasons_to_hold_request;
        EPDeployment deployment_Correlate_t16_report_reasons_to_hold_request;

        EPCompiled epCompiled_Correlate_t02_check_confirmation_of_receipt;
        EPDeployment deployment_Correlate_t02_check_confirmation_of_receipt;

        EPCompiled epCompiled_Correlate_t04_determine_confirmation_of_receipt;
        EPDeployment deployment_Correlate_t04_determine_confirmation_of_receipt;

        EPCompiled epCompiled_Correlate_t05_print_and_send_confirmation_of_receipt;
        EPDeployment deployment_Correlate_t05_print_and_send_confirmation_of_receipt;

        EPCompiled epCompiled_Correlate_t06_determine_necessity_of_stop_advice;
        EPDeployment deployment_Correlate_t06_determine_necessity_of_stop_advice;

        EPCompiled epCompiled_Filter_t15_print_document_x_request_unlicensed;
        EPDeployment deployment_Filter_t15_print_document_x_request_unlicensed;

        EPCompiled epCompiled_Filter_t14_determine_document_x_request_unlicensed;
        EPDeployment deployment_Filter_t14_determine_document_x_request_unlicensed;

        EPCompiled epCompiled_Filter_t10_determine_necessity_to_stop_indication;
        EPDeployment deployment_Filter_t10_determine_necessity_to_stop_indication;

        EPCompiled epCompiled_Filter_t19_determine_report_y_to_stop_indication;
        EPDeployment deployment_Filter_t19_determine_report_y_to_stop_indication;

        EPCompiled epCompiled_Filter_t20_print_report_y_to_stop_indication;
        EPDeployment deployment_Filter_t20_print_report_y_to_stop_indication;

        EPCompiled epCompiled_Filter_t18_adjust_report_y_to_stop_indicition;
        EPDeployment deployment_Filter_t18_adjust_report_y_to_stop_indicition;

        EPCompiled epCompiled_Filter_t12_check_document_x_request_unlicensed;
        EPDeployment deployment_Filter_t12_check_document_x_request_unlicensed;

        EPCompiled epCompiled_Filter_t17_check_report_y_to_stop_indication;
        EPDeployment deployment_Filter_t17_check_report_y_to_stop_indication;

        EPCompiled epCompiled_Filter_t03_adjust_confirmation_of_receipt;
        EPDeployment deployment_Filter_t03_adjust_confirmation_of_receipt;

        EPCompiled epCompiled_Filter_t07_1_draft_intern_advice_aspect_1;
        EPDeployment deployment_Filter_t07_1_draft_intern_advice_aspect_1;

        EPCompiled epCompiled_Filter_t07_2_draft_intern_advice_aspect_2;
        EPDeployment deployment_Filter_t07_2_draft_intern_advice_aspect_2;

        EPCompiled epCompiled_Filter_t07_3_draft_intern_advice_hold_for_aspect_3;
        EPDeployment deployment_Filter_t07_3_draft_intern_advice_hold_for_aspect_3;

        EPCompiled epCompiled_Filter_t07_4_draft_internal_advice_to_hold_for_type_4;
        EPDeployment deployment_Filter_t07_4_draft_internal_advice_to_hold_for_type_4;

        EPCompiled epCompiled_Filter_t07_5_draft_intern_advice_aspect_5;
        EPDeployment deployment_Filter_t07_5_draft_intern_advice_aspect_5;

        EPCompiled epCompiled_Filter_t08_draft_and_send_request_for_advice;
        EPDeployment deployment_Filter_t08_draft_and_send_request_for_advice;

        EPCompiled epCompiled_Filter_t09_1_process_or_receive_external_advice_from_party_1;
        EPDeployment deployment_Filter_t09_1_process_or_receive_external_advice_from_party_1;

        EPCompiled epCompiled_Filter_t09_2_process_or_receive_external_advice_from_party_2;
        EPDeployment deployment_Filter_t09_2_process_or_receive_external_advice_from_party_2;

        EPCompiled epCompiled_Filter_t09_3_process_or_receive_external_advice_from_party_3;
        EPDeployment deployment_Filter_t09_3_process_or_receive_external_advice_from_party_3;

        EPCompiled epCompiled_Filter_t09_4_process_or_receive_external_advice_from_party_4;
        EPDeployment deployment_Filter_t09_4_process_or_receive_external_advice_from_party_4;

        EPCompiled epCompiled_Filter_t11_create_document_x_request_unlicensed;
        EPDeployment deployment_Filter_t11_create_document_x_request_unlicensed;

        EPCompiled epCompiled_Filter_t16_report_reasons_to_hold_request;
        EPDeployment deployment_Filter_t16_report_reasons_to_hold_request;

        EPCompiled epCompiled_Filter_t02_check_confirmation_of_receipt;
        EPDeployment deployment_Filter_t02_check_confirmation_of_receipt;

        EPCompiled epCompiled_Filter_t04_determine_confirmation_of_receipt;
        EPDeployment deployment_Filter_t04_determine_confirmation_of_receipt;

        EPCompiled epCompiled_Filter_t05_print_and_send_confirmation_of_receipt;
        EPDeployment deployment_Filter_t05_print_and_send_confirmation_of_receipt;

        EPCompiled epCompiled_Filter_t06_determine_necessity_of_stop_advice;
        EPDeployment deployment_Filter_t06_determine_necessity_of_stop_advice;

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

            epCompiled_start_case_confirmation_of_receipt = compiler.compile(epl_start_case_confirmation_of_receipt, arguments);
            deployment_start_case_confirmation_of_receipt = deploymentService.deploy(epCompiled_start_case_confirmation_of_receipt);

            epCompiled_TE_t15_print_document_x_request_unlicensed = compiler.compile(epl_TE_t15_print_document_x_request_unlicensed, arguments);
            deployment_TE_t15_print_document_x_request_unlicensed = deploymentService.deploy(epCompiled_TE_t15_print_document_x_request_unlicensed);

            epCompiled_TE_t14_determine_document_x_request_unlicensed = compiler.compile(epl_TE_t14_determine_document_x_request_unlicensed, arguments);
            deployment_TE_t14_determine_document_x_request_unlicensed = deploymentService.deploy(epCompiled_TE_t14_determine_document_x_request_unlicensed);

            epCompiled_TE_t10_determine_necessity_to_stop_indication = compiler.compile(epl_TE_t10_determine_necessity_to_stop_indication, arguments);
            deployment_TE_t10_determine_necessity_to_stop_indication = deploymentService.deploy(epCompiled_TE_t10_determine_necessity_to_stop_indication);

            epCompiled_TE_t19_determine_report_y_to_stop_indication = compiler.compile(epl_TE_t19_determine_report_y_to_stop_indication, arguments);
            deployment_TE_t19_determine_report_y_to_stop_indication = deploymentService.deploy(epCompiled_TE_t19_determine_report_y_to_stop_indication);

            epCompiled_TE_t20_print_report_y_to_stop_indication = compiler.compile(epl_TE_t20_print_report_y_to_stop_indication, arguments);
            deployment_TE_t20_print_report_y_to_stop_indication = deploymentService.deploy(epCompiled_TE_t20_print_report_y_to_stop_indication);

            epCompiled_TE_t18_adjust_report_y_to_stop_indicition = compiler.compile(epl_TE_t18_adjust_report_y_to_stop_indicition, arguments);
            deployment_TE_t18_adjust_report_y_to_stop_indicition = deploymentService.deploy(epCompiled_TE_t18_adjust_report_y_to_stop_indicition);

            epCompiled_TE_t12_check_document_x_request_unlicensed = compiler.compile(epl_TE_t12_check_document_x_request_unlicensed, arguments);
            deployment_TE_t12_check_document_x_request_unlicensed = deploymentService.deploy(epCompiled_TE_t12_check_document_x_request_unlicensed);

            epCompiled_TE_t17_check_report_y_to_stop_indication = compiler.compile(epl_TE_t17_check_report_y_to_stop_indication, arguments);
            deployment_TE_t17_check_report_y_to_stop_indication = deploymentService.deploy(epCompiled_TE_t17_check_report_y_to_stop_indication);

            epCompiled_TE_t03_adjust_confirmation_of_receipt = compiler.compile(epl_TE_t03_adjust_confirmation_of_receipt, arguments);
            deployment_TE_t03_adjust_confirmation_of_receipt = deploymentService.deploy(epCompiled_TE_t03_adjust_confirmation_of_receipt);

            epCompiled_TE_t07_1_draft_intern_advice_aspect_1 = compiler.compile(epl_TE_t07_1_draft_intern_advice_aspect_1, arguments);
            deployment_TE_t07_1_draft_intern_advice_aspect_1 = deploymentService.deploy(epCompiled_TE_t07_1_draft_intern_advice_aspect_1);

            epCompiled_TE_t07_2_draft_intern_advice_aspect_2 = compiler.compile(epl_TE_t07_2_draft_intern_advice_aspect_2, arguments);
            deployment_TE_t07_2_draft_intern_advice_aspect_2 = deploymentService.deploy(epCompiled_TE_t07_2_draft_intern_advice_aspect_2);

            epCompiled_TE_t07_3_draft_intern_advice_hold_for_aspect_3 = compiler.compile(epl_TE_t07_3_draft_intern_advice_hold_for_aspect_3, arguments);
            deployment_TE_t07_3_draft_intern_advice_hold_for_aspect_3 = deploymentService.deploy(epCompiled_TE_t07_3_draft_intern_advice_hold_for_aspect_3);

            epCompiled_TE_t07_4_draft_internal_advice_to_hold_for_type_4 = compiler.compile(epl_TE_t07_4_draft_internal_advice_to_hold_for_type_4, arguments);
            deployment_TE_t07_4_draft_internal_advice_to_hold_for_type_4 = deploymentService.deploy(epCompiled_TE_t07_4_draft_internal_advice_to_hold_for_type_4);

            epCompiled_TE_t07_5_draft_intern_advice_aspect_5 = compiler.compile(epl_TE_t07_5_draft_intern_advice_aspect_5, arguments);
            deployment_TE_t07_5_draft_intern_advice_aspect_5 = deploymentService.deploy(epCompiled_TE_t07_5_draft_intern_advice_aspect_5);

            epCompiled_TE_t08_draft_and_send_request_for_advice = compiler.compile(epl_TE_t08_draft_and_send_request_for_advice, arguments);
            deployment_TE_t08_draft_and_send_request_for_advice = deploymentService.deploy(epCompiled_TE_t08_draft_and_send_request_for_advice);

            epCompiled_TE_t09_1_process_or_receive_external_advice_from_party_1 = compiler.compile(epl_TE_t09_1_process_or_receive_external_advice_from_party_1, arguments);
            deployment_TE_t09_1_process_or_receive_external_advice_from_party_1 = deploymentService.deploy(epCompiled_TE_t09_1_process_or_receive_external_advice_from_party_1);

            epCompiled_TE_t09_2_process_or_receive_external_advice_from_party_2 = compiler.compile(epl_TE_t09_2_process_or_receive_external_advice_from_party_2, arguments);
            deployment_TE_t09_2_process_or_receive_external_advice_from_party_2 = deploymentService.deploy(epCompiled_TE_t09_2_process_or_receive_external_advice_from_party_2);

            epCompiled_TE_t09_3_process_or_receive_external_advice_from_party_3 = compiler.compile(epl_TE_t09_3_process_or_receive_external_advice_from_party_3, arguments);
            deployment_TE_t09_3_process_or_receive_external_advice_from_party_3 = deploymentService.deploy(epCompiled_TE_t09_3_process_or_receive_external_advice_from_party_3);

            epCompiled_TE_t09_4_process_or_receive_external_advice_from_party_4 = compiler.compile(epl_TE_t09_4_process_or_receive_external_advice_from_party_4, arguments);
            deployment_TE_t09_4_process_or_receive_external_advice_from_party_4 = deploymentService.deploy(epCompiled_TE_t09_4_process_or_receive_external_advice_from_party_4);

            epCompiled_TE_t11_create_document_x_request_unlicensed = compiler.compile(epl_TE_t11_create_document_x_request_unlicensed, arguments);
            deployment_TE_t11_create_document_x_request_unlicensed = deploymentService.deploy(epCompiled_TE_t11_create_document_x_request_unlicensed);

            epCompiled_TE_t16_report_reasons_to_hold_request = compiler.compile(epl_TE_t16_report_reasons_to_hold_request, arguments);
            deployment_TE_t16_report_reasons_to_hold_request = deploymentService.deploy(epCompiled_TE_t16_report_reasons_to_hold_request);

            epCompiled_TE_t02_check_confirmation_of_receipt = compiler.compile(epl_TE_t02_check_confirmation_of_receipt, arguments);
            deployment_TE_t02_check_confirmation_of_receipt = deploymentService.deploy(epCompiled_TE_t02_check_confirmation_of_receipt);

            epCompiled_TE_t04_determine_confirmation_of_receipt = compiler.compile(epl_TE_t04_determine_confirmation_of_receipt, arguments);
            deployment_TE_t04_determine_confirmation_of_receipt = deploymentService.deploy(epCompiled_TE_t04_determine_confirmation_of_receipt);

            epCompiled_TE_t05_print_and_send_confirmation_of_receipt = compiler.compile(epl_TE_t05_print_and_send_confirmation_of_receipt, arguments);
            deployment_TE_t05_print_and_send_confirmation_of_receipt = deploymentService.deploy(epCompiled_TE_t05_print_and_send_confirmation_of_receipt);

            epCompiled_TE_t06_determine_necessity_of_stop_advice = compiler.compile(epl_TE_t06_determine_necessity_of_stop_advice, arguments);
            deployment_TE_t06_determine_necessity_of_stop_advice = deploymentService.deploy(epCompiled_TE_t06_determine_necessity_of_stop_advice);

            CompilerArguments arguments2 = new CompilerArguments(configuration);
            arguments2.getPath().add(runtime.getRuntimePath());

            epCompiled_Correlate_t15_print_document_x_request_unlicensed = EPCompilerProvider.getCompiler().compile(epl_Correlate_t15_print_document_x_request_unlicensed, arguments2);
            deployment_Correlate_t15_print_document_x_request_unlicensed = deploymentService.deploy(epCompiled_Correlate_t15_print_document_x_request_unlicensed);

            epCompiled_Correlate_t14_determine_document_x_request_unlicensed = EPCompilerProvider.getCompiler().compile(epl_Correlate_t14_determine_document_x_request_unlicensed, arguments2);
            deployment_Correlate_t14_determine_document_x_request_unlicensed = deploymentService.deploy(epCompiled_Correlate_t14_determine_document_x_request_unlicensed);

            epCompiled_Correlate_t10_determine_necessity_to_stop_indication = EPCompilerProvider.getCompiler().compile(epl_Correlate_t10_determine_necessity_to_stop_indication, arguments2);
            deployment_Correlate_t10_determine_necessity_to_stop_indication = deploymentService.deploy(epCompiled_Correlate_t10_determine_necessity_to_stop_indication);

            epCompiled_Correlate_t19_determine_report_y_to_stop_indication = EPCompilerProvider.getCompiler().compile(epl_Correlate_t19_determine_report_y_to_stop_indication, arguments2);
            deployment_Correlate_t19_determine_report_y_to_stop_indication = deploymentService.deploy(epCompiled_Correlate_t19_determine_report_y_to_stop_indication);

            epCompiled_Correlate_t20_print_report_y_to_stop_indication = EPCompilerProvider.getCompiler().compile(epl_Correlate_t20_print_report_y_to_stop_indication, arguments2);
            deployment_Correlate_t20_print_report_y_to_stop_indication = deploymentService.deploy(epCompiled_Correlate_t20_print_report_y_to_stop_indication);

            epCompiled_Correlate_t18_adjust_report_y_to_stop_indicition = EPCompilerProvider.getCompiler().compile(epl_Correlate_t18_adjust_report_y_to_stop_indicition, arguments2);
            deployment_Correlate_t18_adjust_report_y_to_stop_indicition = deploymentService.deploy(epCompiled_Correlate_t18_adjust_report_y_to_stop_indicition);

            epCompiled_Correlate_t12_check_document_x_request_unlicensed = EPCompilerProvider.getCompiler().compile(epl_Correlate_t12_check_document_x_request_unlicensed, arguments2);
            deployment_Correlate_t12_check_document_x_request_unlicensed = deploymentService.deploy(epCompiled_Correlate_t12_check_document_x_request_unlicensed);

            epCompiled_Correlate_t17_check_report_y_to_stop_indication = EPCompilerProvider.getCompiler().compile(epl_Correlate_t17_check_report_y_to_stop_indication, arguments2);
            deployment_Correlate_t17_check_report_y_to_stop_indication = deploymentService.deploy(epCompiled_Correlate_t17_check_report_y_to_stop_indication);

            epCompiled_Correlate_t03_adjust_confirmation_of_receipt = EPCompilerProvider.getCompiler().compile(epl_Correlate_t03_adjust_confirmation_of_receipt, arguments2);
            deployment_Correlate_t03_adjust_confirmation_of_receipt = deploymentService.deploy(epCompiled_Correlate_t03_adjust_confirmation_of_receipt);

            epCompiled_Correlate_t07_1_draft_intern_advice_aspect_1 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t07_1_draft_intern_advice_aspect_1, arguments2);
            deployment_Correlate_t07_1_draft_intern_advice_aspect_1 = deploymentService.deploy(epCompiled_Correlate_t07_1_draft_intern_advice_aspect_1);

            epCompiled_Correlate_t07_2_draft_intern_advice_aspect_2 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t07_2_draft_intern_advice_aspect_2, arguments2);
            deployment_Correlate_t07_2_draft_intern_advice_aspect_2 = deploymentService.deploy(epCompiled_Correlate_t07_2_draft_intern_advice_aspect_2);

            epCompiled_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3, arguments2);
            deployment_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3 = deploymentService.deploy(epCompiled_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3);

            epCompiled_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4, arguments2);
            deployment_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4 = deploymentService.deploy(epCompiled_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4);

            epCompiled_Correlate_t07_5_draft_intern_advice_aspect_5 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t07_5_draft_intern_advice_aspect_5, arguments2);
            deployment_Correlate_t07_5_draft_intern_advice_aspect_5 = deploymentService.deploy(epCompiled_Correlate_t07_5_draft_intern_advice_aspect_5);

            epCompiled_Correlate_t08_draft_and_send_request_for_advice = EPCompilerProvider.getCompiler().compile(epl_Correlate_t08_draft_and_send_request_for_advice, arguments2);
            deployment_Correlate_t08_draft_and_send_request_for_advice = deploymentService.deploy(epCompiled_Correlate_t08_draft_and_send_request_for_advice);

            epCompiled_Correlate_t09_1_process_or_receive_external_advice_from_party_1 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t09_1_process_or_receive_external_advice_from_party_1, arguments2);
            deployment_Correlate_t09_1_process_or_receive_external_advice_from_party_1 = deploymentService.deploy(epCompiled_Correlate_t09_1_process_or_receive_external_advice_from_party_1);

            epCompiled_Correlate_t09_2_process_or_receive_external_advice_from_party_2 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t09_2_process_or_receive_external_advice_from_party_2, arguments2);
            deployment_Correlate_t09_2_process_or_receive_external_advice_from_party_2 = deploymentService.deploy(epCompiled_Correlate_t09_2_process_or_receive_external_advice_from_party_2);

            epCompiled_Correlate_t09_3_process_or_receive_external_advice_from_party_3 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t09_3_process_or_receive_external_advice_from_party_3, arguments2);
            deployment_Correlate_t09_3_process_or_receive_external_advice_from_party_3 = deploymentService.deploy(epCompiled_Correlate_t09_3_process_or_receive_external_advice_from_party_3);

            epCompiled_Correlate_t09_4_process_or_receive_external_advice_from_party_4 = EPCompilerProvider.getCompiler().compile(epl_Correlate_t09_4_process_or_receive_external_advice_from_party_4, arguments2);
            deployment_Correlate_t09_4_process_or_receive_external_advice_from_party_4 = deploymentService.deploy(epCompiled_Correlate_t09_4_process_or_receive_external_advice_from_party_4);

            epCompiled_Correlate_t11_create_document_x_request_unlicensed = EPCompilerProvider.getCompiler().compile(epl_Correlate_t11_create_document_x_request_unlicensed, arguments2);
            deployment_Correlate_t11_create_document_x_request_unlicensed = deploymentService.deploy(epCompiled_Correlate_t11_create_document_x_request_unlicensed);

            epCompiled_Correlate_t16_report_reasons_to_hold_request = EPCompilerProvider.getCompiler().compile(epl_Correlate_t16_report_reasons_to_hold_request, arguments2);
            deployment_Correlate_t16_report_reasons_to_hold_request = deploymentService.deploy(epCompiled_Correlate_t16_report_reasons_to_hold_request);

            epCompiled_Correlate_t02_check_confirmation_of_receipt = EPCompilerProvider.getCompiler().compile(epl_Correlate_t02_check_confirmation_of_receipt, arguments2);
            deployment_Correlate_t02_check_confirmation_of_receipt = deploymentService.deploy(epCompiled_Correlate_t02_check_confirmation_of_receipt);

            epCompiled_Correlate_t04_determine_confirmation_of_receipt = EPCompilerProvider.getCompiler().compile(epl_Correlate_t04_determine_confirmation_of_receipt, arguments2);
            deployment_Correlate_t04_determine_confirmation_of_receipt = deploymentService.deploy(epCompiled_Correlate_t04_determine_confirmation_of_receipt);

            epCompiled_Correlate_t05_print_and_send_confirmation_of_receipt = EPCompilerProvider.getCompiler().compile(epl_Correlate_t05_print_and_send_confirmation_of_receipt, arguments2);
            deployment_Correlate_t05_print_and_send_confirmation_of_receipt = deploymentService.deploy(epCompiled_Correlate_t05_print_and_send_confirmation_of_receipt);

            epCompiled_Correlate_t06_determine_necessity_of_stop_advice = EPCompilerProvider.getCompiler().compile(epl_Correlate_t06_determine_necessity_of_stop_advice, arguments2);
            deployment_Correlate_t06_determine_necessity_of_stop_advice = deploymentService.deploy(epCompiled_Correlate_t06_determine_necessity_of_stop_advice);


            CompilerArguments arguments3 = new CompilerArguments(configuration);
            arguments3.getPath().add(runtime.getRuntimePath());

            epCompiled_Filter_t15_print_document_x_request_unlicensed = EPCompilerProvider.getCompiler().compile(epl_Filter_t15_print_document_x_request_unlicensed, arguments3);
            deployment_Filter_t15_print_document_x_request_unlicensed = deploymentService.deploy(epCompiled_Filter_t15_print_document_x_request_unlicensed);

            epCompiled_Filter_t14_determine_document_x_request_unlicensed = EPCompilerProvider.getCompiler().compile(epl_Filter_t14_determine_document_x_request_unlicensed, arguments3);
            deployment_Filter_t14_determine_document_x_request_unlicensed = deploymentService.deploy(epCompiled_Filter_t14_determine_document_x_request_unlicensed);

            epCompiled_Filter_t10_determine_necessity_to_stop_indication = EPCompilerProvider.getCompiler().compile(epl_Filter_t10_determine_necessity_to_stop_indication, arguments3);
            deployment_Filter_t10_determine_necessity_to_stop_indication = deploymentService.deploy(epCompiled_Filter_t10_determine_necessity_to_stop_indication);

            epCompiled_Filter_t19_determine_report_y_to_stop_indication = EPCompilerProvider.getCompiler().compile(epl_Filter_t19_determine_report_y_to_stop_indication, arguments3);
            deployment_Filter_t19_determine_report_y_to_stop_indication = deploymentService.deploy(epCompiled_Filter_t19_determine_report_y_to_stop_indication);

            epCompiled_Filter_t20_print_report_y_to_stop_indication = EPCompilerProvider.getCompiler().compile(epl_Filter_t20_print_report_y_to_stop_indication, arguments3);
            deployment_Filter_t20_print_report_y_to_stop_indication = deploymentService.deploy(epCompiled_Filter_t20_print_report_y_to_stop_indication);

            epCompiled_Filter_t18_adjust_report_y_to_stop_indicition = EPCompilerProvider.getCompiler().compile(epl_Filter_t18_adjust_report_y_to_stop_indicition, arguments3);
            deployment_Filter_t18_adjust_report_y_to_stop_indicition = deploymentService.deploy(epCompiled_Filter_t18_adjust_report_y_to_stop_indicition);

            epCompiled_Filter_t12_check_document_x_request_unlicensed = EPCompilerProvider.getCompiler().compile(epl_Filter_t12_check_document_x_request_unlicensed, arguments3);
            deployment_Filter_t12_check_document_x_request_unlicensed = deploymentService.deploy(epCompiled_Filter_t12_check_document_x_request_unlicensed);

            epCompiled_Filter_t17_check_report_y_to_stop_indication = EPCompilerProvider.getCompiler().compile(epl_Filter_t17_check_report_y_to_stop_indication, arguments3);
            deployment_Filter_t17_check_report_y_to_stop_indication = deploymentService.deploy(epCompiled_Filter_t17_check_report_y_to_stop_indication);

            epCompiled_Filter_t03_adjust_confirmation_of_receipt = EPCompilerProvider.getCompiler().compile(epl_Filter_t03_adjust_confirmation_of_receipt, arguments3);
            deployment_Filter_t03_adjust_confirmation_of_receipt = deploymentService.deploy(epCompiled_Filter_t03_adjust_confirmation_of_receipt);

            epCompiled_Filter_t07_1_draft_intern_advice_aspect_1 = EPCompilerProvider.getCompiler().compile(epl_Filter_t07_1_draft_intern_advice_aspect_1, arguments3);
            deployment_Filter_t07_1_draft_intern_advice_aspect_1 = deploymentService.deploy(epCompiled_Filter_t07_1_draft_intern_advice_aspect_1);

            epCompiled_Filter_t07_2_draft_intern_advice_aspect_2 = EPCompilerProvider.getCompiler().compile(epl_Filter_t07_2_draft_intern_advice_aspect_2, arguments3);
            deployment_Filter_t07_2_draft_intern_advice_aspect_2 = deploymentService.deploy(epCompiled_Filter_t07_2_draft_intern_advice_aspect_2);

            epCompiled_Filter_t07_3_draft_intern_advice_hold_for_aspect_3 = EPCompilerProvider.getCompiler().compile(epl_Filter_t07_3_draft_intern_advice_hold_for_aspect_3, arguments3);
            deployment_Filter_t07_3_draft_intern_advice_hold_for_aspect_3 = deploymentService.deploy(epCompiled_Filter_t07_3_draft_intern_advice_hold_for_aspect_3);

            epCompiled_Filter_t07_4_draft_internal_advice_to_hold_for_type_4 = EPCompilerProvider.getCompiler().compile(epl_Filter_t07_4_draft_internal_advice_to_hold_for_type_4, arguments3);
            deployment_Filter_t07_4_draft_internal_advice_to_hold_for_type_4 = deploymentService.deploy(epCompiled_Filter_t07_4_draft_internal_advice_to_hold_for_type_4);

            epCompiled_Filter_t07_5_draft_intern_advice_aspect_5 = EPCompilerProvider.getCompiler().compile(epl_Filter_t07_5_draft_intern_advice_aspect_5, arguments3);
            deployment_Filter_t07_5_draft_intern_advice_aspect_5 = deploymentService.deploy(epCompiled_Filter_t07_5_draft_intern_advice_aspect_5);

            epCompiled_Filter_t08_draft_and_send_request_for_advice = EPCompilerProvider.getCompiler().compile(epl_Filter_t08_draft_and_send_request_for_advice, arguments3);
            deployment_Filter_t08_draft_and_send_request_for_advice = deploymentService.deploy(epCompiled_Filter_t08_draft_and_send_request_for_advice);

            epCompiled_Filter_t09_1_process_or_receive_external_advice_from_party_1 = EPCompilerProvider.getCompiler().compile(epl_Filter_t09_1_process_or_receive_external_advice_from_party_1, arguments3);
            deployment_Filter_t09_1_process_or_receive_external_advice_from_party_1 = deploymentService.deploy(epCompiled_Filter_t09_1_process_or_receive_external_advice_from_party_1);

            epCompiled_Filter_t09_2_process_or_receive_external_advice_from_party_2 = EPCompilerProvider.getCompiler().compile(epl_Filter_t09_2_process_or_receive_external_advice_from_party_2, arguments3);
            deployment_Filter_t09_2_process_or_receive_external_advice_from_party_2 = deploymentService.deploy(epCompiled_Filter_t09_2_process_or_receive_external_advice_from_party_2);

            epCompiled_Filter_t09_3_process_or_receive_external_advice_from_party_3 = EPCompilerProvider.getCompiler().compile(epl_Filter_t09_3_process_or_receive_external_advice_from_party_3, arguments3);
            deployment_Filter_t09_3_process_or_receive_external_advice_from_party_3 = deploymentService.deploy(epCompiled_Filter_t09_3_process_or_receive_external_advice_from_party_3);

            epCompiled_Filter_t09_4_process_or_receive_external_advice_from_party_4 = EPCompilerProvider.getCompiler().compile(epl_Filter_t09_4_process_or_receive_external_advice_from_party_4, arguments3);
            deployment_Filter_t09_4_process_or_receive_external_advice_from_party_4 = deploymentService.deploy(epCompiled_Filter_t09_4_process_or_receive_external_advice_from_party_4);

            epCompiled_Filter_t11_create_document_x_request_unlicensed = EPCompilerProvider.getCompiler().compile(epl_Filter_t11_create_document_x_request_unlicensed, arguments3);
            deployment_Filter_t11_create_document_x_request_unlicensed = deploymentService.deploy(epCompiled_Filter_t11_create_document_x_request_unlicensed);

            epCompiled_Filter_t16_report_reasons_to_hold_request = EPCompilerProvider.getCompiler().compile(epl_Filter_t16_report_reasons_to_hold_request, arguments3);
            deployment_Filter_t16_report_reasons_to_hold_request = deploymentService.deploy(epCompiled_Filter_t16_report_reasons_to_hold_request);

            epCompiled_Filter_t02_check_confirmation_of_receipt = EPCompilerProvider.getCompiler().compile(epl_Filter_t02_check_confirmation_of_receipt, arguments3);
            deployment_Filter_t02_check_confirmation_of_receipt = deploymentService.deploy(epCompiled_Filter_t02_check_confirmation_of_receipt);

            epCompiled_Filter_t04_determine_confirmation_of_receipt = EPCompilerProvider.getCompiler().compile(epl_Filter_t04_determine_confirmation_of_receipt, arguments3);
            deployment_Filter_t04_determine_confirmation_of_receipt = deploymentService.deploy(epCompiled_Filter_t04_determine_confirmation_of_receipt);

            epCompiled_Filter_t05_print_and_send_confirmation_of_receipt = EPCompilerProvider.getCompiler().compile(epl_Filter_t05_print_and_send_confirmation_of_receipt, arguments3);
            deployment_Filter_t05_print_and_send_confirmation_of_receipt = deploymentService.deploy(epCompiled_Filter_t05_print_and_send_confirmation_of_receipt);

            epCompiled_Filter_t06_determine_necessity_of_stop_advice = EPCompilerProvider.getCompiler().compile(epl_Filter_t06_determine_necessity_of_stop_advice, arguments3);
            deployment_Filter_t06_determine_necessity_of_stop_advice = deploymentService.deploy(epCompiled_Filter_t06_determine_necessity_of_stop_advice);


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
        EPStatement statement_start_case_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_start_case_confirmation_of_receipt.getDeploymentId(), "start_case_confirmation_of_receipt");
        EPStatement statement_TE_t15_print_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_TE_t15_print_document_x_request_unlicensed.getDeploymentId(), "TE_t15_print_document_x_request_unlicensed");
        EPStatement statement_TE_t14_determine_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_TE_t14_determine_document_x_request_unlicensed.getDeploymentId(), "TE_t14_determine_document_x_request_unlicensed");
        EPStatement statement_TE_t10_determine_necessity_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_TE_t10_determine_necessity_to_stop_indication.getDeploymentId(), "TE_t10_determine_necessity_to_stop_indication");
        EPStatement statement_TE_t19_determine_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_TE_t19_determine_report_y_to_stop_indication.getDeploymentId(), "TE_t19_determine_report_y_to_stop_indication");
        EPStatement statement_TE_t20_print_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_TE_t20_print_report_y_to_stop_indication.getDeploymentId(), "TE_t20_print_report_y_to_stop_indication");
        EPStatement statement_TE_t18_adjust_report_y_to_stop_indicition = runtime.getDeploymentService().getStatement(deployment_TE_t18_adjust_report_y_to_stop_indicition.getDeploymentId(), "TE_t18_adjust_report_y_to_stop_indicition");
        EPStatement statement_TE_t12_check_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_TE_t12_check_document_x_request_unlicensed.getDeploymentId(), "TE_t12_check_document_x_request_unlicensed");
        EPStatement statement_TE_t17_check_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_TE_t17_check_report_y_to_stop_indication.getDeploymentId(), "TE_t17_check_report_y_to_stop_indication");
        EPStatement statement_TE_t03_adjust_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_TE_t03_adjust_confirmation_of_receipt.getDeploymentId(), "TE_t03_adjust_confirmation_of_receipt");
        EPStatement statement_TE_t07_1_draft_intern_advice_aspect_1 = runtime.getDeploymentService().getStatement(deployment_TE_t07_1_draft_intern_advice_aspect_1.getDeploymentId(), "TE_t07_1_draft_intern_advice_aspect_1");
        EPStatement statement_TE_t07_2_draft_intern_advice_aspect_2 = runtime.getDeploymentService().getStatement(deployment_TE_t07_2_draft_intern_advice_aspect_2.getDeploymentId(), "TE_t07_2_draft_intern_advice_aspect_2");
        EPStatement statement_TE_t07_3_draft_intern_advice_hold_for_aspect_3 = runtime.getDeploymentService().getStatement(deployment_TE_t07_3_draft_intern_advice_hold_for_aspect_3.getDeploymentId(), "TE_t07_3_draft_intern_advice_hold_for_aspect_3");
        EPStatement statement_TE_t07_4_draft_internal_advice_to_hold_for_type_4 = runtime.getDeploymentService().getStatement(deployment_TE_t07_4_draft_internal_advice_to_hold_for_type_4.getDeploymentId(), "TE_t07_4_draft_internal_advice_to_hold_for_type_4");
        EPStatement statement_TE_t07_5_draft_intern_advice_aspect_5 = runtime.getDeploymentService().getStatement(deployment_TE_t07_5_draft_intern_advice_aspect_5.getDeploymentId(), "TE_t07_5_draft_intern_advice_aspect_5");
        EPStatement statement_TE_t08_draft_and_send_request_for_advice = runtime.getDeploymentService().getStatement(deployment_TE_t08_draft_and_send_request_for_advice.getDeploymentId(), "TE_t08_draft_and_send_request_for_advice");
        EPStatement statement_TE_t09_1_process_or_receive_external_advice_from_party_1 = runtime.getDeploymentService().getStatement(deployment_TE_t09_1_process_or_receive_external_advice_from_party_1.getDeploymentId(), "TE_t09_1_process_or_receive_external_advice_from_party_1");
        EPStatement statement_TE_t09_2_process_or_receive_external_advice_from_party_2 = runtime.getDeploymentService().getStatement(deployment_TE_t09_2_process_or_receive_external_advice_from_party_2.getDeploymentId(), "TE_t09_2_process_or_receive_external_advice_from_party_2");
        EPStatement statement_TE_t09_3_process_or_receive_external_advice_from_party_3 = runtime.getDeploymentService().getStatement(deployment_TE_t09_3_process_or_receive_external_advice_from_party_3.getDeploymentId(), "TE_t09_3_process_or_receive_external_advice_from_party_3");
        EPStatement statement_TE_t09_4_process_or_receive_external_advice_from_party_4 = runtime.getDeploymentService().getStatement(deployment_TE_t09_4_process_or_receive_external_advice_from_party_4.getDeploymentId(), "TE_t09_4_process_or_receive_external_advice_from_party_4");
        EPStatement statement_TE_t11_create_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_TE_t11_create_document_x_request_unlicensed.getDeploymentId(), "TE_t11_create_document_x_request_unlicensed");
        EPStatement statement_TE_t16_report_reasons_to_hold_request = runtime.getDeploymentService().getStatement(deployment_TE_t16_report_reasons_to_hold_request.getDeploymentId(), "TE_t16_report_reasons_to_hold_request");
        EPStatement statement_TE_t02_check_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_TE_t02_check_confirmation_of_receipt.getDeploymentId(), "TE_t02_check_confirmation_of_receipt");
        EPStatement statement_TE_t04_determine_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_TE_t04_determine_confirmation_of_receipt.getDeploymentId(), "TE_t04_determine_confirmation_of_receipt");
        EPStatement statement_TE_t05_print_and_send_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_TE_t05_print_and_send_confirmation_of_receipt.getDeploymentId(), "TE_t05_print_and_send_confirmation_of_receipt");
        EPStatement statement_TE_t06_determine_necessity_of_stop_advice = runtime.getDeploymentService().getStatement(deployment_TE_t06_determine_necessity_of_stop_advice.getDeploymentId(), "TE_t06_determine_necessity_of_stop_advice");

        EPStatement statement_Correlate_t15_print_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_Correlate_t15_print_document_x_request_unlicensed.getDeploymentId(), "Correlate_t15_print_document_x_request_unlicensed");
        EPStatement statement_Correlate_t14_determine_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_Correlate_t14_determine_document_x_request_unlicensed.getDeploymentId(), "Correlate_t14_determine_document_x_request_unlicensed");
        EPStatement statement_Correlate_t10_determine_necessity_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_Correlate_t10_determine_necessity_to_stop_indication.getDeploymentId(), "Correlate_t10_determine_necessity_to_stop_indication");
        EPStatement statement_Correlate_t19_determine_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_Correlate_t19_determine_report_y_to_stop_indication.getDeploymentId(), "Correlate_t19_determine_report_y_to_stop_indication");
        EPStatement statement_Correlate_t20_print_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_Correlate_t20_print_report_y_to_stop_indication.getDeploymentId(), "Correlate_t20_print_report_y_to_stop_indication");
        EPStatement statement_Correlate_t18_adjust_report_y_to_stop_indicition = runtime.getDeploymentService().getStatement(deployment_Correlate_t18_adjust_report_y_to_stop_indicition.getDeploymentId(), "Correlate_t18_adjust_report_y_to_stop_indicition");
        EPStatement statement_Correlate_t12_check_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_Correlate_t12_check_document_x_request_unlicensed.getDeploymentId(), "Correlate_t12_check_document_x_request_unlicensed");
        EPStatement statement_Correlate_t17_check_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_Correlate_t17_check_report_y_to_stop_indication.getDeploymentId(), "Correlate_t17_check_report_y_to_stop_indication");
        EPStatement statement_Correlate_t03_adjust_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_Correlate_t03_adjust_confirmation_of_receipt.getDeploymentId(), "Correlate_t03_adjust_confirmation_of_receipt");
        EPStatement statement_Correlate_t07_1_draft_intern_advice_aspect_1 = runtime.getDeploymentService().getStatement(deployment_Correlate_t07_1_draft_intern_advice_aspect_1.getDeploymentId(), "Correlate_t07_1_draft_intern_advice_aspect_1");
        EPStatement statement_Correlate_t07_2_draft_intern_advice_aspect_2 = runtime.getDeploymentService().getStatement(deployment_Correlate_t07_2_draft_intern_advice_aspect_2.getDeploymentId(), "Correlate_t07_2_draft_intern_advice_aspect_2");
        EPStatement statement_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3 = runtime.getDeploymentService().getStatement(deployment_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3.getDeploymentId(), "Correlate_t07_3_draft_intern_advice_hold_for_aspect_3");
        EPStatement statement_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4 = runtime.getDeploymentService().getStatement(deployment_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4.getDeploymentId(), "Correlate_t07_4_draft_internal_advice_to_hold_for_type_4");
        EPStatement statement_Correlate_t07_5_draft_intern_advice_aspect_5 = runtime.getDeploymentService().getStatement(deployment_Correlate_t07_5_draft_intern_advice_aspect_5.getDeploymentId(), "Correlate_t07_5_draft_intern_advice_aspect_5");
        EPStatement statement_Correlate_t08_draft_and_send_request_for_advice = runtime.getDeploymentService().getStatement(deployment_Correlate_t08_draft_and_send_request_for_advice.getDeploymentId(), "Correlate_t08_draft_and_send_request_for_advice");
        EPStatement statement_Correlate_t09_1_process_or_receive_external_advice_from_party_1 = runtime.getDeploymentService().getStatement(deployment_Correlate_t09_1_process_or_receive_external_advice_from_party_1.getDeploymentId(), "Correlate_t09_1_process_or_receive_external_advice_from_party_1");
        EPStatement statement_Correlate_t09_2_process_or_receive_external_advice_from_party_2 = runtime.getDeploymentService().getStatement(deployment_Correlate_t09_2_process_or_receive_external_advice_from_party_2.getDeploymentId(), "Correlate_t09_2_process_or_receive_external_advice_from_party_2");
        EPStatement statement_Correlate_t09_3_process_or_receive_external_advice_from_party_3 = runtime.getDeploymentService().getStatement(deployment_Correlate_t09_3_process_or_receive_external_advice_from_party_3.getDeploymentId(), "Correlate_t09_3_process_or_receive_external_advice_from_party_3");
        EPStatement statement_Correlate_t09_4_process_or_receive_external_advice_from_party_4 = runtime.getDeploymentService().getStatement(deployment_Correlate_t09_4_process_or_receive_external_advice_from_party_4.getDeploymentId(), "Correlate_t09_4_process_or_receive_external_advice_from_party_4");
        EPStatement statement_Correlate_t11_create_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_Correlate_t11_create_document_x_request_unlicensed.getDeploymentId(), "Correlate_t11_create_document_x_request_unlicensed");
        EPStatement statement_Correlate_t16_report_reasons_to_hold_request = runtime.getDeploymentService().getStatement(deployment_Correlate_t16_report_reasons_to_hold_request.getDeploymentId(), "Correlate_t16_report_reasons_to_hold_request");
        EPStatement statement_Correlate_t02_check_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_Correlate_t02_check_confirmation_of_receipt.getDeploymentId(), "Correlate_t02_check_confirmation_of_receipt");
        EPStatement statement_Correlate_t04_determine_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_Correlate_t04_determine_confirmation_of_receipt.getDeploymentId(), "Correlate_t04_determine_confirmation_of_receipt");
        EPStatement statement_Correlate_t05_print_and_send_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_Correlate_t05_print_and_send_confirmation_of_receipt.getDeploymentId(), "Correlate_t05_print_and_send_confirmation_of_receipt");
        EPStatement statement_Correlate_t06_determine_necessity_of_stop_advice = runtime.getDeploymentService().getStatement(deployment_Correlate_t06_determine_necessity_of_stop_advice.getDeploymentId(), "Correlate_t06_determine_necessity_of_stop_advice");

        EPStatement statement_Filter_t15_print_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_Filter_t15_print_document_x_request_unlicensed.getDeploymentId(), "Filter_t15_print_document_x_request_unlicensed");
        EPStatement statement_Filter_t14_determine_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_Filter_t14_determine_document_x_request_unlicensed.getDeploymentId(), "Filter_t14_determine_document_x_request_unlicensed");
        EPStatement statement_Filter_t10_determine_necessity_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_Filter_t10_determine_necessity_to_stop_indication.getDeploymentId(), "Filter_t10_determine_necessity_to_stop_indication");
        EPStatement statement_Filter_t19_determine_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_Filter_t19_determine_report_y_to_stop_indication.getDeploymentId(), "Filter_t19_determine_report_y_to_stop_indication");
        EPStatement statement_Filter_t20_print_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_Filter_t20_print_report_y_to_stop_indication.getDeploymentId(), "Filter_t20_print_report_y_to_stop_indication");
        EPStatement statement_Filter_t18_adjust_report_y_to_stop_indicition = runtime.getDeploymentService().getStatement(deployment_Filter_t18_adjust_report_y_to_stop_indicition.getDeploymentId(), "Filter_t18_adjust_report_y_to_stop_indicition");
        EPStatement statement_Filter_t12_check_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_Filter_t12_check_document_x_request_unlicensed.getDeploymentId(), "Filter_t12_check_document_x_request_unlicensed");
        EPStatement statement_Filter_t17_check_report_y_to_stop_indication = runtime.getDeploymentService().getStatement(deployment_Filter_t17_check_report_y_to_stop_indication.getDeploymentId(), "Filter_t17_check_report_y_to_stop_indication");
        EPStatement statement_Filter_t03_adjust_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_Filter_t03_adjust_confirmation_of_receipt.getDeploymentId(), "Filter_t03_adjust_confirmation_of_receipt");
        EPStatement statement_Filter_t07_1_draft_intern_advice_aspect_1 = runtime.getDeploymentService().getStatement(deployment_Filter_t07_1_draft_intern_advice_aspect_1.getDeploymentId(), "Filter_t07_1_draft_intern_advice_aspect_1");
        EPStatement statement_Filter_t07_2_draft_intern_advice_aspect_2 = runtime.getDeploymentService().getStatement(deployment_Filter_t07_2_draft_intern_advice_aspect_2.getDeploymentId(), "Filter_t07_2_draft_intern_advice_aspect_2");
        EPStatement statement_Filter_t07_3_draft_intern_advice_hold_for_aspect_3 = runtime.getDeploymentService().getStatement(deployment_Filter_t07_3_draft_intern_advice_hold_for_aspect_3.getDeploymentId(), "Filter_t07_3_draft_intern_advice_hold_for_aspect_3");
        EPStatement statement_Filter_t07_4_draft_internal_advice_to_hold_for_type_4 = runtime.getDeploymentService().getStatement(deployment_Filter_t07_4_draft_internal_advice_to_hold_for_type_4.getDeploymentId(), "Filter_t07_4_draft_internal_advice_to_hold_for_type_4");
        EPStatement statement_Filter_t07_5_draft_intern_advice_aspect_5 = runtime.getDeploymentService().getStatement(deployment_Filter_t07_5_draft_intern_advice_aspect_5.getDeploymentId(), "Filter_t07_5_draft_intern_advice_aspect_5");
        EPStatement statement_Filter_t08_draft_and_send_request_for_advice = runtime.getDeploymentService().getStatement(deployment_Filter_t08_draft_and_send_request_for_advice.getDeploymentId(), "Filter_t08_draft_and_send_request_for_advice");
        EPStatement statement_Filter_t09_1_process_or_receive_external_advice_from_party_1 = runtime.getDeploymentService().getStatement(deployment_Filter_t09_1_process_or_receive_external_advice_from_party_1.getDeploymentId(), "Filter_t09_1_process_or_receive_external_advice_from_party_1");
        EPStatement statement_Filter_t09_2_process_or_receive_external_advice_from_party_2 = runtime.getDeploymentService().getStatement(deployment_Filter_t09_2_process_or_receive_external_advice_from_party_2.getDeploymentId(), "Filter_t09_2_process_or_receive_external_advice_from_party_2");
        EPStatement statement_Filter_t09_3_process_or_receive_external_advice_from_party_3 = runtime.getDeploymentService().getStatement(deployment_Filter_t09_3_process_or_receive_external_advice_from_party_3.getDeploymentId(), "Filter_t09_3_process_or_receive_external_advice_from_party_3");
        EPStatement statement_Filter_t09_4_process_or_receive_external_advice_from_party_4 = runtime.getDeploymentService().getStatement(deployment_Filter_t09_4_process_or_receive_external_advice_from_party_4.getDeploymentId(), "Filter_t09_4_process_or_receive_external_advice_from_party_4");
        EPStatement statement_Filter_t11_create_document_x_request_unlicensed = runtime.getDeploymentService().getStatement(deployment_Filter_t11_create_document_x_request_unlicensed.getDeploymentId(), "Filter_t11_create_document_x_request_unlicensed");
        EPStatement statement_Filter_t16_report_reasons_to_hold_request = runtime.getDeploymentService().getStatement(deployment_Filter_t16_report_reasons_to_hold_request.getDeploymentId(), "Filter_t16_report_reasons_to_hold_request");
        EPStatement statement_Filter_t02_check_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_Filter_t02_check_confirmation_of_receipt.getDeploymentId(), "Filter_t02_check_confirmation_of_receipt");
        EPStatement statement_Filter_t04_determine_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_Filter_t04_determine_confirmation_of_receipt.getDeploymentId(), "Filter_t04_determine_confirmation_of_receipt");
        EPStatement statement_Filter_t05_print_and_send_confirmation_of_receipt = runtime.getDeploymentService().getStatement(deployment_Filter_t05_print_and_send_confirmation_of_receipt.getDeploymentId(), "Filter_t05_print_and_send_confirmation_of_receipt");
        EPStatement statement_Filter_t06_determine_necessity_of_stop_advice = runtime.getDeploymentService().getStatement(deployment_Filter_t06_determine_necessity_of_stop_advice.getDeploymentId(), "Filter_t06_determine_necessity_of_stop_advice");

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
        statement_TE_t15_print_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t15_print_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t14_determine_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t14_determine_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t10_determine_necessity_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t10_determine_necessity_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t19_determine_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t19_determine_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t20_print_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t20_print_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t18_adjust_report_y_to_stop_indicition.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t18_adjust_report_y_to_stop_indicition-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t12_check_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t12_check_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t17_check_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t17_check_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t03_adjust_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t03_adjust_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t07_1_draft_intern_advice_aspect_1.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t07_1_draft_intern_advice_aspect_1-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t07_2_draft_intern_advice_aspect_2.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t07_2_draft_intern_advice_aspect_2-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t07_3_draft_intern_advice_hold_for_aspect_3.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t07_3_draft_intern_advice_hold_for_aspect_3-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t07_4_draft_internal_advice_to_hold_for_type_4.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t07_4_draft_internal_advice_to_hold_for_type_4-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t07_5_draft_intern_advice_aspect_5.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t07_5_draft_intern_advice_aspect_5-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t08_draft_and_send_request_for_advice.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t08_draft_and_send_request_for_advice-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t09_1_process_or_receive_external_advice_from_party_1.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t09_1_process_or_receive_external_advice_from_party_1-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t09_2_process_or_receive_external_advice_from_party_2.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t09_2_process_or_receive_external_advice_from_party_2-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t09_3_process_or_receive_external_advice_from_party_3.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t09_3_process_or_receive_external_advice_from_party_3-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t09_4_process_or_receive_external_advice_from_party_4.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t09_4_process_or_receive_external_advice_from_party_4-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t11_create_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t11_create_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t16_report_reasons_to_hold_request.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t16_report_reasons_to_hold_request-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t02_check_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t02_check_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t04_determine_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t04_determine_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t05_print_and_send_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t05_print_and_send_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_TE_t06_determine_necessity_of_stop_advice.addListener((newData, oldData, s, r) -> {System.out.println("Event-TE_t06_determine_necessity_of_stop_advice-instance "+newData[0].getUnderlying()); }); 

        statement_Correlate_t15_print_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t15_print_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t14_determine_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t14_determine_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t10_determine_necessity_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t10_determine_necessity_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t19_determine_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t19_determine_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t20_print_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t20_print_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t18_adjust_report_y_to_stop_indicition.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t18_adjust_report_y_to_stop_indicition-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t12_check_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t12_check_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t17_check_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t17_check_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t03_adjust_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t03_adjust_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t07_1_draft_intern_advice_aspect_1.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t07_1_draft_intern_advice_aspect_1-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t07_2_draft_intern_advice_aspect_2.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t07_2_draft_intern_advice_aspect_2-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t07_3_draft_intern_advice_hold_for_aspect_3.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t07_3_draft_intern_advice_hold_for_aspect_3-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t07_4_draft_internal_advice_to_hold_for_type_4.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t07_4_draft_internal_advice_to_hold_for_type_4-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t07_5_draft_intern_advice_aspect_5.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t07_5_draft_intern_advice_aspect_5-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t08_draft_and_send_request_for_advice.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t08_draft_and_send_request_for_advice-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t09_1_process_or_receive_external_advice_from_party_1.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t09_1_process_or_receive_external_advice_from_party_1-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t09_2_process_or_receive_external_advice_from_party_2.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t09_2_process_or_receive_external_advice_from_party_2-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t09_3_process_or_receive_external_advice_from_party_3.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t09_3_process_or_receive_external_advice_from_party_3-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t09_4_process_or_receive_external_advice_from_party_4.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t09_4_process_or_receive_external_advice_from_party_4-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t11_create_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t11_create_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t16_report_reasons_to_hold_request.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t16_report_reasons_to_hold_request-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t02_check_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t02_check_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t04_determine_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t04_determine_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t05_print_and_send_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t05_print_and_send_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_Correlate_t06_determine_necessity_of_stop_advice.addListener((newData, oldData, s, r) -> {System.out.println("Event-Correlate_t06_determine_necessity_of_stop_advice-instance "+newData[0].getUnderlying()); }); 

        statement_Filter_t15_print_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t15_print_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t14_determine_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t14_determine_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t10_determine_necessity_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t10_determine_necessity_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t19_determine_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t19_determine_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t20_print_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t20_print_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t18_adjust_report_y_to_stop_indicition.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t18_adjust_report_y_to_stop_indicition-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t12_check_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t12_check_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t17_check_report_y_to_stop_indication.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t17_check_report_y_to_stop_indication-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t03_adjust_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t03_adjust_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t07_1_draft_intern_advice_aspect_1.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t07_1_draft_intern_advice_aspect_1-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t07_2_draft_intern_advice_aspect_2.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t07_2_draft_intern_advice_aspect_2-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t07_3_draft_intern_advice_hold_for_aspect_3.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t07_3_draft_intern_advice_hold_for_aspect_3-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t07_4_draft_internal_advice_to_hold_for_type_4.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t07_4_draft_internal_advice_to_hold_for_type_4-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t07_5_draft_intern_advice_aspect_5.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t07_5_draft_intern_advice_aspect_5-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t08_draft_and_send_request_for_advice.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t08_draft_and_send_request_for_advice-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t09_1_process_or_receive_external_advice_from_party_1.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t09_1_process_or_receive_external_advice_from_party_1-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t09_2_process_or_receive_external_advice_from_party_2.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t09_2_process_or_receive_external_advice_from_party_2-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t09_3_process_or_receive_external_advice_from_party_3.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t09_3_process_or_receive_external_advice_from_party_3-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t09_4_process_or_receive_external_advice_from_party_4.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t09_4_process_or_receive_external_advice_from_party_4-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t11_create_document_x_request_unlicensed.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t11_create_document_x_request_unlicensed-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t16_report_reasons_to_hold_request.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t16_report_reasons_to_hold_request-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t02_check_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t02_check_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t04_determine_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t04_determine_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t05_print_and_send_confirmation_of_receipt.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t05_print_and_send_confirmation_of_receipt-instance "+newData[0].getUnderlying()); }); 
        statement_Filter_t06_determine_necessity_of_stop_advice.addListener((newData, oldData, s, r) -> {System.out.println("Event-Filter_t06_determine_necessity_of_stop_advice-instance "+newData[0].getUnderlying()); }); 

*/

        String filePath = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\CoSeLoG_unlabeled_eventID.csv";
        new UnlabeledEventSource_eventID(filePath, 8577, eventService, "UnlabeledEvent").run();//take 35 records from UnlabeledEvent

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