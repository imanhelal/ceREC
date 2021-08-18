'''
This code is copyrighted to Iman Helal @2016-2017 Research work
Information System Department, Faculty of computers and Information System
Cairo University, Egypt
'''
#from _overlapped import NULL
from pip._vendor.cachecontrol import heuristics

'''
# Inputs 
#     Arguments from user:
#         (0.1) Get Heuristics for each activity
#         (0.2) Get probability threshold for activity
#     Request for model structure
#         (0.3) Get Model Structure derived from [Behavioral Profile Java], 
#            or something supporting loops -- May need to update of BP 
#            -- or require from user to input it in specific format
 
# Pre-processing steps:
# (1.1) Get the first attribute - node (start_activity) -- derived from Model Structure
# (1.2) Build ModelStructure - Matrix of [ModelActivities-Relationships]

# Empty strucutres to use:
# (2.1) Cases to be linked to nodes - Index [Cases] (not applicable for time window)
# (2.2) Probability of each activity -will be calculated and updated with each incoming events - List (Probabilities)

# WatchHandler is called from Observer
# Watching file for detecting stream of events [S]
# |- for each incoming event:
# |----- If (1.2)[start_activity] generate new case in Cases -- i.e. create Node n, and add it to Cases as new case
# |----- Update (2.2)[Probabilities] for the activity corresponding to event
# |-----
# |----- # Need ternary relationship between Nodes, Patterns, Cases (check only nodes that satisfy both case(s) and pattern(s))
# |-----
# |----- Check which pattern is waiting for the incoming event (these are parent nodes of the event)
# |---------- Check for each if it is a possible parent - add PossibleParents 
# |--------------- Filter by case's time window based on pre-processing in (1.1)  w.r.t event -- 
# |--------------- Filter parents according to (0.2) of given activity w.r.t event -- remove from PossibleParents 
# |---------- then add node to the corresponding pattern in Patterns index
# |--------------- Check for each for inserted node in Patterns -- add node to the Cases index
'''

'''
    1- Specifying the model structure in a separate graph
    2- Define each incoming event into its own label (linking to model structure and activities), based on Model Heuristics 
        -- a new instance is added with specific identifier of (caseID, incomingEventSerial)
    3- Embedded inside (step 2), add instance to its respective case.
    4- Special case for step 3, if the incoming event is a startActivity of Model, then a new case is defined with empty dict()
    5- Check directly after adding if constraints are satisfied
        5.1- Embedded inside this algorithm, the structure constraints.(need further investigation)
        5.2- Required to check Resource constraints
    
'''

import sqlite3

import sys
import math
import os
import shutil
import csv 
import psutil


import g_config
from TraverseGraph import TraverseGraph
from PN import PetriNet

from datetime import datetime
import time # to calculate the processing time of the algorithm & used in watchdog 
# required imports for watchdog
import logging
from collections import deque
from watchdog.observers import Observer
#from watchdog.events import LoggingEventHandler
from watchdog.events import FileSystemEventHandler
# 
# import sys
# import time
# import logging
# from watchdog.observers import Observer
# from watchdog.events import LoggingEventHandler

import gc


'''################################################################
############# The one used with required results

class MyDBConn2(object):
    _instance = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = object.__new__(cls)
            
            try:
                print('connecting to database 2 ...')
                cls._instance._db_connection = sqlite3.connect('multipleDBConn.db')#, isolation_level=None) # for testing
                #conn = sqlite3.connect('file:BPI2013Memory?mode=memory&cache=shared', isolation_level=None) # for running
                cls._instance._db_cursor = cls._instance._db_connection.cursor()
                                
            except Exception as error:
                print('Error: connection 2 not established '.format(error))
                sqlite3._instance = None

            else:
                print('connection 2 established\n')

        return cls._instance
    
    def __init__(self):
        self._db_connection = self._instance._db_connection
        self._db_cur = self._instance._db_cursor
        #conn = sqlite3.connect('file:BPI2013Memory?mode=memory&cache=shared', isolation_level=None) # for running
    
    def getDBInstance(self):
        return self._db_connection
        
    
    def getCursorInstance(self):
        return self._db_cur
    
    def __del__(self):
        self._db_connection.commit()
        self._db_connection.close()
    



################################################################
'''

