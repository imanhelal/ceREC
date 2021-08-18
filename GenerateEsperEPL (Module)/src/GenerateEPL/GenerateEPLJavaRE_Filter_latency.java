package GenerateEPL;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GenerateEPLJavaRE_Filter_latency {
	private static List<List<String>> eplNames = new ArrayList<List<String>>();//String[]
	private static String javaClass= "BPI2013Closed";//"Wellness";//"CoSeLoG"; //"BPI2013Closed";//"BPI2017";//"ISJeg_v3";// "Sepsis";//"loop1000";//"Paper2"; //"Paper"
	private static double threshold = 0.3;
	private static int limits = 5;
	private static String heur_Meter = "sec";//"min";//
	private static int heur_Meter_Number = 1000;//60;//1000*60;
	private static int heur_milli_number = 1;
	private static int logLength = 6661;//8540;//561672;
	
	
	public static void main(String[] args) throws IOException  {
		String inputFilePath = "..\\GenerateEsperEPL\\src\\GenerateEPL\\"+javaClass+"_inputRules_1R.txt"; //inputRules.txt";//
		String outputFilePath = "..\\GenerateEsperEPL\\src\\GenerateEPL\\"+javaClass+"_outputRules_1R.txt";//outputRules2.txt";//
		String heurFilePath = "..\\GenerateEsperEPL\\src\\GenerateEPL\\"+javaClass+"_heuristics.csv";//BPIC2017-clean-mod-ordered-noLC-noR-Heuristic-v2.csv";//BPIC2017-clean-mod-Heuristic-v1-correct.csv"; //javaClass+"_heuristics.csv";//heuristics.csv";//
		
//		String runnerFilePath = "C:\\IH\\eclipse-workspace\\CEPForEventCorrelation\\src\\main\\java\\eg\\cu\\fci\\is\\correlator\\Runner_"+javaClass+".java";
		String runnerFilePath = "C:\\IH\\Docs\\eclipse-ws2019\\CEPForEventCorrelation_latency\\src\\main\\java\\eg\\cu\\fci\\is\\correlator\\Runner_"+javaClass+"_1R_v2.java";
		String[] startEvents = {"accepted","unmatched"};//{"a_create application"};//{"confirmation of receipt"};//{"accepted"};//case sensitive  //can take more than one start events {"A","G"};
		prepareEPL(inputFilePath,outputFilePath,startEvents,heurFilePath);
		
		//generate Java class given start events
		FileWriter frJava = new FileWriter(runnerFilePath);
        generateJava(frJava,startEvents);
        frJava.close();
	}
	
	private static void prepareEPL(String inputFilePath,String outputFilePath,String[] startEvents, String heurFilePath) throws IOException{
		
		//long countActivities = Files.lines(Paths.get(heurFilePath), Charset.defaultCharset()).count()-1; //to pass the header line
		//System.out.println("Count of activities " +countActivities);
		
		BufferedReader reader;
		BufferedReader heurRead;
		int count = 0;
		
		heurRead = new BufferedReader(new FileReader(heurFilePath));
		heurRead.readLine(); //to skip header
		String heurLine = heurRead.readLine();
		
		Hashtable<String, int[]> heuristics = new Hashtable<String, int[]>();
		
		while(heurLine != null) {
			String[] dataHeur = heurLine.split(",");
			int [] minMax = new int[2];
			minMax[0] = Integer.parseInt(dataHeur[1].trim());//min
			minMax[1] = Integer.parseInt(dataHeur[2].trim());//max
			heuristics.put(dataHeur[0], minMax);
			heurLine = heurRead.readLine();
		}
		
		for (Entry<String, int[]> e : heuristics.entrySet()) 
			System.out.println(e.getKey() + " " + Arrays.toString(e.getValue())); 
		
		heurRead.close();
		
        reader = new BufferedReader(new FileReader(inputFilePath));
        String line = reader.readLine();
        
        FileWriter fr = new FileWriter(outputFilePath);
        String pred = ""; //predecessor
        String succ = ""; //successor
        /*
         * Sample input sentences
         * A -> B					sequence
         * (B # N) -> C				XOR
         * (C & D) -> E			AND
         * (D # (X & Y) ) -> Z		Mix of XOR,AND [Not yet finished]
         */
        
        //Write a set of predefined epl statements based on the given set of start events.
        predefinedEPL(fr,startEvents);
        
        //Read and Write the given set of rules.
        while (line != null ) {
        	count++;
        	String[] data = line.split("->");
        	if(line.length()!=0)
        	{
	            try {
	            	pred = data[0].trim();
	            	succ = data[1].trim();
	            	
	            	System.out.print("Rule = "+count+" ");
	            	System.out.println("predessor = "+pred+" , successor = "+succ);
	            	
//	            	String detailsPred = processPred3(pred);
	            	String patternExpression = processPred4(pred,succ);
	            	boolean andEPL = false;
	            	if(pred.contains("&")) 
	            			andEPL=true;
	            	boolean xorEPL = false;
	            	if(pred.contains("#")) 
	            			xorEPL=true;
	            	//rules2EPL(detailsPred,succ,heuristics, fr, andEPL);
	            	rules2EPL_v2(patternExpression,pred,succ,heuristics, fr, andEPL, xorEPL);
	            }catch(NullPointerException npe){
	            	npe.printStackTrace();
	            }
        	}
            line = reader.readLine();
        }
        //close both reader and writer
        reader.close();
        fr.close();
        

	}

	private static String processXOR(String xorEx, String succ) {
		// String regex = "\\([ ]*([a-zA-Z\\d]*)[ ]*[#][ ]*([a-zA-Z\\d]*)[ ]*\\)";
		//String regex = "([(]?(([a-zA-Z&\\d]*)*)[#]?(R)?)[)]?";
		String regex = "([(]?(([a-zA-Z&\\-\\d\\s\\_\\(\\)]*)*)[#]?(R)?)[)]?";
		Pattern pt = Pattern.compile(regex);
		Matcher matcher = pt.matcher(xorEx);
		StringBuilder result = new StringBuilder();

		boolean start = false;
		boolean xor = false;
		String anding = "";
		List<String> XORs = new ArrayList<String>();
		while (matcher.find()) {
//			System.out.println("matcherXOR "+matcher.group(2));
			if (matcher.group(2).trim().length() > 0 && matcher.group(2).trim().contains("&") ) {
				if(anding.trim().length()>0)
				{
					anding +=" or ";
				}
				anding += processAND(matcher.group(2).trim(),succ);
			}
			else {
				if (!start && matcher.group(2).trim().length() > 0) {
					result.append("(every pred=FilterLabeledEvent(");
					xor = true;
				}
				else if (start && matcher.group(2).trim().length() > 0) {
					result.append(" or ");
				}
				if (matcher.group(2).trim().length() > 0) {
					XORs.add(matcher.group(2).trim());
					result.append("activity=");
					result.append("'");
					result.append(matcher.group(2).trim());
					result.append("'");
					start = true;
				}
			}

		}
		
		if(anding.trim().length()>0) {
			if(xor)
			{
				result.append(") -> every succ=UnlabeledEvent(activity='" + succ + "'))  or " );
			}
			result.append(anding);
		}
		else {
			result.append(") -> every succ=UnlabeledEvent(activity='" + succ + "'))" );	
		}
		
//		System.out.println("XORs ==> " + XORs.toString());
		return result.toString();

	}

	private static String processAND(String andEx, String succ) {
		// String regex = "\\([ ]*([a-zA-Z\\d]*)[ ]*[&][ ]*([a-zA-Z\\d]*)[ ]*\\)";
		String regex = "([(]?(([a-zA-Z\\d-\\s\\_\\(\\)]*)*)[&]?(R)?)[)]?";
		Pattern pt = Pattern.compile(regex);
		Matcher matcher = pt.matcher(andEx);
		StringBuilder result = new StringBuilder();
		result.append("((every pred=FilterLabeledEvent(");
		boolean start = false;
		int countANDs = 0;
		List<String> ANDs = new ArrayList<String>();
		while (matcher.find()) {
			if (start && matcher.group(2).length() > 0 && countANDs > 0)
				result.append(" and every inter" + countANDs + " = FilterLabeledEvent(");
			if (matcher.group(2).length() > 0) {
				ANDs.add(matcher.group(2));
				result.append("activity=");
				result.append("'");
				result.append(matcher.group(2).trim());
				result.append("')");
				start = true;
				countANDs++;
			}

		}
		result.append(")");
		result.append(" -> every succ=UnlabeledEvent(activity='" + succ + "'))" );
//		System.out.println("ANDs ==> " + ANDs.toString());
		return result.toString();

	}

	private static String processPred4(String pred, String succ) {
		// To generate the details of the predessor part of the rule into an EPL
		// statement
		StringBuilder detailedPred = new StringBuilder();

		// String regex = "([(]?(([a-zA-Z\\d]*)*)[#]?(R)?)[)]?";

		// https://regex101.com/r/xxuW0O/115
		// "(\\(\\(([a-zA-Z\\d]*)[&]?([a-zA-Z\\d]*)\\)[#]?([a-zA-Z\\d]*)\\))+";
		// matches ((ABC&R)#y)((XY1&y2)#z2))

		// (\(\(([a-zA-Z\d]*)[&]?([a-zA-Z\d]*)\)[#]?([a-zA-Z\d]*)\))+|(\(([a-zA-Z\d]*)[#]?\(([a-zA-Z\d]*)[&]?([a-zA-Z\d]*)\)\))+
		// ((XY1&y2)#z2)(a1#(b1&b2))

		if (pred.contains("&") && !pred.contains("#")) { //Only AND combination
			detailedPred.append(processAND(pred,succ));
		}
		else if (pred.contains("#") ) {//contains XOR combination (possible AND)
			detailedPred.append(processXOR(pred,succ));
		} 
		else if (detailedPred.length() == 0) {
			detailedPred.append("(every pred=FilterLabeledEvent(activity = '" + pred + "')");
			detailedPred.append(" -> every succ=UnlabeledEvent(activity='" + succ + "'))" );

		}

		return detailedPred.toString();
	}

	
//------------------

	// To generate the base EPL statements to run on Esper engine
	private static void predefinedEPL(FileWriter fr, String[] startEvents) {
		
		
		
		List<String> statement = new ArrayList<String>();
		//Only a test statement for the input -- not used elsewhere.
        String epl_Unlabeled = 
        "@Name('Unlabeled') select * from UnlabeledEvent; ";//caseID, activity, timestamp, probability
        statement.add("Unlabeled");
        statement.add(epl_Unlabeled);
        eplNames.add(statement);
        
        
        statement = new ArrayList<String>();
        String epl_FilterLabeledEvent =" @Name('FilterLabeledEvent') select * from FilterLabeledEvent; ";
        statement.add("FilterLabeledEvent");
        statement.add(epl_FilterLabeledEvent);
        eplNames.add(statement);
        
        statement = new ArrayList<String>();
        String epl_Labeled = "@Name('Labeled') select * from LabeledEvent; ";
        statement.add("Labeled");
        statement.add(epl_Labeled);
        eplNames.add(statement);
        
        statement = new ArrayList<String>();
        //an intermediate stream to check the possible labels for incoming unlabeled events. These should be temporary and need to be deleted after usage. 
        String epl_Temp = "@Name('Temp') select * from TempEvent ; ";
        statement.add("Temp");
        statement.add(epl_Temp);
        eplNames.add(statement);
        
        statement = new ArrayList<String>();
        //an intermediate stream to check the possible labels for incoming unlabeled events. These should be temporary and need to be deleted after usage. 
        String epl_FailCorrelation = "@Name('FailCorrelation') select * from FailCorrelation; ";
        statement.add("FailCorrelation");
        statement.add(epl_FailCorrelation);
        eplNames.add(statement);
        
        String listStartEvents = String.join("','", startEvents);
        
        statement = new ArrayList<String>();
        //This query is responsible for retrieving the last caseID in the list of cases. This number+1 is used to initialize the new case with the incoming event 'A'
        String  epl_get_case_ID = "@Name('get_case_ID') "+
	        		" insert into Cases  select (select coalesce(max(caseID)+1, 1) "+
	        		" from  Cases )  as caseID from UnlabeledEvent "+
	        		" where  activity in ('"+listStartEvents+"');  ";
	        		//"@Audit select max(caseID) from Cases; \n";
        statement.add("get_case_ID");
        statement.add(epl_get_case_ID);
        eplNames.add(statement);
        
        String[] epl_start_case_ = new String[startEvents.length];
        
        for(int i=0; i<startEvents.length;i++)
        {
        	statement = new ArrayList<String>();
            //Label the incoming unlabeled event 'A' in a new case with probability 1.0. This is assigned by default. Since this is the entry event of the process.
	        epl_start_case_[i] = "@Name('start_case_"+startEvents[i].replaceAll("\\s+", "_").replaceAll("-+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_").toString()+"') "+
	        		" insert into FilterLabeledEvent (caseID, activity, timestamp, probability, initTime) "+
	        		" select (select coalesce(max(caseID)+1,1) from Cases) as caseID, UE.activity, UE.timestamp, 1.0, UE.sysTime as initTime "+
	        		" from UnlabeledEvent as UE where UE.activity = '"+startEvents[i].toString()+"'; ";
	        statement.add("start_case_"+startEvents[i].replaceAll("\\s+", "_").replaceAll("-+", "_").toString());
	        statement.add(epl_start_case_[i]);
	        eplNames.add(statement);
	    }

		try {
			
			fr.append("// create a table to hold the state of the current caseID\r\n" + 
					"create table Cases (caseID long primary key);\r\n" + 
					"\r\n" + 
					"//an incoming stream of unlabeled events (activity name and its timestamp) \r\n" + 
					"create schema UnlabeledEvent(caseID long, activity string, timestamp long, probability double);\r\n" + 
					"@Name('Unlabeled') select * from UnlabeledEvent;\r\n" + 
					"\r\n" + 
					"//an outgoing stream of final labeled events (caseID, activity name, its timestamp, calculated probability )\r\n" + 
					"create schema LabeledEvent(caseID long, activity string, timestamp long, probability double);\r\n" + 
					"@Name('Labeled') select * from LabeledEvent;\r\n" + 
					"\r\n" + 
					"//an intermediate stream to check the possible labels for incoming unlabeled events. These should be temporary and need to be deleted after usage. \r\n" + 
					"create schema TempEvent (caseID long, activity string, timestamp long, probability double);\r\n" + 
					"@Name('TempEvent') select * from TempEvent ;\r\n" + 
					"");
			fr.append(epl_Unlabeled+"\n");
			fr.append(epl_Labeled+"\n");
			fr.append(epl_FilterLabeledEvent+"\n");
			fr.append(epl_Temp+"\n");
			fr.append(epl_get_case_ID+"\n");
			for(int i=0; i<startEvents.length;i++)
	        {
				fr.append(epl_start_case_[i]+"\n");
	        }
			fr.append("\n");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	
	}

	// To generate the rest of EPL statements for input rules to run on Esper engine
	private static void rules2EPL_v2(String patternExpression, String pred, String succ,Hashtable<String, int[]> heuristics , FileWriter fr, boolean andEPL, boolean xorEPL) {
		List<String> statement = new ArrayList<String>();
		String epl_TE = "";
		String ruleName = "";
		if(!andEPL) {
			epl_TE = "@Priority(5) "+
					"@Name('TE_"+succ.replaceAll("\\s+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_").replaceAll("-+", "_")+"') "+
					"insert into TempEvent (caseID, activity, timestamp) "+
	        		"select distinct pred.caseID, '"+succ+"' as activity, succ.timestamp as timestamp"+
	        		" from pattern ["+patternExpression+" where timer:within("+(1+heuristics.get(succ)[1]/heur_Meter_Number)+" "+heur_Meter+")]#time("+(1+heuristics.get(succ)[1]/heur_Meter_Number)+" "+heur_Meter+") "+//#time(60 sec)
	        		" where (succ.timestamp - pred.timestamp) >= "+heuristics.get(succ)[0]*heur_milli_number+" and (succ.timestamp - pred.timestamp) <="+heuristics.get(succ)[1]*heur_milli_number+";";
	        		//" order by caseID ;"; 
	        		//" select * from TempEvent; \n" ;
			statement.add("TE_"+succ.replaceAll("\\s+", "_").replaceAll("-+", "_").replaceAll("-+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_"));
			statement.add(epl_TE);
			eplNames.add(statement);
	        
		}
		else {//and rule has extra condition
			epl_TE = "@Priority(5) "+
					"@Name('TE_"+succ.replaceAll("\\s+", "_").replaceAll("-+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_")+"') "+
					"insert into TempEvent (caseID, activity, timestamp) "+
	        		"select distinct pred.caseID, '"+succ+"' as activity, succ.timestamp as timestamp"+
	        		" from pattern ["+patternExpression+" where timer:within("+(1+heuristics.get(succ)[1]/heur_Meter_Number)+" "+heur_Meter+") ]#time("+(1+heuristics.get(succ)[1]/heur_Meter_Number)+" "+heur_Meter+") ";
	        		//" where pred.caseID = inter.caseID and (succ.timestamp - max(pred.timestamp,inter.timestamp)) >= "+heuristics.get(succ)[0]+" and (succ.timestamp - max(pred.timestamp,inter.timestamp)) <="+heuristics.get(succ)[1]*heur_Meter_Number+";";
	        		//" order by caseID ; "; 
	        		//" select * from TempEvent; \n" ;
			String whereClause = "";
			int countANDs = countANDS(pred);
			
//			if(countANDs == 2 && !xorEPL)//only AND expression
//			{	
//				whereClause = " where pred.caseID = inter1.caseID and (succ.timestamp - max(pred.timestamp,inter1.timestamp)) >= "+heuristics.get(succ)[0]+" and (succ.timestamp - max(pred.timestamp,inter1.timestamp)) <="+heuristics.get(succ)[1]*heur_Meter_Number;
//			}
//			else 
			if(countANDs >= 2)//handles AND expression
			{	
				String inter = "";
				String condInter = "";
				for(int i=2; i<=countANDs ;i++)
				{
					inter += ",inter"+(i-1)+".timestamp";
					condInter += " pred.caseID = inter"+(i-1)+ ".caseID and";
				}
				whereClause += " where ("+condInter+" (succ.timestamp - max(pred.timestamp"+inter+")) >= "+heuristics.get(succ)[0]*heur_milli_number+" and (succ.timestamp - max(pred.timestamp"+inter+")) <="+heuristics.get(succ)[1]*heur_milli_number+" )";
				//whereClause += " where ( pred.caseID = inter"+(countANDs-1)+".caseID and (succ.timestamp - max(pred.timestamp"+inter+")) >= "+heuristics.get(succ)[0]*heur_milli_number+" and (succ.timestamp - max(pred.timestamp"+inter+")) <="+heuristics.get(succ)[1]*heur_milli_number+" )";
			}
			if(xorEPL) {//handles XOR expression
				whereClause += " or ( (succ.timestamp - pred.timestamp) >= "+heuristics.get(succ)[0]*heur_milli_number+" and (succ.timestamp - pred.timestamp) <="+heuristics.get(succ)[1]*heur_milli_number+" ) ";
			}
			
			epl_TE +=whereClause+" ;";
			statement.add("TE_"+succ.replaceAll("\\s+", "_").replaceAll("-+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_"));
			statement.add(epl_TE);
			eplNames.add(statement);
	        
		}
		statement = new ArrayList<String>();
        String epl_Correlate = "@Priority(20) "+
        		"@Name('Correlate_"+succ.replaceAll("\\s+", "_").replaceAll("-+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_")+"') "+
        		"insert into LabeledEvent (caseID, activity , timestamp, probability) "+
        		"select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , min(((select count(caseID)  " +
        		"from TempEvent.win:time_batch(1 sec) where caseID = T.caseID and activity = T.activity and timestamp=T.timestamp) /count(all caseID, group_by:timestamp)),1.0) as probability "+
        		"from TempEvent.win:time_batch(1 sec) as T  "+
        		"where activity = '"+succ+"'  "+
        		"group by caseID "+
        		"order by timestamp desc "+
        		"limit "+limits+" ; ";
        		//"select * from LabeledEvent; " ;
		statement.add("Correlate_"+succ.replaceAll("\\s+", "_").replaceAll("-+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_"));
		statement.add(epl_Correlate);
        eplNames.add(statement);
        
        statement = new ArrayList<String>();
        String epl_Filter = "@Priority(50) "+
        		"@Name('Filter_"+succ.replaceAll("\\s+", "_").replaceAll("-+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_")+"') "+
        		"insert into FilterLabeledEvent (caseID, activity , timestamp, probability, initTime) "+
        		"select distinct caseID as caseID, last(activity) as activity, max(timestamp) as timestamp , probability,  \"+initTime+\" "+
        		"from LabeledEvent.win:time(1 sec) as T "+
        		"where activity = '"+succ+"'  "+
        		"and probability >= "+threshold+" "+
        		"group by caseID "+
        		"order by probability desc "+//timestamp
        		"limit "+limits+" ;";
        		//"select * from LabeledEvent; " ;
		statement.add("Filter_"+succ.replaceAll("\\s+", "_").replaceAll("-+", "_").replaceAll("\\(", "_").replaceAll("\\)", "_"));
		statement.add(epl_Filter);
        eplNames.add(statement);
        
		try {
			fr.append(epl_TE+"\n\n");
			fr.append(epl_Correlate+"\n\n");
			fr.append(epl_Filter+"\n\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	private static int countANDS(String pred) {
		// TODO Auto-generated method stub
		System.out.println("Start count ANDS -- with --- "+pred);
		int counter = 0;
		if(pred.contains("#")) {
			System.out.println("Expression contains ###### ");
			String regex = "([(]?(([a-zA-Z&\\-\\d\\s\\_\\(\\)]*)*)[#]?(R)?)[)]?";
			Pattern pt = Pattern.compile(regex);
			Matcher matcher = pt.matcher(pred);
			while (matcher.find()) {
				String anding = matcher.group(2).trim();
				if (anding.length() > 0 && anding.contains("&") ) {
					System.out.println("Expression contains &&& " + anding);
					counter = 0;
					String regexAND = "([(]?(([a-zA-Z\\d-\\s\\_\\(\\)]*)*)[&]?(R)?)[)]?";
					Pattern ptAND = Pattern.compile(regexAND);
					Matcher matcherAND = ptAND.matcher(anding);
					while (matcherAND.find()) {
						if (matcherAND.group(2).length() > 0) {
							counter++;
						}
					}
				}
			}
		}
		else
		{
			System.out.println("Expression contains &&&&&&&& ");
			counter = 0;
			String regex = "([(]?(([a-zA-Z\\d-\\s\\_\\(\\)]*)*)[&]?(R)?)[)]?";
			Pattern pt = Pattern.compile(regex);
			Matcher matcher = pt.matcher(pred);
			while (matcher.find()) {
				if (matcher.group(2).length() > 0) {
					counter++;
				}
			}
		}
		System.out.println("Counter ==> " + counter);

		return counter;
	}
//-------------------

	//Generate the Runner Java file for the given input rules based on the EPL rules generated
	private static void generateJava(FileWriter frJava,String[] startEvents) throws IOException {

		String definition = "package eg.cu.fci.is.correlator;\r\n" + 
				"\r\n" + 
				"import java.text.Format;\r\n" + 
				"import java.text.ParseException;\r\n" + 
				"import java.text.SimpleDateFormat;\r\n" + 
				"import java.io.FileWriter;\r\n" + 
				"import java.io.IOException;\r\n" +
				"import java.nio.file.Files;\r\n" + 
				"import java.nio.file.Paths;\r\n"+
				"import java.nio.charset.Charset;\r\n" + 
				"import java.util.Date;\r\n" + 
				"\r\n" + 
				"import com.espertech.esper.common.client.EPCompiled;\r\n" + 
				"import com.espertech.esper.common.client.configuration.Configuration;\r\n" + 
				"import com.espertech.esper.compiler.client.CompilerArguments;\r\n" + 
				"import com.espertech.esper.compiler.client.EPCompileException;\r\n" + 
				"import com.espertech.esper.compiler.client.EPCompiler;\r\n" + 
				"import com.espertech.esper.compiler.client.EPCompilerProvider;\r\n" + 
				"import com.espertech.esper.runtime.client.*;\r\n" + 
				"//import com.espertech.esper.compiler.client.*;\r\n" + 
				"//import com.espertech.esper.client;\r\n" + 
				"import eg.cu.fci.is.correlator.events.*;\r\n" + 
				"import eg.cu.fci.is.correlator.source.UnlabeledEventSource;\r\n" + 
				"\r\n" + 
				"public class Runner_"+javaClass+" {\r\n" + 
				"\r\n" + 
				"	 private static long initTime =0; \r\n" +
				"\r\n" + 
				"    public static void main(String[] args) throws InterruptedException, IOException {\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"    	System.out.println(\"***** APP START *****\"); \r\n" +
				"    	\r\n" + 
				"    	String logName = \" "+javaClass+" \";\r\n" + 
				"    	String logFailed = \"_fail\";\r\n" + 
				"    	String logSuffix = \"_latency_mem_run\";\r\n" + 
				"    	String logProbSuffix = \"_prob\";\r\n" + 
				"    	double logProb = 0.0;\r\n" + 
				"    	int logRun = 1;"+
				"    	\r\n" + 
				"        String filePath = \"C:\\\\IH\\\\Docs\\\\eclipse-ws2019\\\\CEPForEventCorrelation_latency\\\\\"+logName+\"_unLabeled.csv\";\r\n" + 
				"    	String labeledEvents = \"C:\\\\IH\\\\Docs\\\\eclipse-ws2019\\\\CEPForEventCorrelation_latency\\\\Loop1000\\\\Loop1000_latency_mem2\\\\\"+logName+logProbSuffix+logProb+logSuffix+logRun+\".csv\";\r\n" + 
				"    	String failedEvents = \"C:\\\\IH\\\\Docs\\\\eclipse-ws2019\\\\CEPForEventCorrelation_latency\\\\Loop1000\\\\Loop1000_latency_mem2\\\\\"+logName+logProbSuffix+logProb+logSuffix+logRun+logFailed+\".csv\";\r\n" + 
				"    	\r\n" + 
				"    	FileWriter frLabeled = new FileWriter(labeledEvents,true);\r\n" + 
				"        long countLines  = Files.lines(Paths.get(labeledEvents), Charset.defaultCharset()).count(); \r\n" + 
				"        if(countLines == 0)\r\n" + 
				"        {\r\n" + 
				"        	frLabeled.append(\"CaseID,Activity,Timestamp,Probability,initTime,sysTime\\n\");\r\n" + 
				"        	frLabeled.flush(); \r\n" + 
				"        }\r\n" + 
				"        FileWriter frFailed = new FileWriter(failedEvents,true);\r\n" + 
				"        long countLinesFail  = Files.lines(Paths.get(failedEvents), Charset.defaultCharset()).count(); \r\n" + 
				"        if(countLinesFail == 0)\r\n" + 
				"        {\r\n" + 
				"        	frFailed.append(\"Activity,Timestamp,initTime,sysTime\\n\");\r\n" + 
				"        	frFailed.flush(); \r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"\r\n"+
				"        EPCompiler compiler = EPCompilerProvider.getCompiler();\r\n" + 
				"\r\n" + 
				"        Configuration configuration = new Configuration();\r\n" + 
				"        configuration.getRuntime().getExecution().setPrioritized(true);\r\n" + 
				"\r\n" + 
				"        // Here you register your event types\r\n" + 
				"        configuration.getCommon().addEventType(Cases.class);\r\n" + 
				"        configuration.getCommon().addEventType(UnlabeledEvent.class);\r\n" + 
				"        configuration.getCommon().addEventType(FilterLabeledEvent.class);\r\n" + 
				"        configuration.getCommon().addEventType(LabeledEvent.class);\r\n" + 
				"        configuration.getCommon().addEventType(TempEvent.class);\r\n" + 
				"        configuration.getCommon().addEventType(FailCorrelation.class);" + 
				"\r\n";
		
		frJava.append(definition);
        
		String eplsDef = "";
		String eplCorrDef = "";
		String eplFilterDef = "";
		
        for (int j=0; j<eplNames.toArray().length;j++) {
        	Object[] tempS = eplNames.get(j).toArray();
        	if(tempS[0].toString().contains("Correlate_")) {
        		eplCorrDef += "        String epl_"+tempS[0].toString()+" =\" "+tempS[1].toString()+"\";"+"\n\n";
        	}
        	else if(tempS[0].toString().contains("Filter_")) {
        		eplFilterDef += "        String epl_"+tempS[0].toString()+" =\" "+tempS[1].toString()+"\";"+"\n\n";
        	}
        	else {
        		eplsDef += "        String epl_"+tempS[0].toString()+" =\" "+tempS[1].toString()+"\";"+"\n\n";
        	}
        }
        frJava.append(eplsDef);
        frJava.append(eplCorrDef);
        frJava.append(eplFilterDef);
        
        
        String eplComDepCorrelateDef = "";   
        String eplComDepFilterDef = ""; 
        String epCompileDeployDef = 
        		"        // Get a runtime\r\n" + 
        		"        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);\r\n" + 
        		"        EPDeploymentService  deploymentService = runtime.getDeploymentService();\r\n" + 
        		"\r\n" + 
        		"        CompilerArguments arguments = new CompilerArguments(configuration);\r\n" + 
        		"        arguments.getPath().add(runtime.getRuntimePath());\r\n" + 
        		"\r\n" ;
        
        for (int j=0; j<eplNames.toArray().length;j++) {
        	Object[] tempS = eplNames.get(j).toArray();
        	//for (int k=0; k<tempS.length;k++) {
        		//System.out.println("Yet another "+tempS[k].toString());
        	//}
 
        	if(tempS[0].toString().contains("Correlate_")) {
        		eplComDepCorrelateDef +="        EPCompiled epCompiled_"+tempS[0].toString()+";\r\n" ;
        		eplComDepCorrelateDef +="        EPDeployment deployment_"+tempS[0].toString()+";\r\n\n" ;
        	}
        	else if(tempS[0].toString().contains("Filter_")) {
        		eplComDepFilterDef +="        EPCompiled epCompiled_"+tempS[0].toString()+";\r\n" ;
        		eplComDepFilterDef +="        EPDeployment deployment_"+tempS[0].toString()+";\r\n\n" ;
        	}
        	else {
	        	epCompileDeployDef +="        EPCompiled epCompiled_"+tempS[0].toString()+";\r\n" ;
	        	epCompileDeployDef +="        EPDeployment deployment_"+tempS[0].toString()+";\r\n\n" ;
        	}
    	}
        frJava.append(epCompileDeployDef);
        frJava.append(eplComDepCorrelateDef);
        frJava.append(eplComDepFilterDef);
        
        
        
        String comDepCorrelate = "";
        String comDepFilter = "";
        String comDepOther = "";
        frJava.append("        try {\r\n");
        for (int j=0; j<eplNames.toArray().length;j++) {
        	Object[] tempS = eplNames.get(j).toArray();
        	if(tempS[0].toString().contains("Correlate_") ) {
        		comDepCorrelate += "            epCompiled_"+tempS[0].toString()+" = EPCompilerProvider.getCompiler().compile(epl_"+tempS[0].toString()+", arguments2);\r\n" ; 
        		comDepCorrelate += "            deployment_"+tempS[0].toString()+" = deploymentService.deploy(epCompiled_"+tempS[0].toString()+");\r\n\n" ;
        	}
        	else if(tempS[0].toString().contains("Filter_") ) {
        		comDepFilter += "            epCompiled_"+tempS[0].toString()+" = EPCompilerProvider.getCompiler().compile(epl_"+tempS[0].toString()+", arguments3);\r\n" ; 
        		comDepFilter += "            deployment_"+tempS[0].toString()+" = deploymentService.deploy(epCompiled_"+tempS[0].toString()+");\r\n\n" ;
        	}
        	else {
        		comDepOther += "            epCompiled_"+tempS[0].toString()+" = compiler.compile(epl_"+tempS[0].toString()+", arguments);\r\n" ; 
        		comDepOther += "            deployment_"+tempS[0].toString()+" = deploymentService.deploy(epCompiled_"+tempS[0].toString()+");\r\n\n" ;
        	}
        }
        String args2 = "            CompilerArguments arguments2 = new CompilerArguments(configuration);\r\n" + 
        		"            arguments2.getPath().add(runtime.getRuntimePath());\r\n";
        String args3 = "            CompilerArguments arguments3 = new CompilerArguments(configuration);\r\n" + 
        		"            arguments3.getPath().add(runtime.getRuntimePath());\r\n";        
        frJava.append(comDepOther);
        frJava.append(args2+"\n");
        frJava.append(comDepCorrelate+"\n");
        frJava.append(args3+"\n");
        frJava.append(comDepFilter+"\n");
        frJava.append(
        		"        } catch (EPCompileException | EPDeployException ex) {\r\n" + 
        		"            // handle exception here\r\n" + 
        		"            throw new RuntimeException(ex);\r\n" + 
        		"        }");
             
        String eplClock = "\r\n" + "\r\n" + 
        		"        EPEventService eventService = runtime.getEventService();\r\n" + 
        		"        eventService.clockExternal();\r\n" + 
        		"        eventService.advanceTime(0);\r\n" + 
        		"        System.out.println(\"System clock \"+eventService.getCurrentTime());\r\n" +
        		"        SimpleDateFormat formatter = new SimpleDateFormat(\"MM/dd/yyyy HH:mm:ss\");  \r\n" + 
        		"        Date date = new Date();  \r\n" + 
        		"        System.out.println(\"Start time \"+formatter.format(date));"+
        		"\r\n" ; 
        frJava.append(eplClock);
        
        String eplListenStatements = "";
        String eplListenCorrelate = "";
        String eplListenFilter = "";
        for (int j=0; j<eplNames.toArray().length;j++) {
        	Object[] tempS = eplNames.get(j).toArray();
        	if(tempS[0].toString().contains("Correlate_")) {
        		eplListenCorrelate +="        EPStatement statement_"+tempS[0].toString()+" = runtime.getDeploymentService().getStatement(deployment_"+tempS[0].toString()+".getDeploymentId(), \""+tempS[0].toString()+"\");\r\n" ;
        	}
        	else if(tempS[0].toString().contains("Filter_")) {
        		eplListenFilter +="        EPStatement statement_"+tempS[0].toString()+" = runtime.getDeploymentService().getStatement(deployment_"+tempS[0].toString()+".getDeploymentId(), \""+tempS[0].toString()+"\");\r\n" ;
        	}
        	else {
        		eplListenStatements +="        EPStatement statement_"+tempS[0].toString()+" = runtime.getDeploymentService().getStatement(deployment_"+tempS[0].toString()+".getDeploymentId(), \""+tempS[0].toString()+"\");\r\n" ;  
        	}
        }
        
        frJava.append(eplListenStatements+"\r\n");
        frJava.append(eplListenCorrelate+"\r\n");
        frJava.append(eplListenFilter+"\r\n");
        
        
        String listeners = 
        		"        statement_Unlabeled.addListener((newData, oldData, s, r) -> {\r\n" + 
        		"//            String activity = (String) newData[0].get(\"activity\");\r\n" + 
        		"//            long timestamp = (long) newData[0].get(\"timestamp\");\r\n" + 
        		"//      	initTime = (long) newData[0].get(\"sysTime\");\r\n" + 
        		"//          long sysTime = (long) newData[0].get(\"sysTime\");\r\n" + 
        		"          initTime = (long) newData[0].get(\"sysTime\");\r\n" + 
        		"//          initTime = sysTime;\r\n" + 
        		"//            Date ts = new Date(timestamp);\r\n" + 
        		"//            Format format = new SimpleDateFormat(\"MM/dd/yyyy HH:mm:ss\");\r\n"+
        		"//          System.out.println(\"UnlabeledEvent [\"+ activity+\" , \"+format.format(ts)+\",\"+sysTime+\"]\");//+caseID + \" , \"\r\n" + 
        		"        });\r\n" + 
        		"\r\n" + 
        		"\r\n" + 
        		"        statement_FilterLabeledEvent.addListener((newData, oldData, s, r) -> {\r\n" + 
        		"            long caseID = (long) newData[0].get(\"caseID\");\r\n" + 
        		"            String activity = (String) newData[0].get(\"activity\");\r\n" + 
        		"            long timestamp = (long) newData[0].get(\"timestamp\");\r\n" + 
        		"            double probability = (double) newData[0].get(\"probability\");\r\n" +
        		"            //initTime = (long) newData[0].get(\"initTime\");\r\n" + 
        		"            long sysTime = (long) newData[0].get(\"sysTime\");\r\n"+
        		"            Date ts = new Date(timestamp);\r\n" + 
        		"            Format format = new SimpleDateFormat(\"MM/dd/yyyy HH:mm:ss\");\r\n" + 
        		"            try {\r\n" + 
        		"		    	frLabeled.append(caseID + \",\" + activity+\",\"+format.format(ts)+\",\"+probability+\",\"+initTime+\",\"+sysTime+\"\\n\");\r\n" +
        		"		    	frLabeled.flush(); \r\n"+
        		"			} catch (IOException e) {\r\n" + 
        		"				e.printStackTrace();\r\n" + 
        		"			}\r\n" + 
        		"//		    System.out.println(\"*** FilterLabeledEventEvent [\"+caseID + \" , \" + activity+\" , \"+format.format(ts)+\" , \"+probability+\" , \"+initTime+\" , \"+sysTime+\"]\");\r\n" + 
        		"        });"+
        		"\r\n" + 
        		"//        statement_Labeled.addListener((newData, oldData, s, r) -> {\r\n" + 
        		"//            long caseID = (long) newData[0].get(\"caseID\");\r\n" + 
        		"//            String activity = (String) newData[0].get(\"activity\");\r\n" + 
        		"//            long timestamp = (long) newData[0].get(\"timestamp\");\r\n" + 
        		"//            double probability = (double) newData[0].get(\"probability\");\r\n" + 
        		"//            Date ts = new Date(timestamp);\r\n" + 
        		"//            Format format = new SimpleDateFormat(\"MM/dd/yyyy HH:mm:ss\");\r\n"+
        		"//\r\n" +
        		"////	         try { \r\n" + 
        		"////		    	frLabeled.append(caseID + \",\" + activity+\",\"+format.format(ts)+\",\"+probability+\"\\n\");\r\n" +
        		"////		    	frLabeled.flush();\r\n" + 
        		"////			} catch (IOException e) {\r\n" + 
        		"////				e.printStackTrace();\r\n" + 
        		"////			}\r\n" + 
        		"//		    System.out.println(\"*** LabeledEvent [\"+caseID + \" , \" + activity+\" , \"+format.format(ts)+\" , \"+probability+\"]\");\r\n" + 
        		"//        });\r\n" + 
        		"//\r\n"+
        		"//        statement_Temp.addListener((newData, oldData, s, r) -> {\r\n" + 
        		"//            long caseID = (long) newData[0].get(\"caseID\");\r\n" + 
        		"//            String activity = (String) newData[0].get(\"activity\");\r\n" + 
        		"//            long timestamp = (long) newData[0].get(\"timestamp\");\r\n" + 
        		"//            Date ts = new Date(timestamp);\r\n" + 
        		"//            Format format = new SimpleDateFormat(\"MM/dd/yyyy HH:mm:ss\");\r\n"+
        		"//            System.out.println(\"--- TempEvent [\"+caseID + \" , \" + activity+\" , \"+format.format(ts)+\"]\");\r\n" + 
        		"//        });\r\n\n"+
        		"\r\n" + 
        		"        statement_FailCorrelation.addListener((newData, oldData, s, r) -> {\r\n" + 
        		"            String activity = (String) newData[0].get(\"activity\");\r\n" + 
        		"            long timestamp = (long) newData[0].get(\"timestamp\");\r\n" + 
        		"            Date ts = new Date(timestamp);\r\n" + 
        		"	        Format format = new SimpleDateFormat(\"MM/dd/yyyy HH:mm:ss\");\r\n" + 
        		"            long sysTime = (long) newData[0].get(\"sysTime\");\r\n" + 
        		"            try {\r\n" + 
        		"		    	frFailed.append(activity+\",\"+format.format(ts)+\",\"+initTime+\",\"+sysTime+\"\\n\");\r\n" + 
        		"		    	frFailed.flush(); \r\n" + 
        		"			} catch (IOException e) {\r\n" + 
        		"				e.printStackTrace();\r\n" + 
        		"			}\r\n" + 
        		"        });";
        
        frJava.append(listeners);
        
        String TE_listeners = "";
        String Correlate_listners = "";
        String Filter_listners = "";
        for (int j=0; j<eplNames.toArray().length;j++) {
        	Object[] tempS = eplNames.get(j).toArray();
        	if(tempS[0].toString().contains("Correlate_")) {
        		Correlate_listners += "        statement_"+tempS[0].toString()+".addListener((newData, oldData, s, r) -> {System.out.println(\"Event-"+tempS[0].toString()+"-instance \"+newData[0].getUnderlying()); }); \r\n";
        	}
        	else if(tempS[0].toString().contains("Filter_")) {
        		Filter_listners += "        statement_"+tempS[0].toString()+".addListener((newData, oldData, s, r) -> {System.out.println(\"Event-"+tempS[0].toString()+"-instance \"+newData[0].getUnderlying()); }); \r\n";
        	}
        	else if(tempS[0].toString().contains("TE_")) {
        		TE_listeners += "        statement_"+tempS[0].toString()+".addListener((newData, oldData, s, r) -> {System.out.println(\"Event-"+tempS[0].toString()+"-instance \"+newData[0].getUnderlying()); }); \r\n";
        	}
        }
        
        frJava.append("/*\r\n");
        frJava.append(TE_listeners+"\r\n");
        frJava.append(Correlate_listners+"\r\n");
        frJava.append(Filter_listners+"\r\n");
        frJava.append("*/\r\n\n");
        
        String closingJava = 
        		"        new UnlabeledEventSource(filePath, "+logLength+", eventService, \"UnlabeledEvent\").run();//take 35 records from UnlabeledEvent\r\n" + 
        		"\r\n" +
        		"        frLabeled.close(); \r\n"+
        		"        frFailed.close(); \r\n" + 
        		"        "+
        		"        date = new Date(); \r\n" + 
        		"        System.out.println(\"End time \"+formatter.format(date));\r\n" + 
        		"        Runtime rt = Runtime.getRuntime();\r\n" + 
        		"        long memoryMax = rt.maxMemory();\r\n" + 
        		"        long memoryUsed = rt.totalMemory() - rt.freeMemory();\r\n" + 
        		"        System.out.println(\"memoryUsed \"+memoryUsed/(1024*1024)+\" MB\");\r\n" + 
        		"        System.out.println(\"memoryMax \"+memoryMax/(1024*1024)+\" MB\");\r\n" + 
        		"        double memoryUsedPercent = (memoryUsed * 100.0) / memoryMax;\r\n" + 
        		"        System.out.println(\"memoryUsedPercent: \" + memoryUsedPercent);\r\n" + 
        		"\r\n" + 
        		"    }"+
        		"\r\n" + 
        		"}";
        frJava.append(closingJava);
		
	}
/*
	//To print List of List of Strings
	private static void printListofList(List<List<String>> eplStatementsNames) {
		for (int j=0; j<eplStatementsNames.toArray().length;j++) {
        	Object[] tempS = eplStatementsNames.get(j).toArray();
        	for (int k=0; k<tempS.length;k++) {
        		System.out.println("Yet another "+tempS[k].toString());
        	}
        }
    }
	
	//To print List of Strings
	private static void printList(List<String> eplStatementsNames) {
		for (String string : eplStatementsNames) {
            System.out.println(string);
        }
    }

*/

	
}
