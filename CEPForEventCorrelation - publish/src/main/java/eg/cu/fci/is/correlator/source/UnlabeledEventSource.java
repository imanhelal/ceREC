package eg.cu.fci.is.correlator.source;

import com.espertech.esper.common.client.EventSender;
import com.espertech.esper.runtime.client.EPEventService;
import eg.cu.fci.is.correlator.events.UnlabeledEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UnlabeledEventSource implements Runnable {

    private static final long serialVersionUID = -2873892890991630938L;
    private final EventSender sender;
    private final EPEventService service;
    private boolean running = true;
    private String filePath;
    private int numRecordsToEmit = Integer.MAX_VALUE;

    public UnlabeledEventSource(String filePath, EPEventService service, String type) {
        this.filePath = filePath;
        this.sender = service.getEventSender(type);
        this.service = service;
    }

    public UnlabeledEventSource(String filePath, int numRecordsToEmit, EPEventService service, String type) {
        this.filePath = filePath;
        this.sender = service.getEventSender(type);
        this.service = service;
        this.filePath = filePath;
        this.numRecordsToEmit = numRecordsToEmit;
    }

    @Override
    public void run() {
    	long lastTime = 0;
        try {
            int recordsEmitted = 0;
            BufferedReader reader;

            reader = new BufferedReader(new FileReader(filePath));

            String line;
            reader.readLine();//skip the header line
            line = reader.readLine();
            //long ts = 0;
            while (running && line != null && recordsEmitted <= numRecordsToEmit) {
                String[] data = line.split(",");
                //ts = Integer.parseInt(data[0].trim());
                //*
                String inputString = data[0].trim();
                SimpleDateFormat df1 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                //SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date d = df1.parse(inputString);
                long timeInMillis = d.getTime();
                //*/
                //System.out.println("Event "+data[1]+" "+d+" Time  = " + timeInMillis);
                
                //System.out.println("Time = " + ts);//timeInMillis ts

                send(data,timeInMillis);
                //send(data,ts);
                Thread.sleep(10);
                recordsEmitted++;
                line = reader.readLine();
                lastTime = timeInMillis;
            }
            reader.close();
            //Thread.sleep(1000);
            lastTime ++;
            //service.advanceTime(Long.MAX_VALUE+1);
            service.advanceTime(lastTime);
            System.out.println("service.getCurrentTime() "+service.getCurrentTime());
            System.out.println("Long.MAX_VALUE+1 "+Long.MAX_VALUE+1);
            System.out.println("lastTime "+lastTime);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch(NumberFormatException ne) {
        	ne.printStackTrace();
        } catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void send(String[] data, int ts)  {
        if (service.getCurrentTime() < ts) {
            service.advanceTime(ts);
        }
        //Here you can control what to send on the stream
        sender.sendEvent(new UnlabeledEvent(-1, data[0].trim(), ts,-1));
//        try {
//			Thread.sleep(900);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }
    
    private void send(String[] data, long ts) {

        //Here you can control what to send on the stream
        sender.sendEvent(new UnlabeledEvent(-1, data[1].trim(), ts,-1));

        if (service.getCurrentTime() < ts) {
            service.advanceTime(ts);
        }
//        try {
//			Thread.sleep(900);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

}