'''########################################################################
'''
class WatchHandler(FileSystemEventHandler):

    counter = 0
    fileSize = 0
    prevFileSize = 0
    

    # Detect modification on the specified folder, mostly has one file only
#     def on_moved(self, event):
#         #print(f"ok ok ok, someone moved {event.src_path} to {event.dest_path}")
#         #g_config.process = psutil.Process(os.getpid())
# #         g_config.end_process_memory = process.memory_percent() - g_config.start_process_memory
#         
#         g_config.end_process_memory +=  g_config.process.memory_info().vms#- g_config.start_process_memory
#         print("end memory percent of the processXXX",g_config.end_process_memory)
# 
#     
    # Detect modification on the specified folder, mostly has one file only
    def on_modified(self, event):
        #print ("count ",self.counter)
        #print("filename to observe: ",g_config.observing_filename)
        if self.counter == 0:
            g_config.start_time_program = datetime.now().strftime('%m/%d/%Y  %I:%M:%S %p')
            print ('Starting program ',g_config.start_time_program)
                   
        g_config.count_events = self.file_len()
        #print ("fileSize on change ",g_config.count_events)
        
        self.get_last_row(g_config.count_events, g_config.observing_filename)
        self.counter=self.counter+1 # to be ready for next observation


    def file_len(self):
        #print("checking file length function")
        with open(g_config.observing_filename, 'r') as f:
            for i, l in enumerate(f,1):
                pass
        return i

    def get_last_row(self,length,csv_filename):
        #print("Getting last row function")
        self.fileSize = length
        # read last n updated lines from file
        readLines = self.fileSize - self.prevFileSize
        self.prevFileSize = self.fileSize
        #print("Reading ",readLines," lines")
        #######readTS = datetime.now().strftime('%m/%d/%Y  %I:%M:%S %p') # for reading timestamp in the watcher
        with open(csv_filename, 'r') as f: 
            reading = csv.DictReader(f)
            multipleLines =  deque(reading, readLines)
            header = reading.fieldnames
                        
            for l in multipleLines:
                #print("in loop",l) 
                timestamp = l[header[0]]
                ##timestamp = datetime.strptime(l[header[0]],'%Y-%m-%d %H:%M:%S')#l[header[0]].strftime('%m/%d/%Y  %I:%M:%S %p')
                event = l[header[1]].lower()
                life_cycle = l[header[2]].lower()
                resource = l[header[3]].lower()
                process = psutil.Process(os.getpid())
                #g_config.process = psutil.Process(os.getpid())
#                 g_config.end_process_memory = process.memory_percent() - g_config.start_process_memory
               
#                 g_config.end_process_memory =  g_config.process.memory_info().vms/(1024*1024) #- g_config.start_process_memory
#                 print("end memory percent of the process",g_config.end_process_memory," MB")
             
                #print('Reading in loop: ', header,' ',timestamp, ' ', event, ' ', life_cycle, ' ', resource)
                # here is where my code will be further called for each incoming event
                
                #tg = TraverseGraph(timestamp, event, life_cycle, resource, g_config.StartActivity)#, allActivities, Parents,InstancesPool, ActivitiesCounts, ActivitiesProbabilities)

#                 tg = TraverseGraph(timestamp, event, g_config.StartActivity)#, allActivities, Parents,InstancesPool, ActivitiesCounts, ActivitiesProbabilities)
#                 tg = TraverseGraph(g_config.cSQL,g_config.conn,timestamp, event, life_cycle, resource, g_config.StartActivity, readTS)#, allActivities, Parents,InstancesPool, ActivitiesCounts, ActivitiesProbabilities)
                tg = TraverseGraph(g_config.cSQL,g_config.conn,timestamp, event, life_cycle, resource, g_config.StartActivity)#, allActivities, Parents,InstancesPool, ActivitiesCounts, ActivitiesProbabilities)









'''
########################################################################
'''
'''
To check code performance:
Open console on the path of the application and run:
python -m cProfile my_code.py
'''



###############################################################################
class VariablesClearance:
    def clear_all(self):
        """Clears all the variables from the workspace of the spyder application."""
        gl = globals().copy()
        for var in gl:
            if var[0] == '_': continue
            if 'func' in str(globals()[var]): continue
            if 'module' in str(globals()[var]): continue
     
            del globals()[var]


