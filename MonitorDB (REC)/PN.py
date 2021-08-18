'''
This code is copyrighted to Iman Helal @2016-2017 Research work
Information System Department, Faculty of computers and Information System
Cairo University, Egypt
'''
'''
This class is a preparation step to analyze Petri net model for:
- MonitorGraph project
'''

import json
import itertools
from CleanTD import CleanTD
from datetime import datetime

class PetriNet:

    # store places and transitions as dictionary
    # places = {'p1':([],['t0'], ...}
    # this will help in buildPN to jump for required place directly
    edges = []
    places = dict()
    transitions = dict()
    prevTransitions = dict()
    nextTransitions = dict()
    dependenciesTransition = dict()
    G={} #initializing the empty graph (dependents/predecessors)
    reversedG={} #initializing the empty graph (successors)
    
################################################################################        

    def __init__(self):
        
####################################################################
#    # Old CoSeLoG - wrong situation
#     # Final list of dependencies =  {'tau from tree1': [['confirmation of receipt']], 
#                          't07-1 draft intern advice aspect 1': [["t05 print and send confirmation of receipt"]], 
#                          't06 determine necessity of stop advice': [['t09-1 process or receive external advice from party 1'], ['tau from tree4']], 
#                          "t05 print and send confirmation of receipt": [['t04 determine confirmation of receipt']], 
#                          "t15 print document x request unlicensed": [['t14 determine document x request unlicensed'], ['tau from tree7']], 
#                          't08 draft and send request for advice': [['t07-1 draft intern advice aspect 1'], ['tau from tree2']], 
#                          't17 check report y to stop indication': [['t16 report reasons to hold request']], 
#                          'tau from tree3': [['t07-1 draft intern advice aspect 1'], ['tau from tree2']], 
#                          't11 create document x request unlicensed': [['tau from tree1'], ['t10 determine necessity to stop indication']], 
#                          'confirmation of receipt': [], 'tau from tree4': [['t08 draft and send request for advice'], ['tau from tree3']], 
#                          "t20 print report y to stop indication": [['t19 determine report y to stop indication']], 
#                          'tau from tree5': [['tau from tree1'], ['t10 determine necessity to stop indication']], 
#                          't19 determine report y to stop indication': [['t17 check report y to stop indication']], 
#                          't16 report reasons to hold request': [['tau from tree1'], ['t10 determine necessity to stop indication']], 
#                          'tau from tree7': [['t12 check document x request unlicensed'], ['tau from tree6']], 
#                          'tau from tree8': [['t14 determine document x request unlicensed'], ['tau from tree7']], 
#                          'tau from tree2': [["t05 print and send confirmation of receipt"]], 
#                          'tau from tree6': [['t11 create document x request unlicensed']], 
#                          't12 check document x request unlicensed': [['t11 create document x request unlicensed']], 
#                          't09-1 process or receive external advice from party 1': [['t08 draft and send request for advice'], ['tau from tree3']], 
#                          't04 determine confirmation of receipt': [['t02 check confirmation of receipt']], 
#                          't14 determine document x request unlicensed': [['t12 check document x request unlicensed'], ['tau from tree6']], 
#                          't10 determine necessity to stop indication': [['t06 determine necessity of stop advice']], 
#                          't02 check confirmation of receipt': [['confirmation of receipt']]}
#         self.addTransition("Confirmation of receipt")
#         self.addTransition("T02 Check confirmation of receipt")
#         self.addTransition("T04 Determine confirmation of receipt")
#         self.addTransition("T05 print and send confirmation of receipt")
#         self.addTransition("T07-1 Draft intern advice aspect 1")
#         self.addTransition("T08 Draft and send request for advice")
#         self.addTransition("T09-1 Process or receive external advice from party 1")
#         self.addTransition("T06 Determine necessity of stop advice")
#         self.addTransition("T10 Determine necessity to stop indication")
#         self.addTransition("T16 Report reasons to hold request")
#         self.addTransition("T17 Check report Y to stop indication")
#         self.addTransition("T19 Determine report Y to stop indication")
#         self.addTransition("T20 print report Y to stop indication")
#         self.addTransition("T11 Create document X request unlicensed")
#         self.addTransition("T12 Check document X request unlicensed")
#         self.addTransition("T14 Determine document X request unlicensed")
#         self.addTransition("T15 print document X request unlicensed")
#           
#         self.addTransition("tau from tree1")
#         self.addTransition("tau from tree2")
#         self.addTransition("tau from tree3")
#         self.addTransition("tau from tree4")
#         self.addTransition("tau from tree5")
#         self.addTransition("tau from tree6")
#         self.addTransition("tau from tree7")
#         self.addTransition("tau from tree8")
#          
#         self.addPlace('p1')
#         self.addPlace('p2')
#         self.addPlace('p3')
#         self.addPlace('p4')
#         self.addPlace('p5')
#         self.addPlace('p6')
#         self.addPlace('p7')
#         self.addPlace('p8')
#         self.addPlace('p9')
#         self.addPlace('p10')
#         self.addPlace('p11')
#         self.addPlace('p12')
#         self.addPlace('p13')
#         self.addPlace('p14')
#         self.addPlace('p15')
#         self.addPlace('p16')
#         self.addPlace('p17')
#          
#         self.setEdge( 'p1',"Confirmation of receipt")
#         self.setEdge("Confirmation of receipt", 'p2')
#         self.setEdge( 'p2',"tau from tree1")
#         self.setEdge( 'p2',"T02 Check confirmation of receipt")
#           
#         self.setEdge("tau from tree1", 'p10')
#           
#           
#         self.setEdge("T02 Check confirmation of receipt", 'p3')
#         self.setEdge( 'p3',"T04 Determine confirmation of receipt")
#         self.setEdge("T04 Determine confirmation of receipt", 'p4')
#         self.setEdge( 'p4',"T05 print and send confirmation of receipt")
#         self.setEdge("T05 print and send confirmation of receipt", 'p5')
#         self.setEdge( 'p5',"T07-1 Draft intern advice aspect 1")
#         self.setEdge('p5',"tau from tree2")
#         self.setEdge("T07-1 Draft intern advice aspect 1",'p6')
#         self.setEdge("tau from tree2",'p6')
#         self.setEdge( 'p6',"T08 Draft and send request for advice")
#         self.setEdge('p6',"tau from tree3")
#         self.setEdge("T08 Draft and send request for advice", 'p7')
#         self.setEdge("tau from tree3", 'p7')
#         self.setEdge('p7',"T09-1 Process or receive external advice from party 1")
#         self.setEdge('p7',"tau from tree4")
#         self.setEdge("T09-1 Process or receive external advice from party 1", 'p8')
#         self.setEdge("tau from tree4", 'p8')
#         self.setEdge('p8',"T06 Determine necessity of stop advice")
#         self.setEdge("T06 Determine necessity of stop advice", 'p9')
#         self.setEdge('p9',"T10 Determine necessity to stop indication")
#         self.setEdge("T10 Determine necessity to stop indication",'p10')
#           
#         self.setEdge('p10',"T16 Report reasons to hold request")
#         self.setEdge('p10',"T11 Create document X request unlicensed")
#         self.setEdge('p10',"tau from tree5")
#           
#         self.setEdge("tau from tree5",'p17')
#           
#         self.setEdge("T16 Report reasons to hold request",'p11')
#         self.setEdge( 'p11',"T17 Check report Y to stop indication")
#         self.setEdge("T17 Check report Y to stop indication",'p12')
#         self.setEdge( 'p12',"T19 Determine report Y to stop indication")
#         self.setEdge("T19 Determine report Y to stop indication",'p13')
#         self.setEdge( 'p13',"T20 print report Y to stop indication")
#         self.setEdge("T20 print report Y to stop indication", 'p17')
#           
#         self.setEdge("T11 Create document X request unlicensed",'p14')
#         self.setEdge('p14',"T12 Check document X request unlicensed")
#         self.setEdge('p14',"tau from tree6")
#         self.setEdge("T12 Check document X request unlicensed",'p15')
#         self.setEdge("tau from tree6",'p15')
#         self.setEdge( 'p15',"T14 Determine document X request unlicensed")
#         self.setEdge('p15',"tau from tree7")
#         self.setEdge("T14 Determine document X request unlicensed",'p16')
#         self.setEdge("tau from tree7",'p16')
#         self.setEdge('p16',"T15 print document X request unlicensed")
#         self.setEdge('p16',"tau from tree8")
#         self.setEdge("T15 print document X request unlicensed",'p17')
#         self.setEdge("tau from tree8",'p17')
#          
#         self.setToken("p1")    
####################################################################  
    
#########################################################################################################
#           #loop log - corrected -- Good with 8536 events - 1000 cases (used in comparison) 
#             originalDependencies =  {'d': [['a'], ['*e']], 'tau': [['b']], 'h': [['g'], ['f']], 'f': [['tau']], 
#                              'a': [], 'b': [['d']], 'e': [['b']], 'g': [['c']], 'c': [['tau']]}
#             modelDependencies = {'h': [['g'], ['f']], 'f': [['b']], 'e': [['b']], 'g': [['c']], 
#                             'd': [['a'], ['*e']], 'a': [], 'c': [['b']], 'b': [['d']]}
#      
#
#             self.addPlace('p1')
#             self.addPlace('p2')
#             self.addPlace('p3')
#             self.addPlace('p4')
#             self.addPlace('p5')
#             self.addPlace('p6')
#             self.addPlace('p7')
#             self.addPlace('p8')
#                        
#             self.addTransition('a')
#             self.addTransition('b')
#             self.addTransition('c')
#             self.addTransition('d')
#             self.addTransition('e')
#             self.addTransition('f')
#             self.addTransition('g')
#             self.addTransition('h')
#             self.addTransition('tau')
#                        
#                        
#             self.setEdge('p1', 'a')
#             self.setEdge('a', 'p2')
#             self.setEdge('p2', 'd')
#             self.setEdge('d', 'p3')
#             self.setEdge('p3', 'b')
#             self.setEdge('b', 'p4')
#             self.setEdge('p4', 'e')
#             self.setEdge('e', 'p2')
#             self.setEdge('p4', 'tau')
#                      
#             self.setEdge('tau', 'p5')
#             self.setEdge('p5', 'c')
#             self.setEdge('p5', 'f')
#                                    
#             self.setEdge('c', 'p6')
#             self.setEdge('p6', 'g')
#             self.setEdge('g', 'p7')
#             self.setEdge('f', 'p7')
#             self.setEdge('p7', 'h')
#             self.setEdge('h', 'p8')
#             self.setToken('p1')
#########################################################################################################

