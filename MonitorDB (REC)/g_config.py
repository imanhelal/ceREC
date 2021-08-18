'''
This code is copyrighted to Iman Helal @2016-2018 Research work
Information System Department, Faculty of computers and Information System
Cairo University, Egypt
'''
import datetime
# from monitorDB import MyDBConn
# from MyDBConn import MyDBConn

import sqlite3
import psutil
import os




'''################################################################
############# The one used with required results
'''
class MyDBConn2(object):
    _instance = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = object.__new__(cls)
            
            try:
                print('connecting to database 2 ...')
#                 cls._instance._db_connection = sqlite3.connect('sepsis-threshold-0-v4.db', check_same_thread = False);#, isolation_level=None) # for testing
#                cls._instance._db_connection = sqlite3.connect('sepsis-thr30-v6-sec-lag30.db', check_same_thread = False, isolation_level=None) # for running

                cls._instance._db_connection = sqlite3.connect('CoSeLoG_latency_mem2\CoSeLoG_day_lag50_latency_mem_run9.db', check_same_thread = False, isolation_level=None) # for running
#                 cls._instance._db_connection = sqlite3.connect('BPI2013Closed_latency_mem2\BPI2013Closed_day_lag50_latency_mem_run6.db', check_same_thread = False, isolation_level=None) # for running
#                 cls._instance._db_connection = sqlite3.connect('Loop1000_latency_mem2\loop1000_sec_lag30_latency_mem_run5.db', check_same_thread = False, isolation_level=None) # for running
#                 cls._instance._db_connection = sqlite3.connect('sepsis-thr30-min-lag30_latency-v1.db', check_same_thread = False, isolation_level=None) # for running
                #conn = sqlite3.connect('file:BPI2013Memory?mode=memory&cache=shared', isolation_level=None) # for running
                cls._instance._db_cursor = cls._instance._db_connection.cursor()#####file:?mode=memory&cache=shared
                                
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
    


'''
################################################################
'''



# A set of global variables that are accessed and/or updated through the application
process = psutil.Process(os.getpid())
start_process_memory = process.memory_info().vms/1024 # process.memory_percent() # memtype="rss"
end_process_memory = process.memory_info().vms/1024 # process.memory_percent() # by default same as start, this should be updated during execution

start_time_program = datetime.datetime.now()
# Regarding input events used in watchdog
observing_filename = "C:/IH/VS2015Projects/SimulatingEvents/SimulatingEvents/bin/Debug/S.csv"
observing_path = "C:/IH/VS2015Projects/SimulatingEvents/SimulatingEvents/bin/Debug"

# Output of labeling mechanism and checking of user constraints.
#dashboard_filename="C:/IH/LiClipseWorkspace/MonitorDB/output/dashboard.csv"

# Preparing for user input heuristics
H=dict()

##############################################################################
# From Behavioral Profile - yet to be filled automatically
StartActivity = []
PossibleStartActivity = []
# To Do: get from Behavior Profile the list of activities and their relationships
# For now, these patterns will be modified manually
# Initial value for each of the defined PredecessorActivities is dict(), i.e. empty set of activityLabels
# Index of Predecessor Activities - defined once to traverse labels of activities
DependencyActivities = dict()
DependencySemantics = dict()
#DependencyActivities instead of PredecessorActivities 
##############################################################################
# Ongoing calculations and addition with each incoming event 
count_events = 0 # serial of each incoming event
serial= 0
isLifeCycle = False
threshold = 0.0
thresholdStr = ' AND trust >='+str(threshold) 
# Dictionary for list of activities and their probabilities, {activity:prob}
# Default count=0 for all activities, default probability=0.0
# Initiated after BP traversal + Ongoing calculations
ActivitiesProbability = dict()  # updated activity probability with each incoming event
ActivityCount = dict()          # updated number of occurrences of each activity
allActivities = []          # list of all activities (updated once model interpreted)

# Index of instances per each activity, based on new assigned caseID
InstancePool = dict()

# Index of Cases and their respective instanceEvents
# Special case with start activity, the event will be added directly to add a new Case 
# It acts as a new Key for the incoming new values (i.e.nodes) in CaseNodes
# Empty dictionary for cases and their corresponding nodes
# E.g. Cases = {1:[node1,node3], 2:[node2], ...}
# add case identifier whenever starting with Start_Activity
CasesNodes = dict()
CaseCount = 0

db = MyDBConn2()
conn = db.getDBInstance()
cSQL = conn.cursor()