if __name__ == "__main__":

    """Clears all the variables from the workspace of the spyder application."""
#     gl = globals().copy()
#     for var in gl:
#         if var[0] == '_': continue
#         if 'func' in str(globals()[var]): continue
#         if 'module' in str(globals()[var]): continue
# 
#         del globals()[var]

#     vc = VariablesClearance()
#     vc.clear_all()

    #proc = psutil.Process(os.getpid())
    gc.collect()
    #memory = proc.get_memory_info().rss # not working-gives error.
    
    
    start_time_program = time.time()
    
#     g_config.process = psutil.Process(os.getpid())
#     start_process_memory = process.memory_percent()
    print("Start memory percent of the process",g_config.start_process_memory)

#'''
    print ("Model Settings: ")
    
    # Written example here is: a->XOR((b),(c->d))->e
    # In either cases of xor/and -- e can come after label b or label d
    # In case of AND(X,Y)->Z -- This will have either X->Y->Z or Y->X->Z, so Z will have possible predecessor X or Y  
   
    pn = PetriNet()
#     g_config.StartActivity = pn.getStartTransition()  #'a' -- it is now a list of start transitions
#     g_config.allActivities=pn.getTransitionsList()
    g_config.DependencyActivities =pn.getModelDependencies2() #{'a':[], 'b':[['a']], 'c':[['a']], 'd':[['c']], 'e':[['b'],['d']]}
    g_config.StartActivity = pn.getStartActivity()  #'a' -- it is now a list of start transitions
    g_config.allActivities=pn.getAllActivities()
    g_config.PossibleStartActivity = pn.getPossibleStartActivity()
    
    ## This loop is added for BPI2013-closed log, since it won't append the other possible start activities on start activities.
    for pSA in g_config.PossibleStartActivity:
        g_config.StartActivity.append(pSA) 
    

    for A in g_config.DependencyActivities.keys():
        LabelDependents = g_config.DependencyActivities[A]
        xorEntries = []
        loopEntries = []
        andEntries = []
        andNonLoopEntries = []
        andLoopEntries = []
        if len(LabelDependents ) == 1: # sequence, AND cases
            Dependents = [num for elem in LabelDependents for num in elem] #flatten list
            if len(Dependents ) == 1:
                xorEntries.extend(Dependents)
            else:
                andLoopEntries = [l[1:] for l in Dependents if l[0]=='*']
                andNonLoopEntries = [l for l in Dependents if l[0]!='*']
                #andNonLoopEntries.extend(andLoopEntries)
                andEntries.extend(andNonLoopEntries)
                andEntries.extend(andLoopEntries)
        elif len(LabelDependents ) > 1: # xor, loop cases
            # need to flatten labels
            isAndEntries = [elem for elem in LabelDependents if len(elem)>1]
            otherEntries = [elem for elem in LabelDependents if len(elem)==1]
        
            andDependents = [num for elem in isAndEntries for num in elem] #flatten list
            andLoopEntries = [l[1:] for l in andDependents if l[0]=='*']
            andNonLoopEntries = [l for l in andDependents if l[0]!='*']
        
            Dependents = [num for elem in otherEntries for num in elem] #flatten list
            loopEntries = [l[1:] for l in Dependents if l[0]=='*']
            xorEntries = [l for l in Dependents if l[0]!='*']
            loopEntries.extend(andLoopEntries)
            xorEntries.extend(andNonLoopEntries)
        xorD = "("+','.join("'"+d+"'" for d in xorEntries)+")"
        loopD =  "("+','.join("'"+d+"'" for d in loopEntries)+")" 
        inD = "("+','.join("'"+d+"'" for d in andEntries)+")"
        
        g_config.DependencySemantics[A] = [xorEntries,loopEntries,andEntries,andNonLoopEntries,andLoopEntries, xorD, loopD, inD]
        
    print ('(1) Start activity = ', g_config.StartActivity, ' possible start activities = ', g_config.PossibleStartActivity)
    print ('(2) All activities = ', g_config.allActivities)
    print ('(3.1) Model dependencies per each activity = ',g_config.DependencyActivities)
    print ('(3.2) Model dependencies per each activity = ',g_config.DependencySemantics)
    # Structured once all structure is known from behavioral profile (distint events)
    #g_config.InstancePool = {'a':[], 'b':[], 'c':[], 'd':[], 'e':[]}
    
    # updated with each detected event in watcher
    #g_config.ActivitiesProbability = {'a':0.0, 'b':0.0, 'c':0.0, 'd':0.0, 'e':0.0}
    #g_config.ActivityCount = {'a':0, 'b':0, 'c':0, 'd':0, 'e':0}
    
    for act in g_config.allActivities:
        #print('act ' ,act)
        g_config.InstancePool[act] = []
        g_config.ActivitiesProbability[act] = 0.0
        g_config.ActivityCount[act] = 0
         
    InstancesPool = g_config.InstancePool
    ActivitiesProbabilities = g_config.ActivitiesProbability
    ActivitiesCounts = g_config.ActivityCount
    print('(4) Initial Instance Pool = ',g_config.InstancePool)
    print('(5) Initial ActivitiesProbability = ',g_config.ActivitiesProbability)
    print('(6) Initial ActivityCount = ',g_config.ActivityCount)
    
    
    #conn = sqlite3.connect('Loop_1000.db')
    #conn = sqlite3.connect('test.db')#, isolation_level=None) # for testing
    ##conn = sqlite3.connect('physical-start.db')#, isolation_level=None) # for testing in paper