####################################################################    
#     ## Paper example (Hospital check-up) - with silent transition, instead of K ##### Journal
#     ## Final list of dependencies =  {'g': [['e']], 'b': [['a'], ['*n']], 'l': [['*tau'], ['*g']], 
#                 'd': [['b']], 'e': [['*d'], ['*h']], 'j': [['c']], 'c': [['b']], 'i': [['c']], 
#                 'm': [['l']], 'f': [['e']], 'h': [['f']], 'tau': [['i', 'j']], 'n': [['l']], 'a': []}
#
#     originalDependencies  =  {'g': [['e']], 'b': [['a'], ['*n']], 'l': [['*tau'], ['*g']], 
#                 'd': [['b']], 'e': [['*d'], ['*h']], 'j': [['c']], 'c': [['b']], 'i': [['c']], 
#                 'm': [['l']], 'f': [['e']], 'h': [['f']], 'tau': [['i', 'j']], 'n': [['l']], 'a': []}
#     CleanTD = {'b': [['a'], ['*n']], 'f': [['e']], 'l': [['*g'], ['*i,*j']], 'n': [['l']], 
#      'e': [['*d'], ['*h']], 'd': [['b']], 'i': [['c']], 'c': [['b']], 'g': [['e']], 
#      'h': [['f']], 'm': [['l']], 'j': [['c']], 'a': []}
#  
#             self.addTransition("A");
#             self.addTransition("B");
#             self.addTransition("C");
#             self.addTransition("D");
#             self.addTransition("E");
#             self.addTransition("F");
#             self.addTransition("G");
#             self.addTransition("H");
#             self.addTransition("I");
#             self.addTransition("J");
#             self.addTransition("tau");
#             self.addTransition("L");
#             self.addTransition("M");
#             self.addTransition("N");
#                    
#                    
#             self.addPlace("p1");
#             self.addPlace("p2");
#             self.addPlace("p3");
#             self.addPlace("p4");
#             self.addPlace("p5");
#             self.addPlace("p6");
#             self.addPlace("p7");
#             self.addPlace("p8");
#             self.addPlace("p9");
#             self.addPlace("p10");
#             self.addPlace("p11");
#             self.addPlace("p12");
#             self.addPlace("p13");
#                    
#                    
#             self.setEdge('p1', 'a');
#             self.setEdge('a', 'p2');
#             self.setEdge('p2', 'b');
#             self.setEdge('b', 'p3');
#             self.setEdge('p3', 'c');
#             self.setEdge('p3', 'd');
#                    
#             self.setEdge('c', 'p4');
#             self.setEdge('c', 'p5');
#             self.setEdge('p4', 'i');
#             self.setEdge('p5', 'j');
#             self.setEdge('i', 'p6');
#             self.setEdge('j', 'p7');
#             self.setEdge('p6', 'tau');
#             self.setEdge('p7', 'tau');
#                    
#             self.setEdge('d', 'p8');
#             self.setEdge('p8', 'e');
#             self.setEdge('e', 'p9');
#             self.setEdge('p9', 'f');
#             self.setEdge('p9', 'g');
#             self.setEdge('f', 'p10');
#             self.setEdge('p10', 'h');
#             self.setEdge('h', 'p8');
#                    
#             self.setEdge('tau', 'p11');
#             self.setEdge('g', 'p11');
#             self.setEdge('p11', 'l');
#             self.setEdge('l', 'p12');
#             self.setEdge('p12', 'm');
#             self.setEdge('p12', 'n');
#             self.setEdge('n', 'p2');
#                    
#             self.setEdge('m', 'p13');
#                        
#             self.setToken('p1')






    
####################################################################
#     # My loop example:
#     # Final list of dependencies =  {'d': [['a']], 'g': [['c']], 'h': [['g', 'f']], 'c': [['b']], 'e': [['a', 'c']], 'b': [['d'], ['*e']], 'f': [['b']], 'a': []}
#     
#             self.addPlace('p1')
#             self.addPlace('p2')
#             self.addPlace('p3')
#             self.addPlace('p4')
#             self.addPlace('p5')
#             self.addPlace('p6')
#             self.addPlace('p7')
#             self.addPlace('p8')
#             self.addPlace('p9')
#                 
#             self.addTransition('a')
#             self.addTransition('b')
#             self.addTransition('c')
#             self.addTransition('d')
#             self.addTransition('e')
#             self.addTransition('f')
#             self.addTransition('g')
#             self.addTransition('h')
#                 
#                 
#             self.setEdge('p1', 'a')
#             self.setEdge('a', 'p2')
#             self.setEdge('p2', 'd')
#             self.setEdge('p2', 'e')
#             self.setEdge('d', 'p3')
#             self.setEdge('e', 'p3')
#             self.setEdge('p3', 'b')
#             self.setEdge('b', 'p4')
#                 
#             self.setEdge('p4', 'f')
#             self.setEdge('p4', 'c')
#                 
#                 
#             self.setEdge('c', 'p6')
#             self.setEdge('p6', 'g')
#             self.setEdge('g', 'p7')
#             self.setEdge('p7', 'h')
#             self.setEdge('c', 'p5')
#             self.setEdge('p5', 'e')
#                 
#             self.setEdge('f', 'p8')
#             self.setEdge('p8', 'h')
#             self.setEdge('h', 'p9')
#                 
#             self.setToken('p1')
####################################################################

    
####################################################################    
#     # Complicated loop example, not sound PN
#     # a->AND( e, XOR(e,d))->b->XOR((c->XOR(e,g)) , f)->h   
#     # loop(e->b->c->e)
#     # Final list of dependencies =  {'e': [['a', 'c']], 'h': [['f', 'g']], 'g': [['c']], 'd': [['a']], 'b': [['d'], ['*e']], 'f': [['b']], 'a': [], 'c': [['b']]}
#     
#     self.addTransition("A")
#     self.addTransition("B")
#     self.addTransition("C")
#     self.addTransition("D")
#     self.addTransition("E")
#     self.addTransition("F")
#     self.addTransition("G")
#     self.addTransition("H")
#     
#     self.addPlace("p1")
#     self.addPlace("p2")
#     self.addPlace("p3")
#     self.addPlace("p4")
#     self.addPlace("p5")
#     self.addPlace("p6")
#     self.addPlace("p7")
#     self.addPlace("p8")
#     self.addPlace("p9")
#     
#     self.setEdge('p1', 'a')
#     self.setEdge('a', 'p2')
#     self.setEdge('p2', 'd')
#     self.setEdge('p2', 'e')
#     self.setEdge('d', 'p3')
#     self.setEdge('e', 'p3')
#     self.setEdge('p3', 'b')
#     self.setEdge('b', 'p4')
#     self.setEdge('p4', 'f')
#     self.setEdge('p4', 'c')
#     
#     self.setEdge('c', 'p5')
#     self.setEdge('p5', 'e')
#     self.setEdge('f', 'p8')
#     self.setEdge('p8', 'h')
#     
#     self.setEdge('c', 'p6')
#     self.setEdge('p6', 'g')
#     self.setEdge('g', 'p7')
#     self.setEdge('p7', 'h')
#     
#     self.setEdge('h', 'p9')
#     
#     self.setToken('p1')
#     #self.buildNet()
#     #self.buildModel() # This step is to provide model logic for GraphModel application
####################################################################

####################################################################    
#     ## Paper example (Hosptial check-up)
#     ## Final list of dependencies =  {'f': [['e']], 'c': [['b']], 'j': [['c']], 'b': [['a'], ['*n']], 'k': [['i', 'j']], 'l': [['*k'], ['*g']], 'm': [['l']], 'e': [['*h'], ['*d']], 'd': [['b']], 'i': [['c']], 'g': [['e']], 'h': [['f']], 'a': [], 'n': [['l']]}
#     self.addTransition("A");
#     self.addTransition("B");
#     self.addTransition("C");
#     self.addTransition("D");
#     self.addTransition("E");
#     self.addTransition("F");
#     self.addTransition("G");
#     self.addTransition("H");
#     self.addTransition("I");
#     self.addTransition("J");
#     self.addTransition("K");
#     self.addTransition("L");
#     self.addTransition("M");
#     self.addTransition("N");
#     
#     '''
#     self.addTransition("O");
#     self.addTransition("P");
#     self.addTransition("Q");
#     self.addTransition("R");
#     '''
#     
#     
#     self.addPlace("p1");
#     self.addPlace("p2");
#     self.addPlace("p3");
#     self.addPlace("p4");
#     self.addPlace("p5");
#     self.addPlace("p6");
#     self.addPlace("p7");
#     self.addPlace("p8");
#     self.addPlace("p9");
#     self.addPlace("p10");
#     self.addPlace("p11");
#     self.addPlace("p12");
#     self.addPlace("p13");
#     '''
#     self.addPlace("p14");
#     self.addPlace("p15");
#     self.addPlace("p16");
#     '''
#          
#     self.setEdge('p1', 'a');
#     self.setEdge('a', 'p2');
#     self.setEdge('p2', 'b');
#     self.setEdge('b', 'p3');
#     self.setEdge('p3', 'c');
#     self.setEdge('p3', 'd');
#     
#     self.setEdge('c', 'p4');
#     self.setEdge('c', 'p5');
#     self.setEdge('p4', 'i');
#     self.setEdge('p5', 'j');
#     self.setEdge('i', 'p6');
#     self.setEdge('j', 'p7');
#     self.setEdge('p6', 'k');
#     self.setEdge('p7', 'k');
#     
#     self.setEdge('d', 'p8');
#     self.setEdge('p8', 'e');
#     self.setEdge('e', 'p9');
#     self.setEdge('p9', 'f');
#     self.setEdge('p9', 'g');
#     self.setEdge('f', 'p10');
#     self.setEdge('p10', 'h');
#     self.setEdge('h', 'p8');
#     
#     self.setEdge('k', 'p11');
#     self.setEdge('g', 'p11');
#     self.setEdge('p11', 'l');
#     self.setEdge('l', 'p12');
#     self.setEdge('p12', 'm');
#     self.setEdge('p12', 'n');
#     self.setEdge('n', 'p2');
#     
#     self.setEdge('m', 'p13');
#     
#     
#     '''
#     self.setEdge('p13', 'o')
#     self.setEdge('o', 'p14')
#     self.setEdge('p14', 'p')
#     self.setEdge('p', 'p15')
#     self.setEdge('p15', 'q')
#     self.setEdge('q', 'p14')
#     self.setEdge('p15', 'r')
#     self.setEdge('r', 'p16')
#     '''
#     
#     
#     self.setToken('p1')
###################################################################


####################################################################
#  BPI 2013 - VOLVO company - concept name only (not as Dina for comparison) - incidents
#     ### Final list of dependencies =  {
#             'queued': [['tau from tree1'], ['*tau from tree3']], 
#             'tau from tree1': [], 
#             'tau from tree3': [['queued']], 
#             'tau from tree2': [['queued']], 
#             'tau from tree5': [['accepted']], 
#             'accepted': [['tau from tree1'], ['*tau from tree5']], 
#             'completed': [['tau from tree1']], 
#             'tau from tree4': [['accepted']], 
#             'tau from tree6': [['completed', 'tau from tree2', 'tau from tree4']]}
# #  Should be   #Final Dependencies = {'queued': [['accepted']], 'accepted': [[],['unmatched'], ['*queued']], 'completed': [['accepted']], 'unmatched': []}     
#     cleanedTD = {'completed': [], 'queued': [['*queued']], 'accepted': [['*accepted']]}
#      
#      
#             self.addTransition("Accepted")
#             self.addTransition("Queued")
#             self.addTransition("Completed")
#              
#             self.addTransition("tau from tree1")
#             self.addTransition("tau from tree2")
#             self.addTransition("tau from tree3")
#             self.addTransition("tau from tree4")
#             self.addTransition("tau from tree5")
#             self.addTransition("tau from tree6")
#              
#             self.addPlace("p1")
#             self.addPlace("p2")
#             self.addPlace("p3")
#             self.addPlace("p4")
#             self.addPlace("p5")
#             self.addPlace("p6")
#             self.addPlace("p7")
#             self.addPlace("p8")
#             self.addPlace("p9")
#             self.addPlace("p10")
#              
#             self.setEdge("p1", "tau from tree1")
#              
#             self.setEdge("tau from tree1", "p2")
#             self.setEdge("tau from tree1", "p3")
#             self.setEdge("tau from tree1", "p4")
#             
#             self.setEdge("p2", "Queued")
#             self.setEdge("p3", "Completed")
#             self.setEdge("p4", "Accepted")
#              
#             self.setEdge("Queued", "p5")
#             self.setEdge("Completed", "p6")
#             self.setEdge("Accepted", "p7")
#              
#             self.setEdge("p5", "tau from tree2")
#             self.setEdge("p5", "tau from tree3")
#             self.setEdge("tau from tree3", "p2")
#             
#             self.setEdge("p7", "tau from tree4")
#             self.setEdge("p7", "tau from tree5")
#             self.setEdge("tau from tree5", "p4")
#             
#             
#             self.setEdge("tau from tree2", "p8")
#             self.setEdge("tau from tree4", "p9")
#             
#             self.setEdge("p6", "tau from tree6")
#             self.setEdge("p8", "tau from tree6")
#             self.setEdge("p9", "tau from tree6")
#             
#             self.setEdge("tau from tree6", "p10")
#             
#                  
#             self.setToken("p1")

####################################################################
#  BPI 2013 - VOLVO company - concept name only (as Dina for comparison) - closed problems
#     ### Final list of dependencies =  {
#             'queued': [['tau from tree1'], ['*tau from tree3']], 
#             'tau from tree1': [], 
#             'tau from tree3': [['queued']], 
#             'tau from tree2': [['queued']], 
#             'tau from tree5': [['accepted']], 
#             'accepted': [['tau from tree1'], ['*tau from tree5']], 
#             'completed': [['tau from tree1']], 
#             'tau from tree4': [['accepted']], 
#             'tau from tree6': [['completed', 'tau from tree2', 'tau from tree4']]}
     
#     cleanedTD = {'completed': [], 'queued': [['*queued']], 'accepted': [['*accepted']]}
#      
#       
#             self.addTransition("Unmatched")
#             self.addTransition("Accepted")
#             self.addTransition("Queued")
#             self.addTransition("Completed")
#                     
#             self.addTransition("tau1")
#             self.addTransition("tau2")
#                    
#             self.addPlace("p1")
#             self.addPlace("p2")
#             self.addPlace("p3")
#             self.addPlace("p4")
#             self.addPlace("p5")
#                     
#             self.setEdge("p1", "tau1")
#             self.setEdge("p1", "Unmatched")
#                     
#             self.setEdge("tau1", "p2")
#             self.setEdge("Unmatched", "p2")
#                    
#             self.setEdge("p2", "Accepted")
#             self.setEdge("Accepted", "p3")
#                    
#             self.setEdge("p3", "tau2")
#             self.setEdge("p3", "Queued")
#             self.setEdge("Queued", "p2")
#                    
#             self.setEdge("tau2", "p4")
#                    
#             self.setEdge("p4", "Completed")
#             self.setEdge("Completed", "p5")
#                     
#             self.setToken("p1")

