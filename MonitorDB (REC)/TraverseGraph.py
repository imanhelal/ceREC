'''
This code is copyrighted to Iman Helal @2016-2017 Research work
Information System Department, Faculty of computers and Information System
Cairo University, Egypt
'''
import sqlite3

import sys
import math
import csv
from datetime import datetime 
import time 
import g_config
#from collections import namedtuple

class TraverseGraph:

    timestamp = None
    event = ''
    life_cycle = 'completed'
    resource = None
    isLifeCycle = False
#     startActivity = [] #''
    #CaseInstance = namedtuple('CaseInstance', ['id', 'name','timestamp','prob'])
#     readTimestamp = None
    cSQL = None
    conn = None
    init_correlate_timestamp = None
    
#     def __init__(self, sqlStat, connStat,timestamp, event, life_cycle, resource, startActivity,readTS):
    def __init__(self, sqlStat, connStat,timestamp, event, life_cycle, resource, startActivity):
#    def __init__(self, timestamp, event, startActivity):
        #print('==============In TraverseGraph==============')
        g_config.serial = g_config.serial+1
        #print('serial: ',g_config.serial)
        self.timestamp = timestamp
        self.event = event
        self.life_cycle = life_cycle
        self.resource = resource
        self.cSQL = sqlStat
        self.conn = connStat
        self.init_correlate_timestamp = time.time_ns()
#         self.readTimestamp = readTS
        #print("SQL stat ",self.cSQL, " conn ",self.conn) 
#         self.startActivity = startActivity
        #conn = sqlite3.connect('MEIMI.db')
        #cSQL = conn.cursor()
        
        #cSQL.execute("INSERT INTO Events VALUES (?,?,?)",(g_config.serial,event,timestamp))
        #conn.commit()
#        self.addInstanceDB( self.timestamp, self.event)
        self.addInstanceDB( self.timestamp, self.event, self.life_cycle, self.resource,self.init_correlate_timestamp)
        #self.addInstanceDashboard( self.timestamp, self.event, self.life_cycle, self.resource)
        #self.addInstance( self.timestamp, self.event, self.life_cycle, self.resource)
    
################################################################################    
    # Udpate the activity probability with each incoming event
    def updateActivityProbability(self,activity):
        g_config.ActivityCount[activity] = g_config.ActivityCount[activity] +1
        g_config.ActivitiesProbability[activity]=self.trunc(g_config.ActivityCount[activity] / g_config.serial , 4)

################################################################################        
    # Truncate the float number to number of digits after decimal
    def trunc(self, number, digit=4):
        return (math.floor(number * pow(10, digit) + 0.5)) / pow(10, digit)
    
################################################################################        
    # Convert string to timestamp
    def convertS2TS(self, timeString):
        try:
            S2TS=datetime.strptime(timeString,'%Y-%m-%d  %H:%M:%S')
        except ValueError:
            try:
                S2TS=datetime.strptime(timeString,'%Y-%m-%d  %H:%M')
            except ValueError:
                print ("error in parsing datetime from string ")
                print (ValueError)
        return S2TS 
    
################################################################################        
    # Update format to timestamp, to make sure it is in my required format
    def convertTS2S(self, time1):
        try:
            TS2S=time1.strftime('%Y-%m-%d  %H:%M:%S')
        except ValueError:
            try:
                TS2S=time1.strftime('%Y-%m-%d  %H:%M')
            except ValueError:
                print ("error in parsing datetime from timestamp ")
                print (ValueError)
       
        return TS2S 
    
################################################################################        
    # Difference between 2 timestamps in seconds
    def diff2TS(self, ts1, ts2):
        diff = ts2 - ts1
        return diff.total_seconds() #diff.seconds  is misleading


################################################################################
    # calculate given probability of instance for all possible parent instances
    def calculate_percentage(self, calcPool, highestPriority='avg'):
        calcPrecentage = dict()
        noOfCases = len(sum(calcPool.values(), [])) # to get count of cases instances
        otherHeuristic = list(set(sum(calcPool.values(), [])) - set(calcPool[highestPriority]))
        noOfHighestPriority = len(calcPool[highestPriority])
        noOfOtherHeuristic = len(otherHeuristic)
        total = 0.0
        totalNumber = noOfCases * noOfCases
        if(noOfCases == 1):
            totalNumber = 1 
        totalNumber *= 1.0
        if(noOfOtherHeuristic > 0):
            for a in calcPool[highestPriority]:
                calcPrecentage[a] = ((noOfCases + 1.0) / totalNumber)
                total += calcPrecentage[a]
            for m in otherHeuristic:
                calcPrecentage[m] = ((noOfCases - (noOfHighestPriority * 1.0 / noOfOtherHeuristic * 1.0)) / totalNumber) * 1.0
                total += calcPrecentage[m]
        else:
            for a in calcPool[highestPriority]:
                calcPrecentage[a] = ((noOfCases) / totalNumber)
                total += calcPrecentage[a]                    
        
        return calcPrecentage   
    

################################################################################
    # calculate given probability of instance for all possible parent instances
    def calculate_percentage2(self, calcPool, highestPriority='avg'):
        calcPrecentage = dict()
        noOfCases = len(sum(calcPool.values(), [])) # to get count of cases instances
        otherHeuristic = list(set(sum(calcPool.values(), [])) - set(calcPool[highestPriority]))
        noOfHighestPriority = len(calcPool[highestPriority])
        noOfOtherHeuristic = len(otherHeuristic)
        totalNumber = noOfCases * noOfCases * 1.0
        if(noOfOtherHeuristic > 0):
            for a in calcPool[highestPriority]: #instances with average
#                 print('a    =====',a)
                calcPrecentage[a] = ((noOfCases + 1.0) / totalNumber) *100.0
            for m in otherHeuristic: #instances with otherRange--minimum or maximum
#                 print('m    =====',m)
                calcPrecentage[m] = ((noOfCases - (noOfHighestPriority * 1.0 / noOfOtherHeuristic * 1.0)) / totalNumber) * 100.0
        else:
            for a in calcPool[highestPriority]:
#                 print('a-else    =====',a)
                calcPrecentage[a] = ((noOfCases) / totalNumber) *100.0
        
        return calcPrecentage # replacement to calcPool, with the percentage for each average instance OR/AND each other instance   
   
    

################################################################################
################################################################################
    # Add new instance with Dashbard based on the incoming event - Other than the originally start case
#     def addInstanceDB(self, timestamp, event, life_cycle, resource): #before calculating latency using init_correlate_timestamp
    def addInstanceDB(self, timestamp, event, life_cycle, resource,init_correlate_timestamp):