#     conn = sqlite3.connect('BPIC2017-v4.db')#, isolation_level=None) # for testing
    #conn = sqlite3.connect('file:BPI2013Memory?mode=memory&cache=shared', isolation_level=None) # for running
#     cSQL = conn.cursor()
    # for first time only, to make sure the database created, table exists and clean, indexes are created
#     conn = MyDBConn2().getDBInstance()
#     cSQL = conn.cursor()
    g_config.cSQL.execute("DROP TABLE IF EXISTS CorrelatedEvents;")
#     g_config.cSQL.execute("CREATE TABLE CorrelatedEvents(instanceID VARCHAR(20) primary key, inst_timestamp DATETIME, inst_caseID INTEGER default 0, inst_activity VARCHAR(50), read_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, actual_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, event_serial INTEGER REFERENCES Events(serial), life_cycle VARCHAR(20) DEFAULT 'completed', event_resource VARCHAR(40) DEFAULT 'none', trust DOUBLE, warning CHAR(1) DEFAULT '');")

#    Original Table before latency
#     g_config.cSQL.execute("CREATE TABLE CorrelatedEvents(instanceID VARCHAR(20) primary key, inst_timestamp DATETIME, inst_caseID INTEGER default 0, inst_activity VARCHAR(50), actual_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, event_serial INTEGER REFERENCES Events(serial), life_cycle VARCHAR(20) DEFAULT 'completed', event_resource VARCHAR(40) DEFAULT 'none', trust DOUBLE, warning CHAR(1) DEFAULT '');")
    g_config.cSQL.execute("CREATE TABLE CorrelatedEvents(instanceID VARCHAR(20) primary key, inst_timestamp DATETIME, inst_caseID INTEGER default 0, inst_activity VARCHAR(50), actual_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, event_serial INTEGER REFERENCES Events(serial), life_cycle VARCHAR(20) DEFAULT 'completed', event_resource VARCHAR(40) DEFAULT 'none', trust DOUBLE, warning CHAR(1) DEFAULT '', init_correlate_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, end_correlate_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);")
    #cSQL.execute("CREATE TABLE CorrelatedEvents(instanceID VARCHAR(20) PRIMARY KEY, inst_timestamp DATETIME, inst_caseID INTEGER default 0, inst_activity VARCHAR(50), actual_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, event_serial INTEGER, trust DOUBLE, warning CHAR(1) DEFAULT '');")
    g_config.cSQL.execute("CREATE INDEX 'eventInstance_index_activity' ON CorrelatedEvents (inst_activity ASC);")
    g_config.cSQL.execute("CREATE INDEX 'eventInstance_index_caseID' ON CorrelatedEvents (inst_caseID ASC);")
    g_config.cSQL.execute("CREATE INDEX 'eventInstance_index_timestamp' ON CorrelatedEvents (inst_timestamp ASC);")
    g_config.cSQL.execute("CREATE INDEX 'eventInstance_index_combined' ON CorrelatedEvents (inst_caseID ASC, inst_activity ASC, inst_timestamp ASC,trust ASC);")
    g_config.conn.commit()
    #print("SQL stat g_config ",g_config.cSQL, " conn ",g_config.conn)
    
    # reading and preparing Heurisctics input
    #print ("reading Activity execution times metadata - heuristics")
    
    fin  = open(sys.argv[1], "r")
    reader = csv.DictReader(fin)
    rownum = 0
    fields= reader.fieldnames
    #print (fields)
    for row in reader:
            heuristic=[]
            for i in range(1, len(reader.fieldnames)):
                heuristic.append(float(row[reader.fieldnames[i]]))
            g_config.H[row[reader.fieldnames[0]].lower()]=heuristic   
    
    fin.close()
    
    updateHeuristic = []
    for e in g_config.H:
        #print("in updating heuristics for event ",e," its original heuristics: ",g_config.H[e])
        updateHeuristic = []
        updateHeuristic.append(g_config.H[e][0])
        updateHeuristic.append(g_config.H[e][1])
        minR=''
        maxR=''
        strQuery = ''
        if len(e)==2:
            #minR=str(g_config.H[e][0]-g_config.H[e][1])
            #maxR=str(g_config.H[e][0]+g_config.H[e][1])
            #strQuery = " diffTS >="+minR+" AND diffTS <= "+maxR
            strQuery = " diffTS >="+str(g_config.H[e][0]-g_config.H[e][1])+" AND diffTS <= "+str(g_config.H[e][0]+g_config.H[e][1])
            #updateHeuristic.append(str(g_config.H[e][0]-g_config.H[e][1]))
            #updateHeuristic.append(str(g_config.H[e][0]+g_config.H[e][1]))
        else:
            #minR=str(g_config.H[e][2])
            #maxR=str(g_config.H[e][3])
            #strQuery = " diffTS >="+minR+" AND diffTS <= "+maxR
            strQuery = " diffTS >="+str(g_config.H[e][2])+" AND diffTS <= "+str(g_config.H[e][3])
            #updateHeuristic.append(str(g_config.H[e][2]))
            #updateHeuristic.append(str(g_config.H[e][3]))
        ceil_avg = math.ceil(g_config.H[e][0])
        updateHeuristic.append(ceil_avg)
        #updateHeuristic.append(" diffTS >="+minR+" AND diffTS <= "+maxR)
        updateHeuristic.append(strQuery)
        g_config.H[e]=updateHeuristic
        
    
    