####################################################################
#     ##  BPI 2013 - VOLVO company - indicents - including life cycle
#     ### Final list of dependencies =  {'tau from tree4': [['tau from tree3']], 
#             'tau from tree9': [['accepted-in progress']], 
#             'tau from tree8': [['accepted-in progress']], 
#             'completed-closed': [['completed-resolved']], 
#             'tau from tree7': [['queued-awaiting assignment']], 
#             'tau from tree13': [['tau from tree12'], ['completed-closed']], 
#             'accepted-assigned': [['tau from tree3']], 
#             'tau from tree5': [['tau from tree3']], 
#             'tau from tree11': [['tau from tree10']], 
#             'accepted-wait - vendor': [['accepted-wait'], ['tau from tree1']], 
#             'tau from tree1': [], 
#             'tau from tree10': [['accepted-wait - user', 'tau from tree4', 'accepted-assigned', 'tau from tree5', 'tau from tree7', 'tau from tree8']], 
#             'completed-in call': [['tau from tree10']], 
#             'tau from tree3': [['tau from tree2'], ['accepted-wait - vendor'], ['accepted-wait - implementation']], 
#             'tau from tree6': [['queued-awaiting assignment']], 
#             'accepted-wait': [], 
#             'accepted-in progress': [['tau from tree3'], ['*tau from tree9']], 
#             'accepted-wait - user': [['tau from tree3']], 
#             'completed-cancelled': [['tau from tree12'], ['completed-closed']], 
#             'queued-awaiting assignment': [['tau from tree3'], ['*tau from tree6']], 
#             'completed-resolved': [['tau from tree11'], ['completed-in call']], 
#             'tau from tree2': [['accepted-wait'], ['tau from tree1']], 
#             'accepted-wait - implementation': [], 
#             'tau from tree12': [['tau from tree11'], ['completed-in call']]}
#         
#             self.addTransition("Accepted-Wait - Implementation")
#             self.addTransition("Accepted-Wait")
#             self.addTransition("Accepted-Wait - Vendor")
#             self.addTransition("Queued-Awaiting Assignment")
#             self.addTransition("Accepted-Wait - User")
#             self.addTransition("Accepted-Assigned")
#             self.addTransition("Accepted-In Progress")
#             self.addTransition("Completed-In Call")
#             self.addTransition("Completed-Resolved")
#             self.addTransition("Completed-Closed")
#             self.addTransition("Completed-Cancelled")
#              
#             self.addTransition("tau from tree1")
#             self.addTransition("tau from tree2")
#             self.addTransition("tau from tree3")
#             self.addTransition("tau from tree4")
#             self.addTransition("tau from tree5")
#             self.addTransition("tau from tree6")
#             self.addTransition("tau from tree7")
#             self.addTransition("tau from tree8")
#             self.addTransition("tau from tree9")
#             self.addTransition("tau from tree10")
#             self.addTransition("tau from tree11")
#             self.addTransition("tau from tree12")
#             self.addTransition("tau from tree13")
#              
#             self.addPlace("p1")
#             self.addPlace("p2")
#             self.addPlace("p3")
#             self.addPlace("p4")
#             self.addPlace("p5")
#             self.addPlace("p6")
#             self.addPlace("p7")
#             self.addPlace("p8")
#             self.addPlace("p9")
#             self.addPlace("p10")
#             self.addPlace("p11")
#             self.addPlace("p12")
#             self.addPlace("p13")
#             self.addPlace("p14")
#             self.addPlace("p15")
#             self.addPlace("p16")
#             self.addPlace("p17")
#             self.addPlace("p18")
#              
#             self.setEdge("p1", "Accepted-Wait - Implementation")
#             self.setEdge("p1", "Accepted-Wait")
#             self.setEdge("p1", "tau from tree1")
#              
#             self.setEdge("Accepted-Wait", "p2")
#             self.setEdge("tau from tree1", "p2")
#             self.setEdge("p2", "tau from tree2")
#             self.setEdge("p2", "Accepted-Wait - Vendor")
#              
#             self.setEdge("tau from tree2", "p3")
#             self.setEdge("Accepted-Wait - Vendor", "p3")
#             self.setEdge("Accepted-Wait - Implementation", "p3")
#              
#             self.setEdge("p3", "tau from tree3")
#              
#             self.setEdge("tau from tree3", "p4")
#             self.setEdge("tau from tree3", "p5")
#             self.setEdge("tau from tree3", "p6")
#             self.setEdge("tau from tree3", "p7")
#              
#             self.setEdge("p4", "Queued-Awaiting Assignment")
#             self.setEdge("Queued-Awaiting Assignment", "p8")
#             self.setEdge("p8", "tau from tree6")
#             self.setEdge("p8", "tau from tree7")
#             self.setEdge("tau from tree6", "p4")
#             self.setEdge("tau from tree7", "p12")
#              
#             self.setEdge("p5", "Accepted-In Progress")
#             self.setEdge("Accepted-In Progress", "p9")
#             self.setEdge("p9", "tau from tree8")
#             self.setEdge("p9", "tau from tree9")
#             self.setEdge("tau from tree9", "p5")
#             self.setEdge("tau from tree8", "p13")
#              
#             self.setEdge("p6", "Accepted-Wait - User")
#             self.setEdge("p6", "tau from tree4")
#             self.setEdge("Accepted-Wait - User", "p10")
#             self.setEdge("tau from tree4", "p10")
#              
#             self.setEdge("p7", "Accepted-Assigned")
#             self.setEdge("p7", "tau from tree5")
#             self.setEdge("Accepted-Assigned", "p11")
#             self.setEdge("tau from tree5", "p11")
#          
#             self.setEdge("p10", "tau from tree10")
#             self.setEdge("p11", "tau from tree10")
#             self.setEdge("p12", "tau from tree10")
#             self.setEdge("p13", "tau from tree10")
#              
#             self.setEdge("tau from tree10", "p14")
#              
#             self.setEdge("p14", "tau from tree11")
#             self.setEdge("p14", "Completed-In Call")
#          
#             self.setEdge("tau from tree11", "p15")
#             self.setEdge("Completed-In Call", "p15")
#              
#             self.setEdge("p15", "tau from tree12")
#             self.setEdge("p15", "Completed-Resolved")
#              
#             self.setEdge("Completed-Resolved", "p16")
#             self.setEdge("p16", "Completed-Closed")
#              
#             self.setEdge("tau from tree12", "p17")
#             self.setEdge("Completed-Closed", "p17")
#              
#             self.setEdge("p17", "tau from tree13")
#             self.setEdge("p17", "Completed-Cancelled")
#              
#             self.setEdge("tau from tree13", "p18")
#             self.setEdge("Completed-Cancelled", "p18")
#              
#             self.setToken('p1')
####################################################################
#     ##  BPI 2012 
#     ##Final list of dependencies =  {'tau from tree4': [['w_valideren aanvraag-complete'], ['w_nabellen incomplete dossiers-complete'], ['tau from tree6'], ['*tau from tree3'], ['*w_beoordelen fraude-start']], 
#             'tau from tree9': [['tau from tree5']], 
#             'tau from tree10': [['o_created-complete']], 
#             'w_nabellen incomplete dossiers-schedule': [['w_valideren aanvraag-start']], 
#             'w_nabellen offertes-start': [['tau from tree2']], 
#             'tau from tree2': [['a_accepted-complete'], ['w_nabellen offertes-complete'], ['w_nabellen offertes-start'], ['w_beoordelen fraude-complete'], ['w_wijzigen contractgegevens-schedule'], ['w_afhandelen leads-complete'], ['w_completeren aanvraag-start'], ['w_completeren aanvraag-schedule'], ['*w_completeren aanvraag-complete'], ['*tau from tree1']], 
#             'o_sent_back-complete': [['w_valideren aanvraag-complete'], ['w_nabellen incomplete dossiers-complete'], ['tau from tree6'], ['*tau from tree3'], ['*w_beoordelen fraude-start']], 
#             'w_beoordelen fraude-schedule': [['a_partlysubmitted-complete'], ['*tau from tree16']], 
#             'o_selected-complete': [['w_valideren aanvraag-complete'], ['w_nabellen incomplete dossiers-complete'], ['*tau from tree3'], ['*w_beoordelen fraude-start'], ['*tau from tree6']], 
#             'a_approved-complete': [['tau from tree7']], 
#             'tau from tree8': [['w_valideren aanvraag-start']], 
#             'tau from tree6': [['o_selected-complete'], ['a_finalized-complete'], ['a_cancelled-complete'], ['*o_cancelled-complete']], 
#             'tau from tree14': [['w_afhandelen leads-start'], ['w_nabellen incomplete dossiers-start'], ['w_nabellen incomplete dossiers-schedule'], ['tau from tree8'], ['tau from tree9'], ['tau from tree11'], ['tau from tree12'], ['*w_valideren aanvraag-schedule'], ['*tau from tree4'], ['*o_declined-complete']], 
#             'w_valideren aanvraag-schedule': [['o_sent_back-complete']], 
#             'a_registered-complete': [['tau from tree7']], 
#             'tau from tree5': [['o_selected-complete'], ['a_finalized-complete'], ['a_cancelled-complete'], ['*o_cancelled-complete']], 
#             'o_sent-complete': [['tau from tree10']], 
#             'tau from tree12': [['w_nabellen offertes-schedule', 'o_sent-complete']], 
#             'a_partlysubmitted-complete': [['a_submitted-complete']], 
#             'o_cancelled-complete': [['w_valideren aanvraag-complete'], ['w_nabellen incomplete dossiers-complete'], ['*tau from tree3'], ['*w_beoordelen fraude-start'], ['*tau from tree6']], 
#             'w_nabellen incomplete dossiers-complete': [['a_declined-complete'], ['w_afhandelen leads-schedule'], ['a_preaccepted-complete'], ['*w_beoordelen fraude-schedule'], ['*tau from tree14']], 
#             'w_valideren aanvraag-start': [['w_valideren aanvraag-complete'], ['w_nabellen incomplete dossiers-complete'], ['tau from tree6'], ['*tau from tree3'], ['*w_beoordelen fraude-start']], 
#             'w_beoordelen fraude-complete': [['tau from tree2']], 
#             'w_beoordelen fraude-start': [['a_declined-complete'], ['w_afhandelen leads-schedule'], ['a_preaccepted-complete'], ['*w_beoordelen fraude-schedule'], ['*tau from tree14']], 
#             'w_nabellen incomplete dossiers-start': [['w_valideren aanvraag-complete'], ['w_nabellen incomplete dossiers-complete'], ['tau from tree6'], ['*tau from tree3'], ['*w_beoordelen fraude-start']], 
#             'a_activated-complete': [['tau from tree7']], 
#             'o_declined-complete': [['a_declined-complete'], ['w_afhandelen leads-schedule'], ['a_preaccepted-complete'], ['*w_beoordelen fraude-schedule'], ['*tau from tree14']], 
#             'tau from tree13': [['w_afhandelen leads-start'], ['w_nabellen incomplete dossiers-start'], ['tau from tree4'], ['w_valideren aanvraag-schedule'], ['w_nabellen incomplete dossiers-schedule'], ['tau from tree8'], ['tau from tree9'], ['tau from tree11'], ['tau from tree12'], ['*o_declined-complete']], 
#             'tau from tree15': [['tau from tree13']], 
#             'a_declined-complete': [['a_partlysubmitted-complete'], ['*tau from tree16']], 
#             'tau from tree3': [['tau from tree2']], 
#             'a_accepted-complete': [['tau from tree2']], 
#             'w_valideren aanvraag-complete': [['a_declined-complete'], ['w_afhandelen leads-schedule'], ['a_preaccepted-complete'], ['*w_beoordelen fraude-schedule'], ['*tau from tree14']], 
#             'w_nabellen offertes-schedule': [['tau from tree10']], 
#             'w_afhandelen leads-start': [['a_declined-complete'], ['w_afhandelen leads-schedule'], ['a_preaccepted-complete'], ['*w_beoordelen fraude-schedule'], ['*tau from tree14']], 
#             'a_preaccepted-complete': [['a_partlysubmitted-complete'], ['*tau from tree16']], 
#             'w_completeren aanvraag-complete': [['tau from tree2']], 
#             'w_afhandelen leads-schedule': [['a_partlysubmitted-complete'], ['*tau from tree16']], 
#             'w_nabellen offertes-complete': [['tau from tree2']], 
#             'o_accepted-complete': [['tau from tree7']], 
#             'a_finalized-complete': [['w_valideren aanvraag-complete'], ['w_nabellen incomplete dossiers-complete'], ['*tau from tree3'], ['*w_beoordelen fraude-start'], ['*tau from tree6']], 
#             'a_submitted-complete': [], 
#             'o_created-complete': [['tau from tree5']], 
#             'w_wijzigen contractgegevens-schedule': [['tau from tree2']], 
#             'w_afhandelen leads-complete': [['tau from tree2']], 
#             'tau from tree1': [['a_declined-complete'], ['w_afhandelen leads-schedule'], ['a_preaccepted-complete'], ['*w_beoordelen fraude-schedule'], ['*tau from tree14']], 
#             'a_cancelled-complete': [['w_valideren aanvraag-complete'], ['w_nabellen incomplete dossiers-complete'], ['*tau from tree3'], ['*w_beoordelen fraude-start'], ['*tau from tree6']], 
#             'w_completeren aanvraag-start': [['tau from tree2']], 
#             'tau from tree7': [['w_valideren aanvraag-start']], 
#             'w_completeren aanvraag-schedule': [['tau from tree2']], 
#             'tau from tree11': [['o_accepted-complete', 'a_registered-complete', 'a_approved-complete', 'a_activated-complete']], 
#             'tau from tree16': [['tau from tree13']]}
#
#
#             self.addTransition("A_SUBMITTED-COMPLETE")#t1
#             self.addTransition("A_PARTLYSUBMITTED-COMPLETE")#t2
#               
#             self.addTransition("A_DECLINED-COMPLETE")#t3
#             self.addTransition("W_Afhandelen leads-SCHEDULE")#t4
#             self.addTransition("A_PREACCEPTED-COMPLETE")#t5
#             self.addTransition("W_Beoordelen fraude-SCHEDULE")#t6
#               
#             self.addTransition("W_Afhandelen leads-START")#t7
#             self.addTransition("W_Beoordelen fraude-START")#t8
#             self.addTransition("O_DECLINED-COMPLETE")#t9
#             self.addTransition("W_Valideren aanvraag-COMPLETE")#t10
#             self.addTransition("W_Nabellen incomplete dossiers-COMPLETE")#t11
#               
#             self.addTransition("A_ACCEPTED-COMPLETE")#t12
#             self.addTransition("W_Nabellen offertes-COMPLETE")#t13
#             self.addTransition("W_Nabellen offertes-START")#t14
#             self.addTransition("W_Beoordelen fraude-COMPLETE")#t15
#             self.addTransition("W_Wijzigen contractgegevens-SCHEDULE")#t16
#             self.addTransition("W_Afhandelen leads-COMPLETE")#t17
#             self.addTransition("W_Completeren aanvraag-COMPLETE")#t18
#             self.addTransition("W_Completeren aanvraag-START")#t19
#             self.addTransition("W_Completeren aanvraag-SCHEDULE")#t20
#               
#             self.addTransition("W_Nabellen incomplete dossiers-START")#t21
#             self.addTransition("O_SELECTED-COMPLETE")#t22
#             self.addTransition("A_FINALIZED-COMPLETE")#t23
#             self.addTransition("O_CANCELLED-COMPLETE")#t24
#             self.addTransition("A_CANCELLED-COMPLETE")#t25
#             self.addTransition("O_SENT_BACK-COMPLETE")#t26
#             self.addTransition("W_Valideren aanvraag-START")#t27
#               
#             self.addTransition("W_Valideren aanvraag-SCHEDULE")#t28
#             self.addTransition("W_Nabellen incomplete dossiers-SCHEDULE")#t29
#               
#             self.addTransition("O_CREATED-COMPLETE")#t30
#             self.addTransition("O_ACCEPTED-COMPLETE")#t31
#             self.addTransition("A_REGISTERED-COMPLETE")#t32
#             self.addTransition("A_APPROVED-COMPLETE")#t33
#             self.addTransition("A_ACTIVATED-COMPLETE")#t34
#               
#             self.addTransition("W_Nabellen offertes-SCHEDULE")#t35
#             self.addTransition("O_SENT-COMPLETE")#t36
#           
#             self.addTransition("tau from tree1")
#             self.addTransition("tau from tree2")
#             self.addTransition("tau from tree3")
#             self.addTransition("tau from tree4")
#             self.addTransition("tau from tree5")
#             self.addTransition("tau from tree6")
#             self.addTransition("tau from tree7")
#             self.addTransition("tau from tree8")
#             self.addTransition("tau from tree9")
#             self.addTransition("tau from tree10")
#             self.addTransition("tau from tree11")
#             self.addTransition("tau from tree12")
#             self.addTransition("tau from tree13")
#             self.addTransition("tau from tree14")
#             self.addTransition("tau from tree15")
#             self.addTransition("tau from tree16")
#               
#             self.addPlace("p1")
#             self.addPlace("p2")
#             self.addPlace("p3")
#             self.addPlace("p4")
#             self.addPlace("p5")
#             self.addPlace("p6")
#             self.addPlace("p7")
#             self.addPlace("p8")
#             self.addPlace("p9")
#             self.addPlace("p10")
#             self.addPlace("p11")
#             self.addPlace("p12")
#             self.addPlace("p13")
#             self.addPlace("p14")
#             self.addPlace("p15")
#             self.addPlace("p16")
#             self.addPlace("p17")
#             self.addPlace("p18")
#             self.addPlace("p19")
#             self.addPlace("p20")
#             self.addPlace("p21")
#             self.addPlace("p22")
#             self.addPlace("p23")
#             self.addPlace("p24")
#             self.addPlace("p25")
#             self.addPlace("p26")
#             self.addPlace("p27")
#               
#             self.setEdge("p1", "A_SUBMITTED-COMPLETE")
#             self.setEdge("A_SUBMITTED-COMPLETE", "p2")
#             self.setEdge("p2", "A_PARTLYSUBMITTED-COMPLETE")
#             self.setEdge("A_PARTLYSUBMITTED-COMPLETE", "p3")
#               
#             self.setEdge("p3", "A_DECLINED-COMPLETE")
#             self.setEdge("p3", "W_Afhandelen leads-SCHEDULE")
#             self.setEdge("p3", "A_PREACCEPTED-COMPLETE")
#             self.setEdge("p3", "W_Beoordelen fraude-SCHEDULE")
#           
#             self.setEdge("A_DECLINED-COMPLETE", "p4")
#             self.setEdge("W_Afhandelen leads-SCHEDULE", "p4")
#             self.setEdge("A_PREACCEPTED-COMPLETE", "p4")
#             self.setEdge("W_Beoordelen fraude-SCHEDULE", "p4")
#               
#             self.setEdge("p4", "tau from tree1")
#             self.setEdge("p4", "W_Afhandelen leads-START")
#             self.setEdge("p4", "W_Beoordelen fraude-START")
#             self.setEdge("p4", "O_DECLINED-COMPLETE")
#             self.setEdge("p4", "W_Valideren aanvraag-COMPLETE")
#             self.setEdge("p4", "W_Nabellen incomplete dossiers-COMPLETE")
#               
#             self.setEdge("tau from tree1", "p5")
#             self.setEdge("p5", "tau from tree2")
#             self.setEdge("tau from tree2", "p6")
#               
#             self.setEdge("p6", "A_ACCEPTED-COMPLETE")
#             self.setEdge("p6", "W_Nabellen offertes-COMPLETE")
#             self.setEdge("p6", "W_Nabellen offertes-START")
#             self.setEdge("p6", "W_Beoordelen fraude-COMPLETE")
#             self.setEdge("p6", "W_Wijzigen contractgegevens-SCHEDULE")
#             self.setEdge("p6", "W_Afhandelen leads-COMPLETE")
#             self.setEdge("p6", "W_Completeren aanvraag-COMPLETE")
#             self.setEdge("p6", "W_Completeren aanvraag-START")
#             self.setEdge("p6", "W_Completeren aanvraag-SCHEDULE")
#             self.setEdge("p6", "tau from tree3")
#               
#             self.setEdge("A_ACCEPTED-COMPLETE", "p5")
#             self.setEdge("W_Nabellen offertes-COMPLETE", "p5")
#             self.setEdge("W_Nabellen offertes-START", "p5")
#             self.setEdge("W_Beoordelen fraude-COMPLETE", "p5")
#             self.setEdge("W_Wijzigen contractgegevens-SCHEDULE", "p5")
#             self.setEdge("W_Afhandelen leads-COMPLETE", "p5")
#             self.setEdge("W_Completeren aanvraag-COMPLETE", "p5")
#             self.setEdge("W_Completeren aanvraag-START", "p5")
#             self.setEdge("W_Completeren aanvraag-SCHEDULE", "p5")
#               
#             self.setEdge("W_Afhandelen leads-START", "p25")
#             self.setEdge("W_Beoordelen fraude-START", "p7")
#             self.setEdge("O_DECLINED-COMPLETE", "p25")
#             self.setEdge("W_Valideren aanvraag-COMPLETE", "p7")
#             self.setEdge("W_Nabellen incomplete dossiers-COMPLETE", "p7")
#             self.setEdge("tau from tree3", "p7")
#               
#             self.setEdge("p7", "W_Nabellen incomplete dossiers-START")
#             self.setEdge("p7", "O_SELECTED-COMPLETE")
#             self.setEdge("p7", "A_FINALIZED-COMPLETE")
#             self.setEdge("p7", "O_CANCELLED-COMPLETE")
#             self.setEdge("p7", "A_CANCELLED-COMPLETE")
#             self.setEdge("p7", "O_SENT_BACK-COMPLETE")
#             self.setEdge("p7", "W_Valideren aanvraag-START")
#             self.setEdge("p7", "tau from tree4")
#               
#             self.setEdge("W_Nabellen incomplete dossiers-START", "p25")
#             self.setEdge("O_SELECTED-COMPLETE", "p8")
#             self.setEdge("A_FINALIZED-COMPLETE", "p8")
#             self.setEdge("O_CANCELLED-COMPLETE", "p8")
#             self.setEdge("A_CANCELLED-COMPLETE", "p8")
#             self.setEdge("O_SENT_BACK-COMPLETE", "p9")
#             self.setEdge("tau from tree4", "p25")
#             self.setEdge("W_Valideren aanvraag-START", "p10")
#               
#             self.setEdge("p8", "tau from tree5")
#             self.setEdge("p8", "tau from tree6")
#             self.setEdge("p9", "W_Valideren aanvraag-SCHEDULE")
#             self.setEdge("p10", "W_Nabellen incomplete dossiers-SCHEDULE")
#             self.setEdge("p10", "tau from tree7")
#             self.setEdge("p10", "tau from tree8")
#               
#             self.setEdge("tau from tree5", "p11")
#             self.setEdge("W_Valideren aanvraag-SCHEDULE", "p25")
#             self.setEdge("tau from tree6", "p7")
#             self.setEdge("W_Nabellen incomplete dossiers-SCHEDULE", "p25")
#             self.setEdge("tau from tree7", "p12")
#             self.setEdge("tau from tree7", "p13")
#             self.setEdge("tau from tree7", "p14")
#             self.setEdge("tau from tree7", "p15")
#             self.setEdge("tau from tree8", "p25")
#               
#             self.setEdge("p11", "tau from tree9")
#             self.setEdge("p11", "O_CREATED-COMPLETE")
#               
#             self.setEdge("p12", "O_ACCEPTED-COMPLETE")
#             self.setEdge("p13", "A_REGISTERED-COMPLETE")
#             self.setEdge("p14", "A_APPROVED-COMPLETE")
#             self.setEdge("p15", "A_ACTIVATED-COMPLETE")
#               
#             self.setEdge("tau from tree9", "p25")
#             self.setEdge("O_CREATED-COMPLETE", "p16")
#             self.setEdge("O_ACCEPTED-COMPLETE", "p17")
#             self.setEdge("A_REGISTERED-COMPLETE", "p18")
#             self.setEdge("A_APPROVED-COMPLETE", "p19")
#             self.setEdge("A_ACTIVATED-COMPLETE", "p20")
#               
#             self.setEdge("p16", "tau from tree10")
#             self.setEdge("p17", "tau from tree11")
#             self.setEdge("p18", "tau from tree11")
#             self.setEdge("p19", "tau from tree11")
#             self.setEdge("p20", "tau from tree11")
#               
#             self.setEdge("tau from tree10", "p21")
#             self.setEdge("tau from tree10", "p22")
#             self.setEdge("tau from tree11", "p25")
#               
#             self.setEdge("p21", "W_Nabellen offertes-SCHEDULE")
#             self.setEdge("p22", "O_SENT-COMPLETE")
#               
#             self.setEdge("W_Nabellen offertes-SCHEDULE", "p23")
#             self.setEdge("O_SENT-COMPLETE", "p24")
#               
#             self.setEdge("p23", "tau from tree12")
#             self.setEdge("p24", "tau from tree12")
#               
#             self.setEdge("tau from tree12", "p25")
#               
#             self.setEdge("p25", "tau from tree13")
#             self.setEdge("p25", "tau from tree14")
#               
#             self.setEdge("tau from tree13", "p26")
#             self.setEdge("tau from tree14", "p4")
#               
#             self.setEdge("p26", "tau from tree15")
#             self.setEdge("p26", "tau from tree16")
#               
#             self.setEdge("tau from tree15", "p27")
#             self.setEdge("tau from tree16", "p3")
#               
#               
#             self.setToken('p1')