#    def addInstanceDB(self, timestamp, event):
        CaseID = 0
        serial = g_config.serial
        prob = 0.0
        instanceId = '0.0'
        flag=''
        end_correlate_timestamp = None

        #conn = sqlite3.connect('test.db')#, isolation_level=None) # for testing
        ##conn = sqlite3.connect('physical-start.db')#, isolation_level=None) # for testing in paper
#         conn = sqlite3.connect('BPIC2017-v4.db')#, isolation_level=None) # for testing
        #conn = sqlite3.connect('file:BPI2013Memory?mode=memory&cache=shared', isolation_level=None) # for running
        #conn = sqlite3.connect('Loop_1000.db')
            
#         cSQL = conn.cursor()
        ### Needs further checking if it could be one of shared activities with possible start and other
#         print('   event = ',event)

        #if (self.event in g_config.StartActivity) : # Start of new case
        if (self.event in g_config.StartActivity and life_cycle=='started'): # Start of new case in the new scenario
            g_config.isLifeCycle = True   ## to flag the existence of life_cycle values other than completed
            #print("self.isLifeCycle >>>>> ")
            #print(g_config.isLifeCycle)
            self.updateActivityProbability(event)
            g_config.CaseCount = g_config.CaseCount + 1
            CaseID = g_config.CaseCount  
            #print(CaseID)
            instanceId = str(CaseID)+'.'+str(serial)
            #prob = 1.0 # i.e. 100% correct
            #trust=prob*100.0
            trust = 100.0
            end_correlate_timestamp = time.time_ns()
            # to add new case with the inserted instance event, event name, its probability + latency calculations (init/end_correlate_timestamps)
            self.cSQL.execute("INSERT INTO CorrelatedEvents "
                         "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource,init_correlate_timestamp,end_correlate_timestamp) "
                         "VALUES (?,?,?,?,?,?,?,?,?,?);",
                         (instanceId,timestamp,serial,trust,CaseID,event,life_cycle,resource,init_correlate_timestamp,end_correlate_timestamp))

#            This is the previously used query without latency calculations
#            # to add new case with the inserted instance event, event name, its probability
#             self.cSQL.execute("INSERT INTO CorrelatedEvents "
#                          "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource) "
#                          "VALUES (?,?,?,?,?,?,?,?);",
#                          (instanceId,timestamp,serial,trust,CaseID,event,life_cycle,resource))

#             self.cSQL.execute("INSERT INTO CorrelatedEvents "
#                          "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource,read_timestamp) "
#                          "VALUES (?,?,?,?,?,?,?,?,?);",
#                          (instanceId,timestamp,serial,trust,CaseID,event,life_cycle,resource,self.readTimestamp))

#             cSQL.execute("INSERT INTO CorrelatedEvents "
#                          "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity) "
#                          "VALUES (?,?,?,?,?,?);",
#                          (instanceId,timestamp,serial,trust,CaseID,event))
            
        # the case were there is only 'completed' life_cycle value (i.e. the old scenario of code)
        elif (self.event in g_config.StartActivity and life_cycle=='completed' and g_config.isLifeCycle == False ): # Start of new case the old way
            self.updateActivityProbability(event)
            g_config.CaseCount = g_config.CaseCount + 1
            CaseID = g_config.CaseCount  
            #print(CaseID)
            instanceId = str(CaseID)+'.'+str(serial)
            #prob = 1.0 # i.e. 100% correct
            #trust=prob*100.0
            trust = 100.0
            end_correlate_timestamp = time.time_ns()
            # to add new case with the inserted instance event, event name, its probability
            self.cSQL.execute("INSERT INTO CorrelatedEvents "
                         "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource,init_correlate_timestamp,end_correlate_timestamp) "
                         "VALUES (?,?,?,?,?,?,?,?,?,?);",
                         (instanceId,timestamp,serial,trust,CaseID,event,life_cycle,resource,init_correlate_timestamp,end_correlate_timestamp))
            
#            This is the previously used query without latency calculations
#             # to add new case with the inserted instance event, event name, its probability
#             self.cSQL.execute("INSERT INTO CorrelatedEvents "
#                          "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource) "
#                          "VALUES (?,?,?,?,?,?,?,?);",
#                          (instanceId,timestamp,serial,trust,CaseID,event,life_cycle,resource))
             
#             self.cSQL.execute("INSERT INTO CorrelatedEvents "
#                          "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource,read_timestamp) "
#                          "VALUES (?,?,?,?,?,?,?,?,?);",
#                          (instanceId,timestamp,serial,trust,CaseID,event,life_cycle,resource,self.readTimestamp))
#             cSQL.execute("INSERT INTO CorrelatedEvents "
#                          "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity) "
#                          "VALUES (?,?,?,?,?,?);",
#                          (instanceId,timestamp,serial,trust,CaseID,event))
            

             
        else: 
            
            # To assign to an old case even if it was completed status of a start event.
            # Get all labels of parent activity to incoming event Labels = dict()
            #print("life_cycle = "+life_cycle)
#             calcPool = self.checkModelHeuristics(event,timestamp)
#             calcPool = self.checkModelHeuristics2(event,timestamp,life_cycle,resource)
            calcPool = self.checkModelHeuristics3(event,timestamp,life_cycle,resource)
#             print('\t$ final calcPool of event ',event,' are: ',calcPool)

            CaseIDs = self.calculate_percentage2(calcPool, 'avg')
#             print('\t$ final caseIDs are ---- ',CaseIDs)
            if len(CaseIDs) > 0:    
                tempCaseInstances =dict()
                for instance in CaseIDs:
#                     print('instance in caseIDs is ',instance)
                    CaseID = instance[1]#changed position instead of 3
                    prob = float(CaseIDs[instance])
                    
                    # put in a temporary dict for CaseID and list of their instanceIds
                    if tempCaseInstances.get(CaseID):
                        tempCaseInstances[CaseID] = tempCaseInstances[CaseID]+prob
                    else:
                        tempCaseInstances[CaseID]=prob
                     
                # need to be updated based on CaseID (gather together) sum probability of instanceIds
                for tempCI in tempCaseInstances:
                    commulativeProb = tempCaseInstances[tempCI]
                    instanceId = str(tempCI)+'.'+str(serial)
                    trust=commulativeProb #*100.0
                    
#                     cSQL.execute("INSERT INTO CorrelatedEvents (instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity)  VALUES (?,?,?,?,?,?);",
#                       (instanceId,timestamp,serial,trust,tempCI,event))
#                     self.cSQL.execute("INSERT INTO CorrelatedEvents (instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource,read_timestamp)  VALUES (?,?,?,?,?,?,?,?,?);",
#                       (instanceId,timestamp,serial,trust,tempCI,event,life_cycle,resource,self.readTimestamp))
                    end_correlate_timestamp = time.time_ns()
                    self.cSQL.execute("INSERT INTO CorrelatedEvents (instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource,init_correlate_timestamp,end_correlate_timestamp)  VALUES (?,?,?,?,?,?,?,?,?,?);",
                      (instanceId,timestamp,serial,trust,tempCI,event,life_cycle,resource,init_correlate_timestamp,end_correlate_timestamp))