#     g_config.H={'a': [0.0, 0.0, 0.0, 0.0],
#                 'b': [150.0, 90.0, 60.0, 240.0],
#                 'c': [90.0, 30.0, 60.0, 120.0],
#                 'd': [60.0, 0.0, 60.0, 60.0], 
#                 'e': [120.0, 60.0, 60.0, 180.0], 
#                 'f': [120.0, 0.0, 120.0, 120.0], 
#                 'g': [240.0, 180.0, 60.0, 420.0], 
#                 'h': [180.0, 0.0, 180.0, 180.0], 
#                 'i': [120.0, 60.0, 60.0, 180.0],
#                 'j': [180.0, 60.0, 120.0, 240.0],
#                 'k': [60.0, 0.0, 60.0, 60.0],
#                 'l': [150.0, 90.0, 60.0, 240.0], 
#                 'm': [300.0, 240.0, 60.0, 540.0],
#                 'n': [60.0, 0.0, 60.0, 60.0]    
#                 }

    print ('(7) Input Heuristics = ' ,g_config.H)
    if sys.argv[2]:
        g_config.threshold = sys.argv[2]
    print ('(8) User threshold = ',g_config.threshold)
    print ('=====================================')
    print ('Dashboard output:')
    
    # I will need to check a way to keep the structure for model checking of constraints
    
    logging.basicConfig(level=logging.INFO,
                format='%(asctime)s - %(message)s',
                datefmt='%Y-%m-%d %H:%M:%S')

    event_handler = WatchHandler()# customized reaction to modified file
    observer = Observer()
    observer.schedule(event_handler, g_config.observing_path, recursive=False)
    observer.start()
    try:
        while True:
            time.sleep(0)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()
    
    process = psutil.Process(os.getpid())
    print("END?! memory percent of the process",process.memory_percent())

#'''