####################################################################
#     # WABO CoSeLog - original example: (Used in Comparisons)
#     # Final list of dependencies =  {'t15 print document x request unlicensed': [['t10 determine necessity to stop indication']], 
#             'tau from tree9': [['t04 determine confirmation of receipt', 't02 check confirmation of receipt']], 
#             't02 check confirmation of receipt': [['tau from tree8']], 
#             't10 determine necessity to stop indication': [['t06 determine necessity of stop advice']], 
#             't04 determine confirmation of receipt': [['tau from tree8']], 
#             't11 create document x request unlicensed': [['tau from tree4']], 
#             't09-4 process or receive external advice from party 4': [['tau from tree2']], 
#             't09-2 process or receive external advice from party 2': [['tau from tree2']], 
#             'tau from tree1': [['confirmation of receipt'], ['*tau from tree5']], 
#             'tau from tree11': [['t14 determine document x request unlicensed'], ['tau from tree10']], 
#             't14 determine document x request unlicensed': [['tau from tree9']], 
#             't05 print and send confirmation of receipt': [['t14 determine document x request unlicensed'], ['tau from tree10']], 
#             't09-1 process or receive external advice from party 1': [['tau from tree2']], 
#             't07-4 draft internal advice to hold for type 4': [['confirmation of receipt'], ['*tau from tree5']], 
#             't16 report reasons to hold request': [['tau from tree4']], 
#             't07-2 draft intern advice aspect 2': [['confirmation of receipt'], ['*tau from tree5']], 
#             'tau from tree2': [['t08 draft and send request for advice'], ['t09-1 process or receive external advice from party 1'], ['t09-2 process or receive external advice from party 2'], ['t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*tau from tree1']], 
#             'tau from tree7': [['t17 check report y to stop indication']], 
#             't08 draft and send request for advice': [['tau from tree2']], 
#             't07-1 draft intern advice aspect 1': [['confirmation of receipt'], ['*tau from tree5']], 
#             't17 check report y to stop indication': [['t16 report reasons to hold request'], ['*t18 adjust report y to stop indicition']], 
#             'tau from tree12': [['t10 determine necessity to stop indication']], 
#             't06 determine necessity of stop advice': [['tau from tree11'], ['t05 print and send confirmation of receipt']], 
#             't18 adjust report y to stop indicition': [['t17 check report y to stop indication']], 
#             't12 check document x request unlicensed': [['t11 create document x request unlicensed']], 
#             'tau from tree5': [['t03 adjust confirmation of receipt'], ['t07-1 draft intern advice aspect 1'], ['t07-3 draft intern advice hold for aspect 3'], ['t07-4 draft internal advice to hold for type 4'], ['t07-5 draft intern advice aspect 5'], ['*t07-2 draft intern advice aspect 2'], ['*tau from tree3']], 
#             'tau from tree3': [['tau from tree2']], 
#             'confirmation of receipt': [], 
#             't09-3 process or receive external advice from party 3': [['tau from tree2']], 
#             't19 determine report y to stop indication': [['tau from tree7']], 
#             't03 adjust confirmation of receipt': [['confirmation of receipt'], ['*tau from tree5']], 
#             't20 print report y to stop indication': [['t19 determine report y to stop indication']], 
#             'tau from tree6': [['tau from tree4']], 
#             't07-3 draft intern advice hold for aspect 3': [['confirmation of receipt'], ['*tau from tree5']], 
#             'tau from tree8': [['t12 check document x request unlicensed'], ['tau from tree6'], ['t20 print report y to stop indication']], 
#             'tau from tree4': [['tau from tree3'], ['t03 adjust confirmation of receipt'], ['t07-1 draft intern advice aspect 1'], ['t07-2 draft intern advice aspect 2'], ['t07-3 draft intern advice hold for aspect 3'], ['t07-4 draft internal advice to hold for type 4'], ['t07-5 draft intern advice aspect 5']], 
#             'tau from tree10': [['tau from tree9']], 
#             't07-5 draft intern advice aspect 5': [['confirmation of receipt'], ['*tau from tree5']]}
# 
            self.addTransition("Confirmation of receipt")
            self.addTransition("T02 Check confirmation of receipt")
            self.addTransition("T03 Adjust confirmation of receipt")
            self.addTransition("T04 Determine confirmation of receipt")
            self.addTransition("T05 print and send confirmation of receipt")
            self.addTransition("T06 Determine necessity of stop advice")
            self.addTransition("T07-1 Draft intern advice aspect 1")
            self.addTransition("T07-2 Draft intern advice aspect 2")
            self.addTransition("T07-3 Draft intern advice hold for aspect 3")
            self.addTransition("T07-4 Draft internal advice to hold for type 4")
            self.addTransition("T07-5 Draft intern advice aspect 5")
            self.addTransition("T08 Draft and send request for advice")
            self.addTransition("T09-1 Process or receive external advice from party 1")
            self.addTransition("T09-2 Process or receive external advice from party 2")
            self.addTransition("T09-3 Process or receive external advice from party 3")
            self.addTransition("T09-4 Process or receive external advice from party 4")
            self.addTransition("T10 Determine necessity to stop indication")
            self.addTransition("T11 Create document X request unlicensed")
            self.addTransition("T12 Check document X request unlicensed")
            self.addTransition("T14 Determine document X request unlicensed")
            self.addTransition("T15 print document X request unlicensed")
            self.addTransition("T16 Report reasons to hold request")
            self.addTransition("T17 Check report Y to stop indication")
            self.addTransition("T18 Adjust report Y to stop indicition")
            self.addTransition("T19 Determine report Y to stop indication")
            self.addTransition("T20 print report Y to stop indication")
