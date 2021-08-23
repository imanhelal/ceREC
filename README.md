# ceREC
CEP-based Runtime Event Correlation (ceREC) Implementation and Evaluation
The source code associates the paper "Online Correlation for Unlabeled Process Events: A Flexible CEP-based Approach" By: Iman Helal, Ahmed Hany

## **Purpose:**
We introduce an approach that correlates unlabeled events received on a stream. Given a set of start activities, our approach correlates unlabeled events to a case identifier. Our approach is probabilistic. That implies a single uncorrelated event can be assigned to one or more case identifiers with different probabilities. Moreover, our approach is flexible. That is, the user can supply domain knowledge in the form of constraints that reduce the correlation space. This knowledge can be supplied while the application is running. We realize our approach using complex event processing (CEP) technologies. We implemented a prototype on top of Esper, a state of the art industrial CEP engine.

### **Keywords:**
Process Mining; Uncorrelated events; Event Streams; Complex Event Processing

## **To run our code:**
1.	Install Eclipse Modeling Tools - Version: 2019-09 R (4.13.0)
2.	Install Maven
3.	Run GenerateEsperEPL package to generate Esper statements:

	*	Specify the log filename (as javaClass variable), user-defined ranking score threshold (default 0), logLength (length varies per each log to run the stream in Esper)
	*	inputRules in (.txt) format
	*	Select Heuristic data in (.csv) format (if any)
	* 	Specify the output folder inside the eg.cu.fci.is.correlator package in (2) to generate Runner file.
	
4.	Run the generated Runner file in eg.cu.fci.is.correlator modify details accordingly
	
	*	Ranking score threshold
	*	The output is stored in two files one for the correlated events and another for the failed events inside /CEPForEventCorrelation folder.

## **Experiements:**

There are 4 logs: WABO CoSeLoG, BPI2013Closed, Synthetic Log, BPI2017

There following folders that contain the results of our experiements:
1.	ce-REC outputs: All logs with latency and memory calculations
2.	REC outputs: All logs (databases) with latency and memory calculations
3.	Logs: All logs ran in ce-REC to experiement with changed ranking score. 
 
Extra folder *MonitorDB* is added for running REC approach (with updated code for latency).




*This research work is copy righted to: Iman Helal, Ahmed Hany @2020-2021*