#                    #### Original execute query before calculate latency
#                     self.cSQL.execute("INSERT INTO CorrelatedEvents (instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity,life_cycle,event_resource)  VALUES (?,?,?,?,?,?,?,?);",
#                       (instanceId,timestamp,serial,trust,tempCI,event,life_cycle,resource))                    
                
                ###tempCaseInstances.clear()
                    ###conn.commit()    
            else: #if len(CaseIDs) == 0: # cannot be assigned to a case
#                 print ('*',timestamp,"; ",event,"is unassigned with unknown percentage")
                flag='!'
#                 instanceId = str(CaseID)+'.'+str(serial)
                instanceId = '0.'+str(serial)
#                 self.cSQL.execute("INSERT INTO CorrelatedEvents (instanceID,inst_timestamp,event_serial,inst_activity,life_cycle,event_resource,warning,read_timestamp) VALUES (?,?,?,?,?,?,?,?);",
#                   (instanceId,timestamp,serial,event,life_cycle,resource,flag,self.readTimestamp))
                end_correlate_timestamp = time.time_ns()
                self.cSQL.execute("INSERT INTO CorrelatedEvents (instanceID,inst_timestamp,event_serial,inst_activity,life_cycle,event_resource,warning,init_correlate_timestamp,end_correlate_timestamp) VALUES (?,?,?,?,?,?,?,?,?);",
                  (instanceId,timestamp,serial,event,life_cycle,resource,flag,init_correlate_timestamp,end_correlate_timestamp))

#                 #### Original execute query before calculate latency
#                 self.cSQL.execute("INSERT INTO CorrelatedEvents (instanceID,inst_timestamp,event_serial,inst_activity,life_cycle,event_resource,warning) VALUES (?,?,?,?,?,?,?);",
#                   (instanceId,timestamp,serial,event,life_cycle,resource,flag))

                
                ###conn.commit()
            
            ###calcPool.clear()
#         if CaseID == 0: # cannot be assigned to a case
#             # To assign to possible new case
#             if(self.event in g_config.PossibleStartActivity):# Start of new case
#                 self.updateActivityProbability(event)
#                 g_config.CaseCount = g_config.CaseCount + 1
#                 CaseID = g_config.CaseCount  
#                 print('>',CaseID)
#                 instanceId = str(CaseID)+'.'+str(serial)
#                 #prob = 1.0 # i.e. 100% correct
#                 #trust=prob*100.0
#                 trust = 100.0
#                 # to add new case with the inserted instance event, event name, its probability
#                 cSQL.execute("INSERT INTO CorrelatedEvents "
#                              "(instanceID,inst_timestamp,event_serial,trust,inst_caseID,inst_activity) "
#                              "VALUES (?,?,?,?,?,?);",
#                              (instanceId,timestamp,serial,trust,CaseID,event))
#             
#             else:
#                 #print ('*',timestamp,"; ",event,"is unassigned with unknown percentage")
#                 flag='!'
#                 instanceId = str(CaseID)+'.'+str(serial)
#                 cSQL.execute("INSERT INTO CorrelatedEvents (instanceID,inst_timestamp,event_serial,inst_activity,warning) VALUES (?,?,?,?,?);",
#                   (instanceId,timestamp,serial,event,flag))
#                 ###conn.commit()
        self.conn.commit()
################################################################################

################################################################################
################################################################################
################################################################################

################################################################################
################################################################################
################################################################################
################################################################################
################################################################################
    # From Thesis version + modified DB connection
    # This will also check the model logic 
    # i.e. is instance a possible parent for the incoming event?
    def checkModelHeuristics(self,event,ts):
        
        avgArr = []
        other=[]
        metadataTime = g_config.H.get(event)
        avg=metadataTime[2]  # rounded up
        strQuery= metadataTime[3]
        
        ###print("\n>Event ",event," timestamp ",ts," minR: ",minR," maxR: ",maxR)
        
#         LabelDependents = g_config.DependencyActivities[event]
#         g_config.DependencySemantics[A] = [xorEntries,loopEntries,andEntries,andNonLoopEntries,andLoopEntries, xorD, inLoop, inD]
        actSemantics =  g_config.DependencySemantics[event]
        xorEntries = actSemantics[0]
        loopEntries = actSemantics[1]
        andEntries = actSemantics[2]
#         andNonLoopEntries = actSemantics[3]
        andLoopEntries = actSemantics[4]
        xorD = actSemantics[5]
        loopD = actSemantics[6]
        inD = actSemantics[7]
        
        conn = sqlite3.connect('test.db')#, isolation_level=None) # for testing
        #conn = sqlite3.connect('file:BPI2013Memory?mode=memory&cache=shared', isolation_level=None) # for running
        cSQL = conn.cursor()
        
        
        
        # these labels need to be analyzed for correct model logic
#         if len(LabelDependents ) == 1: # sequence, AND cases
        if len(xorEntries) == 1 and len(loopEntries)==0 and len(andEntries)==0:
            SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity in "+xorD+" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
#             SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity = '"+xorEntries[0]+"' AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
            ####print('SQL (Sequence): ',SQL)
            self.cSQL.execute(SQL)
            for row in self.cSQL.fetchall():
                diff=row[5]
                if(diff==avg):                
                    avgArr.append(row)
                else:
                    other.append(row)
            
        elif len(andEntries) >= 1 and len(xorEntries)==0 and len(loopEntries)==0:
            #print('\t2-Label Dependents of \'',event,'\' are AND: ',Dependents )
            # if new length > 1 --> AND case
            # get all the instances in these label
            # need to check all are available in same case id and return the latest of all of them
#             inD = "("+','.join("'"+d+"'" for d in andEntries)+")"
            
            if andLoopEntries: # possible loop of the entries - do not remove cases with events with trust 100%
                SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' ) ORDER BY inst_caseID ;"
#                 print('loop checked in AND')
            else:# no loops of the entries - remove cases with events with trust 100%
                SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 ) ORDER BY inst_caseID ;"
            
#               print('loop not checked ')
            ####print('SQL (AND): ',SQL)
            self.cSQL.execute(SQL)
            for row in self.cSQL.fetchall():
                diff = row[5]
                if(diff==avg):                
                    avgArr.append(row)
                else:
                    other.append(row)
                
        else :     # xor, loop cases
                    
#             loopEntries.extend(andLoopEntries)
#             xorEntries.extend(andNonLoopEntries)
#             print('   loopEntries ',loopEntries,' == xorEntries ',xorEntries)
            if len(loopEntries)>0 and len(xorEntries)==0: # there exists at least one loop entry
                