#                  
            self.addTransition("tau from tree1")
            self.addTransition("tau from tree2")
            self.addTransition("tau from tree3")
            self.addTransition("tau from tree4")
            self.addTransition("tau from tree5")
            self.addTransition("tau from tree6")
            self.addTransition("tau from tree7")
            self.addTransition("tau from tree8")
            self.addTransition("tau from tree9")
            self.addTransition("tau from tree10")
            self.addTransition("tau from tree11")
            self.addTransition("tau from tree12")
            self.addTransition("tau from tree13")
                     
            self.addPlace('p1')
            self.addPlace('p2')
            self.addPlace('p3')
            self.addPlace('p4')
            self.addPlace('p5')
            self.addPlace('p6')
            self.addPlace('p7')
            self.addPlace('p8')
            self.addPlace('p9')
            self.addPlace('p10')
            self.addPlace('p11')
            self.addPlace('p12')
                     
            self.setEdge( "p1", "Confirmation of receipt")
            self.setEdge( "Confirmation of receipt", "p2")
                    
            self.setEdge( "p2", "tau from tree1")
            self.setEdge( "p2", "T02 Check confirmation of receipt")
            self.setEdge( "p2", "T06 Determine necessity of stop advice")
            self.setEdge( "tau from tree1", "p12")
                    
            self.setEdge( "T02 Check confirmation of receipt", "p3")
            self.setEdge( "T06 Determine necessity of stop advice", "p3")
                    
            self.setEdge( "p3", "tau from tree2")
            self.setEdge( "p3", "tau from tree3")
            self.setEdge( "p3", "T07-1 Draft intern advice aspect 1")
            self.setEdge( "p3", "T07-3 Draft intern advice hold for aspect 3")
            self.setEdge( "p3", "T07-4 Draft internal advice to hold for type 4")
                    
            self.setEdge( "T07-1 Draft intern advice aspect 1", "p8")
            self.setEdge( "T07-3 Draft intern advice hold for aspect 3", "p8")
            self.setEdge( "T07-4 Draft internal advice to hold for type 4", "p8")
                    
            self.setEdge( "tau from tree2", "p4")
            self.setEdge( "p4", "tau from tree4")
            self.setEdge( "tau from tree4", "p6")
                    
            self.setEdge( "p6", "tau from tree6")
            self.setEdge( "p6", "T07-2 Draft intern advice aspect 2")
            self.setEdge( "p6", "T07-5 Draft intern advice aspect 5")
            self.setEdge( "p6", "T08 Draft and send request for advice")
            self.setEdge( "p6", "T09-1 Process or receive external advice from party 1")
            self.setEdge( "p6", "T09-2 Process or receive external advice from party 2")
            self.setEdge( "p6", "T09-3 Process or receive external advice from party 3")
            self.setEdge( "p6", "T09-4 Process or receive external advice from party 4")
                    
            self.setEdge( "tau from tree6", "p8")
            self.setEdge( "T07-2 Draft intern advice aspect 2", "p4")
            self.setEdge( "T07-5 Draft intern advice aspect 5", "p4")
            self.setEdge( "T08 Draft and send request for advice", "p4")
            self.setEdge( "T09-1 Process or receive external advice from party 1", "p4")
            self.setEdge( "T09-2 Process or receive external advice from party 2", "p4")
            self.setEdge( "T09-3 Process or receive external advice from party 3", "p4")
            self.setEdge( "T09-4 Process or receive external advice from party 4", "p4")
                    
            self.setEdge( "tau from tree3", "p5")
            self.setEdge( "p5", "tau from tree5")
            self.setEdge( "tau from tree5", "p7")
                    
            self.setEdge( "p7", "tau from tree7")
            self.setEdge( "p7", "T04 Determine confirmation of receipt")
            self.setEdge( "p7", "T05 Print and send confirmation of receipt")
            self.setEdge( "p7", "T10 Determine necessity to stop indication")
            self.setEdge( "p7", "T11 Create document X request unlicensed")
            self.setEdge( "p7", "T12 Check document X request unlicensed")
            self.setEdge( "p7", "T14 Determine document X request unlicensed")
            self.setEdge( "p7", "T16 Report reasons to hold request")
            self.setEdge( "p7", "T17 Check report Y to stop indication")
            self.setEdge( "p7", "T18 Adjust report Y to stop indicition")
            self.setEdge( "p7", "T19 Determine report Y to stop indication")
            self.setEdge( "p7", "T20 Print report Y to stop indication")
                        
            self.setEdge( "tau from tree7", "p8")
            self.setEdge( "T04 Determine confirmation of receipt", "p5")
            self.setEdge( "T05 Print and send confirmation of receipt", "p5")
            self.setEdge( "T10 Determine necessity to stop indication", "p5")
            self.setEdge( "T11 Create document X request unlicensed", "p5")
            self.setEdge( "T12 Check document X request unlicensed", "p5")
            self.setEdge( "T14 Determine document X request unlicensed", "p5")
            self.setEdge( "T16 Report reasons to hold request", "p5")
            self.setEdge( "T17 Check report Y to stop indication", "p5")
            self.setEdge( "T18 Adjust report Y to stop indicition", "p5")
            self.setEdge( "T19 Determine report Y to stop indication", "p5")
            self.setEdge( "T20 Print report Y to stop indication", "p5")
                    
            self.setEdge( "p8", "tau from tree8")
            self.setEdge( "p8", "tau from tree9")
                    
            self.setEdge( "tau from tree9", "p3")
            self.setEdge( "tau from tree8", "p9")
            self.setEdge( "p9", "tau from tree10")
            self.setEdge( "p9", "T03 Adjust confirmation of receipt")
                    
            self.setEdge( "tau from tree10", "p10")
            self.setEdge( "T03 Adjust confirmation of receipt", "p10")
                    
            self.setEdge( "p10", "tau from tree11")
            self.setEdge( "p10", "tau from tree12")
            self.setEdge( "tau from tree12", "p2")
            self.setEdge( "tau from tree11", "p11")
                    
            self.setEdge( "p11", "tau from tree13")
            self.setEdge( "p11", "T15 Print document X request unlicensed")
                    
            self.setEdge( "tau from tree13", "p12")
            self.setEdge( "T15 Print document X request unlicensed", "p12")
             
            self.setToken("p1")    
## CleanTD =  {  'confirmation of receipt': [],
# 't15 print document x request unlicensed': [['t10 determine necessity to stop indication']]  ,
# 't14 determine document x request unlicensed': [['t04 determine confirmation of receipt' ,  't02 check confirmation of receipt']] ,
# 't10 determine necessity to stop indication' : [['t06 determine necessity of stop advice']] ,   
# 't19 determine report y to stop indication' : [['t17 check report y to stop indication']] ,
# 't20 print report y to stop indication': [['t19 determine report y to stop indication']] , 
# 't18 adjust report y to stop indicition': [['t17 check report y to stop indication']] ,
# 't12 check document x request unlicensed' : [['t11 create document x request unlicensed']] ,
# 't17 check report y to stop indication' : [['t16 report reasons to hold request'], ['*t18 adjust report y to stop indicition']] ,
# 't03 adjust confirmation of receipt' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't07-1 draft intern advice aspect 1' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't07-2 draft intern advice aspect 2' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't07-3 draft intern advice hold for aspect 3' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't07-4 draft internal advice to hold for type 4' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't07-5 draft intern advice aspect 5' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't08 draft and send request for advice' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't09-1 process or receive external advice from party 1' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't09-2 process or receive external advice from party 2' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't09-3 process or receive external advice from party 3' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't09-4 process or receive external advice from party 4' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't11 create document x request unlicensed' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't16 report reasons to hold request' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
# 't02 check confirmation of receipt' : [['t12 check document x request unlicensed'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'],  ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'],  ['*t09-4 process or receive external advice from party 4'], ['*t03 adjust confirmation of receipt'], ['confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'], ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['t20 print report y to stop indication']] ,   
# 't04 determine confirmation of receipt' : [['t12 check document x request unlicensed'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'],  ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'],  ['*t09-4 process or receive external advice from party 4'], ['*t03 adjust confirmation of receipt'], ['confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'], ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['t20 print report y to stop indication']] ,   
# 't05 print and send confirmation of receipt' : [ ['t04 determine confirmation of receipt' , 't02 check confirmation of receipt'],  ['t14 determine document x request unlicensed']],  
# 't06 determine necessity of stop advice' : [ ['t04 determine confirmation of receipt' , 't02 check confirmation of receipt'],  ['t14 determine document x request unlicensed'], ['t05 print and send confirmation of receipt']] }  

   
###########################################################################
# Sepsis Cases example -- 2016
# 
#             self.addTransition("ER Registration")
#             self.addTransition("Leucocytes")
#             self.addTransition("CRP")
#             self.addTransition("LacticAcid")
#             self.addTransition("ER Triage")
#             self.addTransition("ER Sepsis Triage")
#             self.addTransition("IV Liquid")
#             self.addTransition("IV Antibiotics")
#             self.addTransition("Admission NC")
#             self.addTransition("Release A")
#             self.addTransition("Return ER")
#             self.addTransition("Admission IC")
#             self.addTransition("Release B")
#             self.addTransition("Release C")
#             self.addTransition("Release D")
#             self.addTransition("Release E")
#             self.addTransition("tau1")
#             self.addTransition("tau2")
#             self.addTransition("tau3")
#             self.addTransition("tau4")
#             self.addTransition("tau5")
#             self.addTransition("tau6")
#             self.addTransition("tau7")
#             self.addTransition("tau8")
#             self.addTransition("tau9")
#             self.addTransition("tau10")
#             self.addTransition("tau11")
#             self.addTransition("tau12")
#             self.addTransition("tau13")
#             self.addTransition("tau14")
#             self.addTransition("tau15")
#             self.addTransition("tau16")
#             self.addTransition("tau17")
#             self.addTransition("tau18")
#             self.addTransition("tau19")
#                
#             self.addPlace("p1")
#             self.addPlace("p2")
#             self.addPlace("p3")
#             self.addPlace("p4")
#             self.addPlace("p5")
#             self.addPlace("p6")
#             self.addPlace("p7")
#             self.addPlace("p8")
#             self.addPlace("p9")
#             self.addPlace("p10")
#             self.addPlace("p11")
#             self.addPlace("p12")
#             self.addPlace("p13")
#             self.addPlace("p14")
#             self.addPlace("p15")
#             self.addPlace("p16")
#             self.addPlace("p17")
#             self.addPlace("p18")
#                
#                
#             self.setEdge("p1", "IV Liquid")
#             self.setEdge("p1", "CRP")
#             self.setEdge("p1", "ER Triage")
#             self.setEdge("p1", "Leucocytes")
#             self.setEdge("p1", "ER Sepsis Triage")
#             self.setEdge("p1", "ER Registration")
#                
#             self.setEdge("IV Liquid", "p2")
#             self.setEdge("CRP", "p2")
#             self.setEdge("ER Triage", "p2")
#             self.setEdge("Leucocytes", "p2")
#             self.setEdge("ER Sepsis Triage", "p2")
#             self.setEdge("ER Registration", "p2")
#                
#             self.setEdge("p2", "tau1")
#             self.setEdge("tau1", "p16")
#                
#             self.setEdge("p2", "tau2")
#             self.setEdge("tau2", "p3")
#             self.setEdge("tau2", "p4")
#                
#             self.setEdge("p3", "tau3")
#             self.setEdge("p3", "tau4")
#             self.setEdge("tau3", "p5")
#             self.setEdge("tau4", "p6")
#             self.setEdge("p5", "LacticAcid")
#             self.setEdge("p6", "tau5")
#             self.setEdge("tau5", "p13")
#             self.setEdge("p6", "tau6")
#             self.setEdge("p6", "IV Antibiotics")
#             self.setEdge("LacticAcid", "p8")
#             self.setEdge("p8", "tau9")
#             self.setEdge("p8", "tau10")
#             self.setEdge("tau9", "p11")
#             self.setEdge("p11", "tau13")
#             self.setEdge("p13", "tau13")
#             self.setEdge("tau6", "p9")
#             self.setEdge("IV Antibiotics", "p9")
#             self.setEdge("p9", "Admission IC")
#             self.setEdge("Admission IC", "p12")
#             self.setEdge("p12", "tau11")
#             self.setEdge("tau11", "p9")
#             self.setEdge("p12", "tau12")
#             self.setEdge("tau12", "p13")
#             self.setEdge("tau4", "p14")
#             self.setEdge("tau13", "p14")
#             self.setEdge("p14", "Release B")
#             self.setEdge("p14", "tau14")
#             self.setEdge("Release B", "p15")
#             self.setEdge("tau14", "p15")
#             self.setEdge("p15", "tau15")
#             self.setEdge("tau15", "p16")
#                
#                
#             self.setEdge("p4", "Admission NC")
#             self.setEdge("Admission NC", "p7")
#             self.setEdge("p7", "tau7")
#             self.setEdge("p7", "tau8")
#             self.setEdge("tau7", "p10")
#             self.setEdge("tau8", "p4")
#             self.setEdge("p10", "tau15")
#                
#                
#             self.setEdge("p16", "tau16")
#             self.setEdge("p16", "Release A")
#             self.setEdge("p16", "Release C")
#             self.setEdge("p16", "Release E")
#             self.setEdge("p16", "Release D")
#                
#             self.setEdge("tau16", "p17")
#             self.setEdge("Release A", "p17")
#             self.setEdge("Release C", "p17")
#             self.setEdge("Release E", "p17")
#             self.setEdge("Release D", "p17")
#                
#             self.setEdge("p17", "Return ER")
#             self.setEdge("p17", "tau17")
#             self.setEdge("Return ER", "p18")
#             self.setEdge("tau17", "p18")
             
            # The rest of petri net is ignored due to self loop on the start activities, which will cause problems.
             
#             self.setEdge("p18", "tau18")
#             self.setEdge("p18", "tau19")
#             
#             self.setEdge("tau18", "p19")
#             self.setEdge("tau19", "p1")
#             
#             self.setToken("p1")
             
####################################################################
###################################################################
#
#    # BPI Challenge 2017 -- Inductive miner --> zero noise
#    ##BPI Challenge 2017    -- description 1,202,267 events - 31509 traces/cases  --> actual 561,672 events
#    ##Final list of dependencies =  {'tau6': [['*tau4'], ['*a_accepted']], 'o_create offer': [['tau3']], 'a_complete': [['tau3']], 'tau3': [['a_complete'], ['w_assess potential fraud'], ['w_validate application'], ['w_call after offers'], ['a_cancelled'], ['o_returned'], ['a_incomplete'], ['o_created'], ['a_pending'], ['w_call incomplete files'], ['w_shortened completion'], ['o_sent (mail and online)'], ['a_validating'], ['o_create offer'], ['o_sent (online only)'], ['a_denied'], ['o_accepted'], ['o_cancelled'], ['w_personal loan collection'], ['*o_refused'], ['*tau2']], 'a_create application': [], 'w_complete application': [['a_submitted'], ['tau1'], ['*tau7']], 'w_call after offers': [['tau3']], 'tau4': [['tau3']], 'a_incomplete': [['tau3']], 'w_handle leads': [['a_submitted'], ['tau1'], ['*tau7']], 'o_created': [['tau3']], 'o_sent (mail and online)': [['tau3']], 'a_submitted': [['a_create application']], 'w_validate application': [['tau3']], 'tau2': [['w_complete application'], ['w_handle leads'], ['a_concept'], ['*tau5']], 'tau1': [['a_create application']], 'o_refused': [['tau3']], 'tau5': [['*tau4'], ['*a_accepted']], 'w_shortened completion': [['tau3']], 'tau7': [['tau6']], 'o_accepted': [['tau3']], 'tau8': [['tau6']], 'a_cancelled': [['tau3']], 'a_pending': [['tau3']], 'w_call incomplete files': [['tau3']], 'o_returned': [['tau3']], 'a_validating': [['tau3']], 'a_concept': [['a_submitted'], ['tau1'], ['*tau7']], 'a_accepted': [['w_complete application'], ['w_handle leads'], ['*tau5'], ['*a_concept']], 'o_sent (online only)': [['tau3']], 'w_personal loan collection': [['tau3']], 'o_cancelled': [['tau3']], 'a_denied': [['tau3']], 'w_assess potential fraud': [['tau3']]}
#    #Clean dependencies =  {'tau6': [['*tau4'], ['*a_accepted']], 
#     'o_create offer': [['tau3']], 
#     'a_complete': [['tau3']], 
#     'tau3': [['a_complete'], ['w_assess potential fraud'], ['w_validate application'], ['w_call after offers'], ['a_cancelled'], ['o_returned'], ['a_incomplete'], ['o_created'], ['a_pending'], ['w_call incomplete files'], ['w_shortened completion'], ['o_sent (mail and online)'], ['a_validating'], ['o_create offer'], ['o_sent (online only)'], ['a_denied'], ['o_accepted'], ['o_cancelled'], ['w_personal loan collection'], ['*o_refused'], ['*tau2']], 
#     'a_create application': [], 
#     'w_complete application': [['a_submitted'], ['tau1'], ['*tau7']], 
#     'w_call after offers': [['tau3']], 
#     'tau4': [['tau3']], 
#     'a_incomplete': [['tau3']], 
#     'w_handle leads': [['a_submitted'], ['tau1'], ['*tau7']], 
#     'o_created': [['tau3']], 
#     'o_sent (mail and online)': [['tau3']], 
#     'a_submitted': [['a_create application']], 
#     'w_validate application': [['tau3']], 
#     'tau2': [['w_complete application'], ['w_handle leads'], ['a_concept'], ['*tau5']], 
#     'tau1': [['a_create application']], 
#     'o_refused': [['tau3']], 
#     'tau5': [['*tau4'], ['*a_accepted']], 
#     'w_shortened completion': [['tau3']], 
#     'tau7': [['tau6']], 
#     'o_accepted': [['tau3']], 
#     'tau8': [['tau6']], 
#     'a_cancelled': [['tau3']], 
#     'a_pending': [['tau3']], 
#     'w_call incomplete files': [['tau3']], 
#     'o_returned': [['tau3']], 
#     'a_validating': [['tau3']], 
#     'a_concept': [['a_submitted'], ['tau1'], ['*tau7']], 
#     'a_accepted': [['w_complete application'], ['w_handle leads'], ['*tau5'], ['*a_concept']], 
#     'o_sent (online only)': [['tau3']], 
#     'w_personal loan collection': [['tau3']], 
#     'o_cancelled': [['tau3']], 
#     'a_denied': [['tau3']], 
#     'w_assess potential fraud': [['tau3']]}
#
#             self.addTransition("A_Create Application")     #t01
#             self.addTransition("A_Submitted")     #t02
#             self.addTransition("W_Complete application")     #t03
#             self.addTransition("W_Handle leads")     #t04
#             self.addTransition("A_Concept")     #t05
#             self.addTransition("A_Accepted")     #t06
#             self.addTransition("A_Complete")     #t07
#             self.addTransition("W_Assess potential fraud")     #t08
#             self.addTransition("O_Refused")     #t09
#             self.addTransition("W_Validate application")     #t10
#             self.addTransition("W_Call after offers")     #t11
#             self.addTransition("A_Cancelled")     #t12
#             self.addTransition("O_Returned")     #t13
#             self.addTransition("A_Incomplete")     #t14
#             self.addTransition("O_Created")     #t15
#             self.addTransition("A_Pending")     #t16
#             self.addTransition("W_Call incomplete files")     #t17
#             self.addTransition("W_Shortened completion")     #t18
#             self.addTransition("O_Sent (mail and online)")     #t19
#             self.addTransition("A_Validating")     #t20
#             self.addTransition("O_Create Offer")     #t21 
#             self.addTransition("O_Sent (online only)")     #t22
#             self.addTransition("A_Denied")     #t23
#             self.addTransition("O_Accepted")     #t24
#             self.addTransition("O_Cancelled")     #t25
#             self.addTransition("W_Personal Loan collection")     #t26
#             self.addTransition("tau1")
#             self.addTransition("tau2")
#             self.addTransition("tau3")
#             self.addTransition("tau4")
#             self.addTransition("tau5")
#             self.addTransition("tau6")
#             self.addTransition("tau7")
#             self.addTransition("tau8")
#                 
#             self.addPlace("p1")
#             self.addPlace("p2")
#             self.addPlace("p3")
#             self.addPlace("p4")
#             self.addPlace("p5")
#             self.addPlace("p6")
#             self.addPlace("p7")
#             self.addPlace("p8")
#             self.addPlace("p9")
#           
#                 
#             self.setEdge("p1","A_Create Application")
#             self.setEdge("A_Create Application","p2")
#             self.setEdge("p2","A_Submitted")
#             self.setEdge("p2","tau1")
#             self.setEdge("A_Submitted","p3")
#             self.setEdge("tau1","p3")
#             self.setEdge("p3","W_Complete application")
#             self.setEdge("p3","W_Handle leads")
#             self.setEdge("p3","A_Concept")
#             self.setEdge("W_Complete application","p4")
#             self.setEdge("W_Handle leads","p4")
#             self.setEdge("A_Concept","p4")
#             self.setEdge("p4","A_Accepted")
#             self.setEdge("p4","tau2")
#             self.setEdge("A_Accepted","p7")
#             self.setEdge("tau2","p5")
#             self.setEdge("p5","tau3")
#             self.setEdge("tau3","p6")
#             self.setEdge("p6","A_Complete")
#             self.setEdge("p6","W_Assess potential fraud")
#             self.setEdge("p6","O_Refused")
#             self.setEdge("p6","W_Validate application")
#             self.setEdge("p6","W_Call after offers")
#             self.setEdge("p6","A_Cancelled")
#             self.setEdge("p6","O_Returned")
#             self.setEdge("p6","A_Incomplete")
#             self.setEdge("p6","O_Created")
#             self.setEdge("p6","A_Pending")
#             self.setEdge("p6","W_Call incomplete files")
#             self.setEdge("p6","W_Shortened completion")
#             self.setEdge("p6","O_Sent (mail and online)")
#             self.setEdge("p6","A_Validating")
#             self.setEdge("p6","O_Create Offer")
#             self.setEdge("p6","O_Sent (online only)")
#             self.setEdge("p6","A_Denied")
#             self.setEdge("p6","O_Accepted")
#             self.setEdge("p6","O_Cancelled")
#             self.setEdge("p6","W_Personal Loan collection")
#             self.setEdge("A_Complete","p5")
#             self.setEdge("W_Assess potential fraud","p5")
#             self.setEdge("O_Refused","p5")
#             self.setEdge("W_Validate application","p5")
#             self.setEdge("W_Call after offers","p5")
#             self.setEdge("A_Cancelled","p5")
#             self.setEdge("O_Returned","p5")
#             self.setEdge("A_Incomplete","p5")
#             self.setEdge("O_Created","p5")
#             self.setEdge("A_Pending","p5")
#             self.setEdge("W_Call incomplete files","p5")
#             self.setEdge("W_Shortened completion","p5")
#             self.setEdge("O_Sent (mail and online)","p5")
#             self.setEdge("A_Validating","p5")
#             self.setEdge("O_Create Offer","p5")
#             self.setEdge("O_Sent (online only)","p5")
#             self.setEdge("A_Denied","p5")
#             self.setEdge("O_Accepted","p5")
#             self.setEdge("O_Cancelled","p5")
#             self.setEdge("W_Personal Loan collection","p5")
#             self.setEdge("p6","tau4")
#             self.setEdge("tau4","p7")
#             self.setEdge("p7","tau5")
#             self.setEdge("tau5","p4")
#             self.setEdge("p7","tau6")
#             self.setEdge("tau6","p8")
#             self.setEdge("p8","tau7")
#             self.setEdge("tau7","p3")
#             self.setEdge("p8","tau8")
#             self.setEdge("tau8","p9")
#                     
#             self.setToken("p1")