#                 inLoop =  "("+','.join("'"+d+"'" for d in loopEntries)+")" 
                SQL="SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" AND inst_caseID !=0  AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"
                #print('check query for ',event)
                ####print('SQL (Loop) ',SQL)
                '''
                SQL=("SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS "
                     " FROM ( SELECT instanceID, MAX(inst_timestamp) AS maxTS, inst_activity, inst_caseID, trust"
                     " FROM CorrelatedEvents WHERE inst_activity in "+inLoop+" AND inst_caseID !=0  AND inst_caseID NOT IN "
                     " ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID "
                     " FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID "
                     " FROM CorrelatedEvents WHERE  inst_caseID in ( SELECT inst_caseID FROM CorrelatedEvents  "
                     " WHERE inst_activity in "+inLoop+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) ) "
                     " WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) ) "
                     " GROUP BY inst_caseID ) WHERE diffTS >="+minR+" AND diffTS <= "+maxR+" "
                     " ORDER BY inst_caseID ;")
                '''
                ###print('SQL (LOOP): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff = row[5]
                    if(diff==avg):                
                        avgArr.append(row)
                    else:
                        other.append(row)
                
            elif len(xorEntries)>0 and len(loopEntries)==0:# there exists at least one xor entry
                # Same as sequence checking
#                 xorD = "("+','.join("'"+d+"'" for d in xorEntries)+")"
                
                SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity IN "+xorD+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 )  ORDER BY inst_caseID ;"
                
                ####print('SQL (XOR): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff = row[5]
                    if(diff==avg):                
                        avgArr.append(row)
                    else:
                        other.append(row)
            
            else: # Both Loop and XOR entries are available
                
#                 xorD =  "("+' OR '.join("inst_activity = '"+d+"' " for d in xorEntries)+" )"    
#                 loopD = "("+' OR '.join("inst_activity = '"+d+"' " for d in loopEntries)+" )"  
                #orStatement = ' OR '.join("inst_activity = '"+d+"' " for d in loopEntries)
                
                SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents WHERE inst_caseID !=0  AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0  AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
                
                ####print('SQL (XOR+LOOP): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff = row[5]
                    if(diff==avg):                
                        avgArr.append(row)
                    else:
                        other.append(row)
        #return possibleInstances
        calcPool = {'avg':avgArr, 'other':other}
        #print('Related parent instances are = ',calcPool)
        return calcPool
################################################################################









################################################################################
################################################################################
################################################################################
################################################################################
    # This will also check the model logic 
    # i.e. is instance a possible parent for the incoming event?
    def checkModelHeuristics2(self,event,ts,life_cycle,resource):
        
        avgArr = []
        other=[]
        metadataTime = g_config.H.get(event)
        avg=metadataTime[2]  # rounded up
        strQuery= metadataTime[3]
        
        ###print("\n>Event ",event," timestamp ",ts," minR: ",minR," maxR: ",maxR)
        
#         LabelDependents = g_config.DependencyActivities[event]
#         g_config.DependencySemantics[A] = [xorEntries,loopEntries,andEntries,andNonLoopEntries,andLoopEntries, xorD, inLoop, inD]
        actSemantics =  g_config.DependencySemantics[event]
        xorEntries = actSemantics[0]
        loopEntries = actSemantics[1]
        andEntries = actSemantics[2]
#         andNonLoopEntries = actSemantics[3]
        andLoopEntries = actSemantics[4]
        xorD = actSemantics[5]
        loopD = actSemantics[6]
        inD = actSemantics[7]
        
        #conn = sqlite3.connect('test.db')#, isolation_level=None) # for testing
        ##conn = sqlite3.connect('physical-start.db')#, isolation_level=None) # for testing in paper
#         conn = sqlite3.connect('BPIC2017-v4.db')#, isolation_level=None) # for testing
        #conn = sqlite3.connect('file:BPI2013Memory?mode=memory&cache=shared', isolation_level=None) # for running
#         cSQL = conn.cursor()
        
        
        
        # these labels need to be analyzed for correct model logic
#         if len(LabelDependents ) == 1: # sequence, AND cases
        #==============================
        if g_config.isLifeCycle:                
            #The new scenarios, in case there exists 'started' and 'completed'
            #'completed': heuristics are checked and the time difference is needed in filtering the query. 
            ##    (trust is calculated based on execution heuristics)
            #'started': model dependencies are checked and the time difference is considered waiting time and ignored in the filtering process.
            ##    (trust is calculated based on number of distribution over cases), there are no restrictions on  waiting time.

            if (life_cycle == 'completed'): # search for started
                # special case for start activities or any other activity ##self.event in g_config.StartActivity
                # get the recent event occurrence, i.e. successive occurrence of start and complete for the same event with heuristics range 
                SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource  FROM CorrelatedEvents WHERE "+strQuery +g_config.thresholdStr+" AND inst_activity = '"+event+"' AND event_resource = '"+resource+"' AND life_cycle like 'start%' AND inst_caseID IN (SELECT DISTINCT inst_caseID FROM CorrelatedEvents WHERE inst_timestamp IN (SELECT  inst_timestamp FROM CorrelatedEvents WHERE inst_activity = '"+event+"' AND life_cycle like 'start%' ORDER BY inst_timestamp DESC LIMIT 1)) AND inst_timestamp IN (SELECT  inst_timestamp FROM CorrelatedEvents WHERE inst_activity = '"+event+"' AND life_cycle like 'start%' ORDER BY inst_timestamp DESC LIMIT 1);"
                #Incorrect results
                #SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND life_cycle like 'start%' AND inst_activity = '"+event+"' AND event_resource = '"+resource+"' AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents  WHERE inst_activity = '"+event+"' AND life_cycle like 'complete%') ORDER BY inst_caseID ;"
#                 print('SQL (*Completed*): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff=row[5]
                    if(diff==avg):                
                        avgArr.append(row)
                    else:
                        other.append(row)
                        
            elif (len(xorEntries) == 1 and len(loopEntries)==0 and len(andEntries)==0) : # search for completed
                # in case of life_cycle='started', only check for the model dependencies, no need for heuristics constraints
                # Since no execution heuristics are checked, so all results have the same probability of occurrence (set as avg)
                SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'complete%' AND inst_activity in "+xorD+ g_config.thresholdStr +" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
    #            # The one used in Thesis
    #             SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity in "+xorD+" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
    
    #             SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity = '"+xorEntries[0]+"' AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