###################################################################


            self.buildNet()
            self.buildModel() # This step is to provide model logic for GraphModel application
            '''
            self.dependenciesTransition={'a':[],
                                         'b':[['a'],['*n']],
                                         'c':[['b']],
                                         'd':[['b']],
                                         'e':[['d'],['*h']],
                                         'f':[['e']],
                                         'g':[['e']],
                                         'h':[['f']],
                                         'i':[['c']],
                                         'j':[['c']],
                                         'k':[['i','j']],
                                         'l':[['g'],['k']],
                                         'm':[['l']],
                                         'n':[['l']]}
            
            
            '''
            #print('Places = ',self.getPlaces())
            #print('Transitions = ',self.getTransitions())
            #print('Edges = ',self.edges)
            #print('Start place = ',self.getStartPlace())
            #print('Start transition (activity) = ',self.getStartTransition())
            #print('\nprevTransitions (undiscovered loops) = ',self.getPrevTransitions()) # This is mainly coming from Petri net
            #print('Model dependencies (with loops) = ',self.getModelDependencies()) # with new logic of loop
            #'''
            
            '''
            loopTransition = 'b'
            loopString ='*'+loopTransition 
            #print('loopString',loopString, ' loopString[0]=',loopString[0], ' loopString[1:]=',loopString[1:],' loopString[:-1]=',loopString[:-1])
            #print('isTransition of loopString[1:] ',self.isTransition(loopString[1:]))
            '''    



    
################################################################################
    def addPlace(self,name):
        if (not self.isPlace(name)):
            inputTransitions=[]
            outputTransitions=[]
            self.places[name]=(inputTransitions,outputTransitions)
            return
        else:
            errorMsg='Error: duplicate addition of place '+name
            raise NameError(errorMsg)
################################################################################
    def isPlace(self,name):
        if not name in self.places.keys():
            return False
        return True
        
################################################################################        
    def addTransition(self,name):
        name=name.lower()
        if (not self.isTransition(name)):
            inputPlaces=[]
            outputPlaces=[]
            self.transitions[name]=(inputPlaces,outputPlaces)
            self.prevTransitions[name]=[]
            self.nextTransitions[name]=[]
            return
        else:
            errorMsg='Error: duplicate addition of transition '+name
            raise NameError(errorMsg)
################################################################################        
    def isTransition(self,name):
        name=name.lower()
        if not name in self.places.keys():
            return False
        return True
        
################################################################################        
    def setEdge(self,v1,v2):
        # note that name of places are transitions are unique in the self
        v1=v1.lower()
        v2=v2.lower()
        
        if(not self.isEdge(v1, v2)):
            self.edges.append((v1,v2))
            for p in self.places:
                if (p == v1):
                    self.places[p][1].append(v2) # add output transition
                    break
                if (p == v2):
                    self.places[p][0].append(v1) # add input transition
                    break
            for t in self.transitions:
                if (t == v1):
                    self.transitions[t][1].append(v2) # add output place
                    break
                if (t == v2):
                    self.transitions[t][0].append(v1) # add input place
                    break
            return
        else:
            errorMsg = 'Error: duplicate addition of edge ('+v1+','+v2+')'
            raise NameError(errorMsg)
    
################################################################################
    def isEdge(self,n1,n2):
        for (v1,v2) in self.edges:
            if (v1==n1 and v2==n2):
                return True
        return False
    
################################################################################        
    def getTransitions(self):
        return self.transitions
################################################################################        
    def getPlaces(self):
        return self.places

################################################################################        
    def setToken(self,name):
        for p in self.places:
            if(p[1] == name):
                p[2] = [] # i.e. input transitions are null (start activity)
                break
        return
################################################################################        
    def getStartPlace(self):
        for p in self.places:
            if(self.places[p][0] == []):
                return p
        return  
        
################################################################################        
    def getStartTransition(self):
        startPlaces=[]
        for p in self.places:
            if(self.places[p][0] == []):
                for t in self.places[p][1]:
                    startPlaces.append(t)
        return startPlaces
        
#        #Returning singular start place
#         for p in self.places:
#             if(self.places[p][0] == []):
#                 for t in self.places[p][1]:
#                     return t
#         return
################################################################################
    # Returns list of transitions (i.e. activities)        
    def getTransitionsList(self):
        transitionList=[]
        for t in self.transitions:
            transitionList.append(t)
        return transitionList
 
################################################################################        
    # It preserves the concept of XOR, AND --> but not distinguishing loops
    # Originally built from Petri net
    def buildNet(self):
        for t in self.transitions:
            prevPlaces = self.transitions[t][0]
            nextPlaces = self.transitions[t][1]
            if(len(prevPlaces)>1):
                andPlaces = [p for p in prevPlaces]
                #self.prevTransitions[t]= [self.places[x][0] for x in andPlaces]
                for x in andPlaces:
                    self.prevTransitions[t] += self.places[x][0]
                self.prevTransitions[t] = [self.prevTransitions[t]]
                ##print ('self.prevTransitions[t] =1= ',self.prevTransitions[t] )
            elif (len(prevPlaces)==1):
                xorPlaces = self.places[prevPlaces[0]][0]
                self.prevTransitions[t]= [[x] for x in xorPlaces] 
                ##print ('self.prevTransitions[t] =2= ',self.prevTransitions[t] )
            else: # for exception handling
                self.prevTransitions[t]= []
                ##print ('self.prevTransitions[t] =3= ',self.prevTransitions[t] )
                
            if(len(nextPlaces)>1):
                andPlaces = [p for p in nextPlaces]
                #self.prevTransitions[t]= [self.places[x][0] for x in andPlaces]
                for x in andPlaces:
                    self.nextTransitions[t] += self.places[x][1]
                self.nextTransitions[t] = [self.nextTransitions[t]]
                ##print ('self.prevTransitions[t] =1= ',self.prevTransitions[t] )
            elif (len(nextPlaces)==1):
                xorPlaces = self.places[nextPlaces[0]][1]
                self.nextTransitions[t]= [[x] for x in xorPlaces] 
                ##print ('self.prevTransitions[t] =2= ',self.prevTransitions[t] )
            else: # for exception handling
                self.prevTransitions[t]= []
                ##print ('self.prevTransitions[t] =3= ',self.prevTransitions[t] )
        return
################################################################################        
    def getPrevTransitions(self):
        return self.prevTransitions


################################################################################        
    def getNextTransitions(self):
        return self.nextTransitions
        
################################################################################        
    # It preserves the concept of XOR, AND 
    # It discovers entry transitions of loops using depth-first
    # Note: Pure Petri net doesn't support self loop without dummy transition.
    # It will use self.prevTransitions to update self.dependenciesTransition
    def buildModel(self):
        
        # Initialize self.dependenciesTransition with singular dependencies of transitions
        
        #visitingTransitions = [x for x in self.prevTransitions]
        ##print('visitingTransitions updated =0= ',visitingTransitions)
        
        visitingTranGraph = dict()
        #startingTransition = []
        startingTransition = self.getStartTransition()
        for x in self.prevTransitions:
            visitingTranGraph[x] = self.prevTransitions[x]  
        #print('visitingTransGraph updated =0= ',visitingTranGraph)
        
        for t in self.prevTransitions:
            if len(self.prevTransitions[t])==1:
                self.dependenciesTransition[t]=self.prevTransitions[t]
                visitingTranGraph.pop(t)
                #visitingTransitions.remove(t)
            elif t in startingTransition: # t == startingTransition:
                self.dependenciesTransition[t]=[]
                visitingTranGraph.pop(t)
                #visitingTransitions.remove(t)
                
        #print('self.dependenciesTransition initially = ',self.dependenciesTransition)
        #print('visitingTranGraph updated =1= to check XOR? ',visitingTranGraph)
        ##print('visitingTransitions updated =1= to check XOR? ',visitingTransitions)
        
        # have to be done using recursion function - dynamic programming
        for x,y in self.edges:self.make_link(self.G,x,y) #constructing the graph using tuple representation
        #print ('Graph = ',self.G)
        myVisitG = {}
        for t in visitingTranGraph:
            if t not in myVisitG :
                myVisitG [t]={}
            listOfInputs = [num for elem in visitingTranGraph[t] for num in elem] #self.prevTransitions[t]
        
            for inputT in listOfInputs:
                (myVisitG [t])[inputT]=1  # build JSON
                
        #print('final myVisitG = ',myVisitG)
        
        myG = {}
        for t in self.prevTransitions:
            if t not in myG:
                myG[t]={}
            listOfInputs = [num for elem in self.prevTransitions[t] for num in elem] 
        
            for inputT in listOfInputs:
                (myG[t])[inputT]=1  # build JSON
         
        #print('final myG = ',myG)

        myReversedG = {}
        for t in self.prevTransitions:
            if t not in myReversedG:
                myReversedG[t]={}
            listOfOutputs = [num for elem in self.nextTransitions[t] for num in elem] 
        
            for outputT in listOfOutputs:
                (myReversedG[t])[outputT]=1  # build JSON
         
        #print('final myReversedG = ',myReversedG)
        #self.reversedG = myReversedG
        
        #print('start time',datetime.now().strftime('%m/%d/%Y  %I:%M:%S %p'))
        
        if len(visitingTranGraph)>=1:
            self.start_traversal(myG,myVisitG , myReversedG)
        
        #print('finish time',datetime.now().strftime('%m/%d/%Y  %I:%M:%S %p'))
        
        return self.dependenciesTransition 
        

################################################################################
    def myDFS(self,visitingTransitions,startTransition):
        return self.dependenciesTransition

################################################################################
    ### Something wrong in visited - graph
    def dfs1(self,graph, start):
        #graph here == visitingTranGraph coming from BuildModel
        visited, stack = set(), [start] #list()
        #print('DFS - graph ',graph, ' visited ',visited,' stack ',stack,' start ',[start])
        
        while stack:
            vertex = stack.pop()
            #print('in stack - vertex = ',vertex)
            if vertex not in visited:
                #print('inside not in visited -- visited is 1 ',visited, ' graph is ',graph)
                visited.add(vertex)
                #print('inside not in visited -- visited is 2 ',visited, ' graph is ',graph)
                #visited.append(vertex)
                stack.extend(graph[vertex] - visited)
        return visited



################################################################################
# This code is from: https://www.hackerearth.com/notes/karthic-hackintosh/simple-explanation-of-implementation-of-dfs-in-python/
    
    
    def dfs(self,G,node,traversed,observingNode):
        traversed[node] = 1
        #print ('traversal:'+ node) 
        for neighbour_nodes in G[node]: #take a neighbouring node
            if neighbour_nodes not in traversed: #condition to check whether the neighbour node is already visited
                self.dfs(G,neighbour_nodes,traversed,observingNode) #recursively traverse the neighbouring node
            else: 
                if neighbour_nodes == observingNode: # revisiting same observing node
                    traversed[node] =traversed[node] +1
                    #print('===updated traversed['+node+']',traversed[node],'*'+node)
                    