#                 print('SQL (*Sequence*): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    #diff=row[5]
                    #if(diff==avg):                
                    avgArr.append(row)
                    #else:
                        #other.append(row)
                
            elif len(andEntries) >= 1 and len(xorEntries)==0 and len(loopEntries)==0:
                #print('\t2-Label Dependents of \'',event,'\' are AND: ',Dependents )
                # if new length > 1 --> AND case
                # get all the instances in these label
                # need to check all are available in same case id and return the latest of all of them
    #             inD = "("+','.join("'"+d+"'" for d in andEntries)+")"
                
                if andLoopEntries: # possible loop of the entries - do not remove cases with events with trust 100%
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND life_cycle like 'start%' AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND life_cycle like 'start%' ) ORDER BY inst_caseID ;"
                    SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'complete%' AND inst_activity IN "+inD + g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND life_cycle like 'complete%' ) ORDER BY inst_caseID ;"
    #            # The one used in Thesis                
    #                 SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' ) ORDER BY inst_caseID ;"
    
                    #print('loop checked in AND')
                else:# no loops of the entries - remove cases with events with trust 100%
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND life_cycle like 'start%' AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'start%' ) ORDER BY inst_caseID ;"
                    SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'complete%' AND inst_activity IN "+inD + g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'complete%' ) ORDER BY inst_caseID ;"
    #            # The one used in Thesis                
    #                 SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 ) ORDER BY inst_caseID ;"
                
                    #print('loop not checked ')
#                 print('SQL (*AND*): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    #diff = row[5]
                    #if(diff==avg):                
                    avgArr.append(row)
                    #else:
                        #other.append(row)
                    
            else :     # xor, loop cases
                        
    #             loopEntries.extend(andLoopEntries)
    #             xorEntries.extend(andNonLoopEntries)
    #             print('   loopEntries ',loopEntries,' == xorEntries ',xorEntries)
                if len(loopEntries)>0 and len(xorEntries)==0: # there exists at least one loop entry
                    
    #                 inLoop =  "("+','.join("'"+d+"'" for d in loopEntries)+")" 
                    #SQL="SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS, life_cycle,event_resource  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust, life_cycle,event_resource FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" AND inst_caseID !=0 AND life_cycle like 'complete%' AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID, life_cycle,event_resource  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID, life_cycle,event_resource  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  AND life_cycle like 'complete%' GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"
                    SQL="SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust, life_cycle,event_resource FROM CorrelatedEvents WHERE inst_activity IN "+loopD+ g_config.thresholdStr+" AND inst_caseID !=0 AND life_cycle like 'complete%' AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID, life_cycle,event_resource  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID, life_cycle,event_resource  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  AND life_cycle like 'complete%' GROUP BY inst_caseID Having count(instanceID)=1 ) )  ORDER BY inst_caseID ;"
    #                # The one used in Thesis
    #                 SQL="SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" AND inst_caseID !=0  AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"
    
                    #print('check query for ',event)
                    ####print('SQL (Loop) ',SQL)
                    '''
                    SQL=("SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS "
                         " FROM ( SELECT instanceID, MAX(inst_timestamp) AS maxTS, inst_activity, inst_caseID, trust"
                         " FROM CorrelatedEvents WHERE inst_activity in "+inLoop+" AND inst_caseID !=0  AND inst_caseID NOT IN "
                         " ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID "
                         " FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID "
                         " FROM CorrelatedEvents WHERE  inst_caseID in ( SELECT inst_caseID FROM CorrelatedEvents  "
                         " WHERE inst_activity in "+inLoop+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) ) "
                         " WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) ) "
                         " GROUP BY inst_caseID ) WHERE diffTS >="+minR+" AND diffTS <= "+maxR+" "
                         " ORDER BY inst_caseID ;")
                    '''
#                     print('SQL (*LOOP*): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        #diff = row[5]
                        #if(diff==avg):                
                        avgArr.append(row)
                        #else:
                            #other.append(row)
                    
                elif len(xorEntries)>0 and len(loopEntries)==0:# there exists at least one xor entry
                    # Same as sequence checking
    #                 xorD = "("+','.join("'"+d+"'" for d in xorEntries)+")"
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND life_cycle like 'start%' AND inst_activity IN "+xorD+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'start%' )  ORDER BY inst_caseID ;"
                    SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource FROM CorrelatedEvents WHERE  inst_caseID !=0 AND life_cycle like 'complete%' AND inst_activity IN "+xorD+ g_config.thresholdStr+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'complete%' )  ORDER BY inst_caseID ;"
    #                # The one used in Thesis
    #                 SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity IN "+xorD+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 )  ORDER BY inst_caseID ;"
                    
#                     print('SQL (*XOR*): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        #diff = row[5]
                        #if(diff==avg):                
                        avgArr.append(row)
                        #else:
                            #other.append(row)
                
                else: # Both Loop and XOR entries are available
                    
    #                 xorD =  "("+' OR '.join("inst_activity = '"+d+"' " for d in xorEntries)+" )"    
    #                 loopD = "("+' OR '.join("inst_activity = '"+d+"' " for d in loopEntries)+" )"  
                    #orStatement = ' OR '.join("inst_activity = '"+d+"' " for d in loopEntries)
                    
                    #SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'start%'  AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'start%' AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
                    SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'complete%'   "+g_config.thresholdStr+" AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'complete%' AND inst_timestamp > ( SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID) AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) ORDER BY inst_caseID ;"
    #                # The one used in Thesis
    #                 SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents WHERE inst_caseID !=0  AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0  AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
                    
#                     print('SQL (*XOR+LOOP*): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        #diff = row[5]
                        #if(diff==avg):                
                        avgArr.append(row)
                        #else:
                            #other.append(row)
        #==============================
        else:        ######### DO NOT CHANGE - OLD SCENARIOS FOR COMPLETED CASES
            #The old scenarios, in case there exists only 'completed'
            if (len(xorEntries) == 1 and len(loopEntries)==0 and len(andEntries)==0) :
                SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity in "+xorD +g_config.thresholdStr+" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
                # The one used in Thesis
                #SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity in "+xorD+" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
    
    #             SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity = '"+xorEntries[0]+"' AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
#                 print('SQL (Sequence): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff=row[5]
                    if(diff==avg):                
                        avgArr.append(row)
                    else:
                        other.append(row)
                
            elif len(andEntries) >= 1 and len(xorEntries)==0 and len(loopEntries)==0:
                #print('\t2-Label Dependents of \'',event,'\' are AND: ',Dependents )
                # if new length > 1 --> AND case
                # get all the instances in these label
                # need to check all are available in same case id and return the latest of all of them
    #             inD = "("+','.join("'"+d+"'" for d in andEntries)+")"
                
                if andLoopEntries: # possible loop of the entries - do not remove cases with events with trust 100%
                    SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD +g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' ) ORDER BY inst_caseID ;"
                    # The one used in Thesis                
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' ) ORDER BY inst_caseID ;"
    
                    #print('loop checked in AND')
                else:# no loops of the entries - remove cases with events with trust 100%
                    SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD +g_config.thresholdStr +" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 ) ORDER BY inst_caseID ;"
                    # The one used in Thesis                
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 ) ORDER BY inst_caseID ;"
                
                    #print('loop not checked ')
#                 print('SQL (AND): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff = row[5]
                    if(diff==avg):                
                        avgArr.append(row)
                    else:
                        other.append(row)
                    
            else :     # xor, loop cases
                        
    #             loopEntries.extend(andLoopEntries)
    #             xorEntries.extend(andNonLoopEntries)
    #             print('   loopEntries ',loopEntries,' == xorEntries ',xorEntries)
                if len(loopEntries)>0 and len(xorEntries)==0: # there exists at least one loop entry
                    
    #                 inLoop =  "("+','.join("'"+d+"'" for d in loopEntries)+")" 
    
                    SQL="SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS, life_cycle,event_resource  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust, life_cycle,event_resource FROM CorrelatedEvents WHERE inst_activity IN "+loopD +g_config.thresholdStr +" AND inst_caseID !=0 AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID, life_cycle,event_resource  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID, life_cycle,event_resource  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"
                    # The one used in Thesis
                    #SQL="SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" AND inst_caseID !=0  AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"
    
                    #print('check query for ',event)
                    ####print('SQL (Loop) ',SQL)
                    '''
                    SQL=("SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS "
                         " FROM ( SELECT instanceID, MAX(inst_timestamp) AS maxTS, inst_activity, inst_caseID, trust"
                         " FROM CorrelatedEvents WHERE inst_activity in "+inLoop+" AND inst_caseID !=0  AND inst_caseID NOT IN "
                         " ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID "
                         " FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID "
                         " FROM CorrelatedEvents WHERE  inst_caseID in ( SELECT inst_caseID FROM CorrelatedEvents  "
                         " WHERE inst_activity in "+inLoop+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) ) "
                         " WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) ) "
                         " GROUP BY inst_caseID ) WHERE diffTS >="+minR+" AND diffTS <= "+maxR+" "
                         " ORDER BY inst_caseID ;")
                    '''
#                     print('SQL (LOOP): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        diff = row[5]
                        if(diff==avg):                
                            avgArr.append(row)
                        else:
                            other.append(row)
                    
                elif len(xorEntries)>0 and len(loopEntries)==0:# there exists at least one xor entry
                    # Same as sequence checking
    #                 xorD = "("+','.join("'"+d+"'" for d in xorEntries)+")"
                    
                    SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity IN "+xorD +g_config.thresholdStr + " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 )  ORDER BY inst_caseID ;"
                    # The one used in Thesis
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity IN "+xorD+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 )  ORDER BY inst_caseID ;"
                    
#                     print('SQL (XOR): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        diff = row[5]
                        if(diff==avg):                
                            avgArr.append(row)
                        else:
                            other.append(row)
                
                else: # Both Loop and XOR entries are available
                    
    #                 xorD =  "("+' OR '.join("inst_activity = '"+d+"' " for d in xorEntries)+" )"    
    #                 loopD = "("+' OR '.join("inst_activity = '"+d+"' " for d in loopEntries)+" )"  
                    #orStatement = ' OR '.join("inst_activity = '"+d+"' " for d in loopEntries)
                    
                    
                    SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource  FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents WHERE inst_caseID !=0   "+g_config.thresholdStr+" AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0  AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
                    # The one used in Thesis
                    #SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents WHERE inst_caseID !=0  AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0  AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
                    
#                     print('SQL (XOR+LOOP): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        diff = row[5]
                        if(diff==avg):                
                            avgArr.append(row)
                        else:
                            other.append(row)

        #==============================
        
        #return possibleInstances
        calcPool = {'avg':avgArr, 'other':other}
        #print('Related parent instances are = ',calcPool)
        return calcPool
################################################################################

################################################################################
################################################################################
################################################################################

################################################################################
    # This will also check the model logic 
    # i.e. is instance a possible parent for the incoming event?
    def checkModelHeuristics3(self,event,ts,life_cycle,resource):
        
        avgArr = []
        other=[]
        metadataTime = g_config.H.get(event)
        avg=metadataTime[2]  # rounded up
        strQuery= metadataTime[3]
        
        ###print("\n>Event ",event," timestamp ",ts," minR: ",minR," maxR: ",maxR)
        
#         LabelDependents = g_config.DependencyActivities[event]
#         g_config.DependencySemantics[A] = [xorEntries,loopEntries,andEntries,andNonLoopEntries,andLoopEntries, xorD, inLoop, inD]
        actSemantics =  g_config.DependencySemantics[event]
        xorEntries = actSemantics[0]
        loopEntries = actSemantics[1]
        andEntries = actSemantics[2]
        andLoopEntries = actSemantics[4]
        xorD = actSemantics[5]
        loopD = actSemantics[6]
        inD = actSemantics[7]
        
        #conn = sqlite3.connect('test.db')#, isolation_level=None) # for testing
        ##conn = sqlite3.connect('physical-start.db')#, isolation_level=None) # for testing in paper
#         conn = sqlite3.connect('BPIC2017-v4.db')#, isolation_level=None) # for testing
        #conn = sqlite3.connect('file:BPI2013Memory?mode=memory&cache=shared', isolation_level=None) # for running
#         cSQL = conn.cursor()
        
        
        
        # these labels need to be analyzed for correct model logic
#         if len(LabelDependents ) == 1: # sequence, AND cases
        #==============================
        if g_config.isLifeCycle:                
            #The new scenarios, in case there exists 'started' and 'completed'
            #'completed': heuristics are checked and the time difference is needed in filtering the query. 
            ##    (trust is calculated based on execution heuristics)
            #'started': model dependencies are checked and the time difference is considered waiting time and ignored in the filtering process.
            ##    (trust is calculated based on number of distribution over cases), there are no restrictions on  waiting time.

            if (life_cycle == 'completed'): # search for started
                # special case for start activities or any other activity ##self.event in g_config.StartActivity
                # get the recent event occurrence, i.e. successive occurrence of start and complete for the same event with heuristics range 
                SQL = "SELECT instanceID, inst_caseID, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, inst_timestamp FROM CorrelatedEvents WHERE "+strQuery +g_config.thresholdStr+" AND inst_activity = '"+event+"' AND event_resource = '"+resource+"' AND life_cycle like 'start%' AND inst_caseID IN (SELECT DISTINCT inst_caseID FROM CorrelatedEvents WHERE inst_timestamp IN (SELECT  inst_timestamp FROM CorrelatedEvents WHERE inst_activity = '"+event+"' AND life_cycle = 'started' ORDER BY inst_timestamp DESC LIMIT 1)) AND inst_timestamp IN (SELECT  inst_timestamp FROM CorrelatedEvents WHERE inst_activity = '"+event+"' AND life_cycle = 'started' ORDER BY inst_timestamp DESC LIMIT 1);"