################################################################################
# This code is from: https://www.hackerearth.com/notes/karthic-hackintosh/simple-explanation-of-implementation-of-dfs-in-python/
    def start_traversal(self,G,checking, ReversedG):
        #print('-----------------------Start Traversal-----------------------------')
        traversed = dict() #dictionary to mark the traversed nodes
        counter = 0
        for node in checking.keys(): #G.keys() returns a node from the graph in its iteration
            observingNode = node
            counter +=1
            #print('observal # ',counter,' observing node '+observingNode)
            traversed.clear() # to check each suspicious XOR for loops
            if node not in traversed: #you start traversing from the root node only if its not visited 
                self.dfs(G,node,traversed,observingNode); #for a connected graph this is called only once
                #print('check happened with ',node, ' traveresed = ',traversed)
                loopList = []
                multiVisit = []
                xorList = self.prevTransitions[observingNode]
                #print('xorList 1==',xorList)
                for i in traversed:
                    if traversed[i]==2:
                        multiVisit.append(i)
                        if self.prevTransitions[i] == observingNode: # a-k-a self loop
                            loopList.append(['*'+i])
                            #print('loop in '+i+' for observed node '+observingNode)
                            xorList.remove([i])
                            #print('xorList 2==',xorList)
                        else: # branch loop
                            traverseLoop=dict()
                            #print('----------check loops for node = ',observingNode,'--------------')
                            #loopList = 
                            self.getLoopEntryTransitionMod(i,observingNode,G,ReversedG,loopList, xorList,traverseLoop)
                            #loopList = self.getLoopEntryTransition(i,observingNode,G,loopList, xorList)
                            #print('final loopList ',loopList)
                            #print('final xorList -- temp -- ',xorList)
                if not loopList:
                    loopList=[]
                if not xorList:
                    xorList=[]
                self.dependenciesTransition[observingNode]=xorList+loopList        
                ##print('----------------------------------------------------')
        
        ##print('traveresed finally is == ',traversed)
        #print('Final list of dependencies = ',self.dependenciesTransition)
        
################################################################################

    def getLoopEntryTransitionMod(self, node, targetNode, G, ReversedG, loopList, xorList,traverseLoop):
        #print('I am checking '+node)
        #print('condition: checkNode=',node,', in G[',targetNode,']=',G[targetNode])
        # to mark which was checked for loop entry
        traverseLoop[node] = 1
        
        #print('+++ ReversedG[',node,'] = ',ReversedG[node])
        #print('+++ traverseLoop = ',traverseLoop)
        for neighbour_nodes in ReversedG[node]: #take a neighbouring node
            if neighbour_nodes in G[targetNode]: # found as dependent of targetNode
                if neighbour_nodes not in traverseLoop:
                    traverseLoop[neighbour_nodes]=1
                    ##print('+++ traverseLoop[',neighbour_nodes,'] = ',traverseLoop[neighbour_nodes])
                else:
                    traverseLoop[neighbour_nodes] = traverseLoop[neighbour_nodes]+1
                #print('+++ traverseLoop[',neighbour_nodes,'] = ',traverseLoop[neighbour_nodes])
                #print('*loop entry is '+neighbour_nodes)
                if([neighbour_nodes] in xorList):
                    xorList.remove([neighbour_nodes])
                    #print('xorList in recursion ',xorList)
                    if loopList is None:
                        loopList=['*'+neighbour_nodes]
                    else:
                        loopList.append(['*'+neighbour_nodes])
                #print('xorList = ',xorList,' *loopList ',loopList)
                #break
                return #loopList #checkNode
            else:
                if neighbour_nodes not in traverseLoop: #condition to check whether the neighbour node is already visited
                    self.getLoopEntryTransitionMod(neighbour_nodes, targetNode, G, ReversedG, loopList, xorList,traverseLoop)
                
                    
            '''                    
            #i.e. not direct loop, need to get its root
            nextTransitions = self.getNextTransition(checkNode)#[num for elem in self.prevTransitions[checkNode] for num in elem] 
            #itertools.chain.from_iterable(self.prevTransitions[checkNode])
            #print('===in else checkNode=',checkNode,', check on next transitions =',nextTransitions)
            for n in nextTransitions:
                #print ('>>> checking ', n,' in nextTransitions ')
                #print('>>> traverseLoop == ',traverseLoop)
                if (traverseLoop is not None): #   and traversed[n]>=1 #  and flag is not True
                    #print('>>> traverseLoop[',n,'] == ',traverseLoop[n])
                    traverseLoop[n] = traverseLoop[n]+1
                    if(n in traverseLoop): # indicates insider loop
                        neighborNodes = G[n]
                    else:
                        self.getLoopEntryTransitionMod(n, targetNode, G, loopList, xorList,traverseLoop)
                else:
                    traverseLoop[n]=1
                    self.getLoopEntryTransitionMod(n, targetNode, G, loopList, xorList,traverseLoop)
            '''                


################################################################################
# Old one with infinite loop
#Problem infinite loop, because of smaller loop inside the big loop

    def getLoopEntryTransition(self, checkNode, targetNode, G, loopList, xorList):
        #print('I am checking '+checkNode)
        #print('condition: ',checkNode,' in G[',targetNode,']',G[targetNode])
        flag=False
        if checkNode in G[targetNode]:
            #print('*loop entry is '+checkNode)
            if([checkNode] in xorList):
                xorList.remove([checkNode])
                #print('xorList in recursion ',xorList)
                loopList.append(['*'+checkNode])
                flag=True
            #print('*loopList ',loopList)
            return #loopList #checkNode
        if flag == True:
            return loopList
        else:
            nextTransitions = self.getNextTransition(checkNode)#[num for elem in self.prevTransitions[checkNode] for num in elem] 
            #itertools.chain.from_iterable(self.prevTransitions[checkNode])
            #print('===in else ',checkNode,' check on next transitions ',nextTransitions)
            for n in nextTransitions:
                    #print('G = ',G) #Problem infinite loop, because of smaller loop inside the big loop
                    self.getLoopEntryTransition(n, targetNode, G, loopList, xorList)
                    


################################################################################                
    def getNextTransition(self,checkNode):
        nextTransitions=[]
        for a,b in self.edges:
            if a == checkNode:
                nextTransitions+=self.places[b][1]  
        return nextTransitions
################################################################################
# This code is from: https://www.hackerearth.com/notes/karthic-hackintosh/simple-explanation-of-implementation-of-dfs-in-python/
    def make_link(self,g,node1,node2): #function to construct the graph in JSOn like format 
        if node1 not in self.G:
            self.G[node1]={}
        (self.G[node1])[node2]=1
        if node2 not in self.G:
            self.G[node2]={}
        (self.G[node2])[node1]=1
        
################################################################################
    def getModelDependencies(self):
        c = CleanTD()
        for oT in  c.originalDependencies.keys():
            ###print('oT = ',oT)
            if 'tau' in oT:
                continue
            oTDependents = c.originalDependencies[oT]
            ###print('|--- oTDependents = ',oTDependents, ' its length = ',len(oTDependents))
            newParentDependencies = c.removeTau3(oTDependents)
            ###print('newParentDependencies ',newParentDependencies, ' oTDependents', oTDependents)
            c.finalDependencies[oT]=oTDependents #newParentDependencies
            ###print('|--- c.finalDependencies[',oT,'] = ',c.finalDependencies[oT])
         
        ###print('Final Dependencies =', c.finalDependencies)
        self.dependenciesTransition.clear()
        for key in c.finalDependencies.keys():
            self.dependenciesTransition[key]= c.finalDependencies[key] 
        return c.finalDependencies    #self.dependenciesTransition

    def getModelDependencies2(self):
#         cDependencies = {'completed': [], 'queued': [[],['*queued']], 'accepted': [[],['*accepted']]} # incidents
#         cDependencies ={'queued': [['accepted']], 'accepted': [[],['unmatched'], ['*queued']], 'completed': [['accepted']], 'unmatched': []} #closed - almost fully correlated (used in comparison)
#         cDependencies ={'queued': [['accepted']], 'accepted': [['unmatched'], ['*queued']], 'completed': [['accepted']], 'unmatched': []} # closed - nothing correlated
#         cDependencies = {'d': [['a'], ['*e']], 'g': [['c']], 'h': [['g'], ['f']], 'c': [['b']], 'e': [['b']], 'b': [['d']], 'f': [['b']], 'a': []} # loop-1000 (used in comparison)
#         cDependencies  = {'b': [['a'], ['*n']], 'f': [['e']], 'l': [['*g'], ['*i,*j']], 'n': [['l']], 'e': [['*d'], ['*h']], 'd': [['b']], 'i': [['c']], 'c': [['b']], 'g': [['e']], 'h': [['f']], 'm': [['l']], 'j': [['c']], 'a': []}# paper - hospital
        ##Wabo CoSeLoG - cleanTD  (to use in comparison)
        cDependencies =  {  'confirmation of receipt': [],
             't15 print document x request unlicensed': [['t10 determine necessity to stop indication']]  ,
             't14 determine document x request unlicensed': [['t04 determine confirmation of receipt' ,  't02 check confirmation of receipt']] ,
             't10 determine necessity to stop indication' : [['t06 determine necessity of stop advice']] ,   
             't19 determine report y to stop indication' : [['t17 check report y to stop indication']] ,
             't20 print report y to stop indication': [['t19 determine report y to stop indication']] , 
             't18 adjust report y to stop indicition': [['t17 check report y to stop indication']] ,
             't12 check document x request unlicensed' : [['t11 create document x request unlicensed']] ,
             't17 check report y to stop indication' : [['t16 report reasons to hold request'], ['*t18 adjust report y to stop indicition']] ,
             't03 adjust confirmation of receipt' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't07-1 draft intern advice aspect 1' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't07-2 draft intern advice aspect 2' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't07-3 draft intern advice hold for aspect 3' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't07-4 draft internal advice to hold for type 4' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't07-5 draft intern advice aspect 5' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't08 draft and send request for advice' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't09-1 process or receive external advice from party 1' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't09-2 process or receive external advice from party 2' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't09-3 process or receive external advice from party 3' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't09-4 process or receive external advice from party 4' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't11 create document x request unlicensed' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't16 report reasons to hold request' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['confirmation of receipt']] ,
             't02 check confirmation of receipt' : [['t12 check document x request unlicensed'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'],  ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'],  ['*t09-4 process or receive external advice from party 4'], ['*t03 adjust confirmation of receipt'], ['confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'], ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['t20 print report y to stop indication']] ,   
             't04 determine confirmation of receipt' : [['t12 check document x request unlicensed'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'],  ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'],  ['*t09-4 process or receive external advice from party 4'], ['*t03 adjust confirmation of receipt'], ['confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'], ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['t20 print report y to stop indication']] ,   
             't05 print and send confirmation of receipt' : [ ['t04 determine confirmation of receipt' , 't02 check confirmation of receipt'],  ['t14 determine document x request unlicensed']],  
             't06 determine necessity of stop advice' : [ ['t04 determine confirmation of receipt' , 't02 check confirmation of receipt'],  ['t14 determine document x request unlicensed'], ['t05 print and send confirmation of receipt']] }  
#
#            # Wabo CoSeLoG -- something incorrect.
#         cDependencies = {'a_create application': [],
#         'a_submitted': [['a_create application']], 
#         'w_complete application': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'w_handle leads': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'a_concept': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'a_accepted': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'a_complete': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'w_assess potential fraud': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'o_refused': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'w_validate application': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'w_call after offers': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'a_cancelled': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'o_returned': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'a_incomplete': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'o_created': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'a_pending': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'w_call incomplete files': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'w_shortened completion': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'o_sent (mail and online)': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'a_validating': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'o_create offer': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'o_sent (online only)': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'a_denied': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'o_accepted': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'o_cancelled': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#         'w_personal loan collection': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']] }
#         ## BPI challenge 2017

#         ## Sepsis cases 2016 (no noise)
#         cDependencies = {
#             'iv liquid': [],
#             'crp': [],
#             'er triage': [],
#             'leucocytes': [],
#             'er sepsis triage': [],
#             'er registration': [],
#             'lacticacid': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['*lacticacid']],
#             'admission nc': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['*admission nc']],
#             'iv antibiotics': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes']],
#             'admission ic': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic']],
#             'release b': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid']],
#             'release a': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release b']],
#             'release c': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release b']],
#             'release d': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release b']],
#             'release e': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release b']],
#             'return er': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release a'], ['release b'], ['release c'], ['release d'], ['release e']]
#             }
                

        self.dependenciesTransition.clear()
        for key in cDependencies.keys():
            self.dependenciesTransition[key]= cDependencies[key] 
        return self.dependenciesTransition


    def getAllActivities(self):
        transitionsWithoutTau = [elem for elem in self.transitions if 'tau' not in elem]
        return transitionsWithoutTau 
    
    def getStartActivity(self):
        startActivities = []
        for elem in self.dependenciesTransition :
            if not self.dependenciesTransition[elem]:
                startActivities.extend([elem])
#             else:
#                 for subElem in self.dependenciesTransition[elem]:
#                     if not subElem:
#                         startActivities.extend([elem])
        #startActivities = [elem for elem in self.dependenciesTransition if not self.dependenciesTransition[elem]]
        return startActivities 

    def getPossibleStartActivity(self):
        startPossibleActivities = []
        for elem in self.dependenciesTransition :
            if self.dependenciesTransition[elem]:
                for subElem in self.dependenciesTransition[elem]:
                    if not subElem:
                        startPossibleActivities.extend([elem])
        #startActivities = [elem for elem in self.dependenciesTransition if not self.dependenciesTransition[elem]]
        return startPossibleActivities 
################################################################################        
################################################################################        