#                 SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource  FROM CorrelatedEvents WHERE "+strQuery +g_config.thresholdStr+" AND inst_activity = '"+event+"' AND event_resource = '"+resource+"' AND life_cycle like 'start%' AND inst_caseID IN (SELECT DISTINCT inst_caseID FROM CorrelatedEvents WHERE inst_timestamp IN (SELECT  inst_timestamp FROM CorrelatedEvents WHERE inst_activity = '"+event+"' AND life_cycle like 'start%' ORDER BY inst_timestamp DESC LIMIT 1)) AND inst_timestamp IN (SELECT  inst_timestamp FROM CorrelatedEvents WHERE inst_activity = '"+event+"' AND life_cycle like 'start%' ORDER BY inst_timestamp DESC LIMIT 1);"
#                 print('SQL (*Completed*): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff=row[2]
                    if(diff==avg):                
                        avgArr.append(row[0:1])
                    else:
                        other.append(row[0:1])
                        
            elif (len(xorEntries) == 1 and len(loopEntries)==0 and len(andEntries)==0) : # search for completed
                # in case of life_cycle='started', only check for the model dependencies, no need for heuristics constraints
                # Since no execution heuristics are checked, so all results have the same probability of occurrence (set as avg)
                SQL = "SELECT instanceID, inst_caseID FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle = 'completed' AND inst_activity in "+xorD+ g_config.thresholdStr +" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
#                 SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'complete%' AND inst_activity in "+xorD+ g_config.thresholdStr +" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"

    #            # The one used in Thesis
    #             SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity in "+xorD+" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
    
#                 print('SQL (*Sequence*): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():           
                    avgArr.append(row)
                    
                
            elif len(andEntries) >= 1 and len(xorEntries)==0 and len(loopEntries)==0:
                #print('\t2-Label Dependents of \'',event,'\' are AND: ',Dependents )
                # if new length > 1 --> AND case
                # get all the instances in these label
                # need to check all are available in same case id and return the latest of all of them
                
                if andLoopEntries: # possible loop of the entries - do not remove cases with events with trust 100%
                    SQL="SELECT instanceID, inst_caseID FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle = 'completed' AND inst_activity IN "+inD + g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND life_cycle = 'completed' ) ORDER BY inst_caseID ;"
#                     SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'complete%' AND inst_activity IN "+inD + g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND life_cycle like 'complete%' ) ORDER BY inst_caseID ;"
    #            # The one used in Thesis                
    #                 SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' ) ORDER BY inst_caseID ;"
    
                    #print('loop checked in AND')
                else:# no loops of the entries - remove cases with events with trust 100%
                    SQL="SELECT instanceID, inst_caseID  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle = 'completed' AND inst_activity IN "+inD + g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle = 'completed' ) ORDER BY inst_caseID ;"
#                     SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'complete%' AND inst_activity IN "+inD + g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'complete%' ) ORDER BY inst_caseID ;"
    #            # The one used in Thesis                
    #                 SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 ) ORDER BY inst_caseID ;"
                
                    #print('loop not checked ')
#                 print('SQL (*AND*): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    avgArr.append(row)
                    
                    
            else :     # xor, loop cases
                        
                if len(loopEntries)>0 and len(xorEntries)==0: # there exists at least one loop entry
                    SQL="SELECT instanceID, inst_caseID   FROM CorrelatedEvents WHERE inst_activity IN "+loopD+ g_config.thresholdStr+" AND inst_caseID !=0 AND life_cycle like 'complete%' AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  AND life_cycle = 'completed' GROUP BY inst_caseID Having count(instanceID)=1 ) )  ORDER BY inst_caseID ;"
#                     SQL="SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust, life_cycle,event_resource FROM CorrelatedEvents WHERE inst_activity IN "+loopD+ g_config.thresholdStr+" AND inst_caseID !=0 AND life_cycle like 'complete%' AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID, life_cycle,event_resource  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID, life_cycle,event_resource  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  AND life_cycle like 'complete%' GROUP BY inst_caseID Having count(instanceID)=1 ) )  ORDER BY inst_caseID ;"
    #                # The one used in Thesis
    #                 SQL="SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" AND inst_caseID !=0  AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"
    
                    #print('check query for ',event)
#                     print('SQL (*LOOP*): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        avgArr.append(row)
                    
                elif len(xorEntries)>0 and len(loopEntries)==0:# there exists at least one xor entry
                    # Same as sequence checking
    
                    SQL="SELECT instanceID, inst_caseID FROM CorrelatedEvents WHERE  inst_caseID !=0 AND life_cycle = 'completed' AND inst_activity IN "+xorD+ g_config.thresholdStr+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle = 'completed' )  ORDER BY inst_caseID ;"
#                     SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource FROM CorrelatedEvents WHERE  inst_caseID !=0 AND life_cycle like 'complete%' AND inst_activity IN "+xorD+ g_config.thresholdStr+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'complete%' )  ORDER BY inst_caseID ;"
    #                # The one used in Thesis
    #                 SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity IN "+xorD+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 )  ORDER BY inst_caseID ;"
                    
#                     print('SQL (*XOR*): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        avgArr.append(row)
                        
                
                else: # Both Loop and XOR entries are available
                                    
                    SQL = "SELECT instanceID, inst_caseID  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle = 'completed'   "+g_config.thresholdStr+" AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle = 'completed' AND inst_timestamp > ( SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID) AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) ORDER BY inst_caseID ;"
#                     SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, life_cycle,event_resource  FROM CorrelatedEvents WHERE inst_caseID !=0 AND life_cycle like 'complete%'   "+g_config.thresholdStr+" AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 AND life_cycle like 'complete%' AND inst_timestamp > ( SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID) AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) ORDER BY inst_caseID ;"
    #                # The one used in Thesis
    #                 SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents WHERE inst_caseID !=0  AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0  AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
                    
#                     print('SQL (*XOR+LOOP*): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        avgArr.append(row)
                    
        #==============================
        else:        ######### DO NOT CHANGE - OLD SCENARIOS FOR COMPLETED CASES
            #The old scenarios, in case there exists only 'completed'
            if (len(xorEntries) == 1 and len(loopEntries)==0 and len(andEntries)==0) :
                SQL = "SELECT instanceID, inst_caseID, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity in "+xorD +g_config.thresholdStr+" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
#                 SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity in "+xorD +g_config.thresholdStr+" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
                # The one used in Thesis
                #SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity in "+xorD+" AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
    
    #             SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust,  (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity = '"+xorEntries[0]+"' AND inst_caseID NOT IN  (SELECT inst_caseID FROM CorrelatedEvents  WHERE inst_activity='"+event+"' AND trust=100.0)  ORDER BY inst_caseID ;"
#                 print('SQL (Sequence): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff=row[2]#changed position instead of 5
                    if(diff==avg):                
                        avgArr.append(row[0:2]) #from start to end-1
                    else:
                        other.append(row[0:2])
                
            elif len(andEntries) >= 1 and len(xorEntries)==0 and len(loopEntries)==0:
                #print('\t2-Label Dependents of \'',event,'\' are AND: ',Dependents )
                # if new length > 1 --> AND case
                # get all the instances in these label
                # need to check all are available in same case id and return the latest of all of them
                
                if andLoopEntries: # possible loop of the entries - do not remove cases with events with trust 100%
                    SQL="SELECT instanceID, inst_caseID, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD +g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' ) ORDER BY inst_caseID ;"
#                     SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD +g_config.thresholdStr+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' ) ORDER BY inst_caseID ;"
                    # The one used in Thesis                
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' ) ORDER BY inst_caseID ;"
    
                    #print('loop checked in AND')
                else:# no loops of the entries - remove cases with events with trust 100%
                    SQL="SELECT instanceID, inst_caseID, diffTS  FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD +g_config.thresholdStr +" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 ) ORDER BY inst_caseID ;"
#                     SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD +g_config.thresholdStr +" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 ) ORDER BY inst_caseID ;"
                    # The one used in Thesis                
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+ " AND inst_caseID !=0 AND inst_activity IN "+inD+" AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 ) ORDER BY inst_caseID ;"
                
                    #print('loop not checked ')
#                 print('SQL (AND): ',SQL)
                self.cSQL.execute(SQL)
                for row in self.cSQL.fetchall():
                    diff = row[2]#changed position instead of 5
                    if(diff==avg):                
                        avgArr.append(row[0:2]) #from start to end-1
                    else:
                        other.append(row[0:2])
                    
            else :     # xor, loop cases
                        
                if len(loopEntries)>0 and len(xorEntries)==0: # there exists at least one loop entry
                    SQL="SELECT instanceID, inst_caseID, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust  FROM CorrelatedEvents WHERE inst_activity IN "+loopD +g_config.thresholdStr +" AND inst_caseID !=0 AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"                    
#                     SQL="SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS, life_cycle,event_resource  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust, life_cycle,event_resource FROM CorrelatedEvents WHERE inst_activity IN "+loopD +g_config.thresholdStr +" AND inst_caseID !=0 AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID, life_cycle,event_resource  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID, life_cycle,event_resource  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"
                    # The one used in Thesis
                    #SQL="SELECT instanceID, maxTS, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',maxTS)) AS diffTS  FROM ( SELECT instanceID, inst_timestamp AS maxTS, inst_activity, inst_caseID, trust FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" AND inst_caseID !=0  AND inst_caseID NOT IN  ( SELECT inst_caseID FROM( SELECT inst_timestamp, inst_caseID, inst_activity, trust, instanceID  FROM(    SELECT distinct inst_caseID , inst_timestamp, inst_activity, trust, instanceID  FROM CorrelatedEvents WHERE  inst_caseID IN ( SELECT inst_caseID FROM CorrelatedEvents   WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID HAVING count(inst_activity)=1 ) )  WHERE inst_activity='"+event+"' AND trust=100  GROUP BY inst_caseID Having count(instanceID)=1 ) )  ) WHERE "+strQuery+"  ORDER BY inst_caseID ;"
    
                    #print('check query for ',event)
#                     print('SQL (LOOP): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        diff = row[2]#changed position instead of 5
                        if(diff==avg):                
                            avgArr.append(row[0:2]) #from start to end-1
                        else:
                            other.append(row[0:2])
                    
                elif len(xorEntries)>0 and len(loopEntries)==0:# there exists at least one xor entry
                    # Same as sequence checking
                    SQL="SELECT instanceID, inst_caseID, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity IN "+xorD +g_config.thresholdStr + " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 )  ORDER BY inst_caseID ;"                    
#                     SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity IN "+xorD +g_config.thresholdStr + " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 )  ORDER BY inst_caseID ;"
                    # The one used in Thesis
                    #SQL="SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS FROM (SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents) AS x WHERE "+strQuery+" AND inst_caseID !=0 AND inst_activity IN "+xorD+ " AND inst_caseID NOT IN (SELECT inst_caseID FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0 )  ORDER BY inst_caseID ;"
                    
#                     print('SQL (XOR): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        diff = row[2]#changed position instead of 5
                        if(diff==avg):                
                            avgArr.append(row[0:2]) #from start to end-1
                        else:
                            other.append(row[0:2])
                
                else: # Both Loop and XOR entries are available
                                        
                    SQL = "SELECT instanceID, inst_caseID, diffTS FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS FROM CorrelatedEvents WHERE inst_caseID !=0   "+g_config.thresholdStr+" AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0  AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
#                     SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS, life_cycle,event_resource  FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS, life_cycle,event_resource FROM CorrelatedEvents WHERE inst_caseID !=0   "+g_config.thresholdStr+" AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0  AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
                    # The one used in Thesis
                    #SQL = "SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, diffTS  FROM ( SELECT instanceID, inst_timestamp, inst_activity, inst_caseID, trust, (STRFTIME('%s','"+ts+"')-STRFTIME('%s',inst_timestamp)) AS diffTS  FROM CorrelatedEvents WHERE inst_caseID !=0  AND ( inst_activity IN "+xorD+" OR ( inst_activity IN "+loopD+"  AND inst_caseID IN (  SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" AND inst_caseID NOT IN ( SELECT inst_caseID  FROM CorrelatedEvents WHERE inst_activity='"+event+"' AND trust=100.0  AND inst_timestamp > (SELECT MAX(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+xorD+" GROUP BY inst_caseID)  AND inst_timestamp < (SELECT MIN(inst_timestamp) FROM CorrelatedEvents WHERE inst_activity IN "+loopD+" GROUP BY inst_caseID)  ) ) ) ) )AS x WHERE "+strQuery+"   ORDER BY inst_caseID ;"
                    
#                     print('SQL (XOR+LOOP): ',SQL)
                    self.cSQL.execute(SQL)
                    for row in self.cSQL.fetchall():
                        diff = row[2]#changed position instead of 5
                        if(diff==avg):                
                            avgArr.append(row[0:2]) #from start to end-1
                        else:
                            other.append(row[0:2])

        #==============================
        
        #return possibleInstances
        calcPool = {'avg':avgArr, 'other':other}
        #print('Related parent instances are = ',calcPool)
        return calcPool
################################################################################







