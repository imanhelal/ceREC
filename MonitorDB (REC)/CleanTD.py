'''
This code is copyrighted to Iman Helal @2016-2017 Research work
Information System Department, Faculty of computers and Information System
Cairo University, Egypt
'''

import re

class CleanTD:
    

    finalDependencies = dict()
#     originalDependencies = {'t1':[[]], 
#           't2':[['t1']],
#           't3':[['t1']],
#           't4':[['tau 4']],
#           't5':[['*tau 5']],
#           't6':[[],['tau 1']],
#           'tau 1':[['t1']],
#           'tau 2':[['t5']],
#           'tau 3':[['tau 1'],['tau 2'],['t2']],
#           'tau 4':[['t2'],['t3'],['tau 3']],
#           'tau 5':[['t1','*t2']]       
#           }


#########################################################################################################
#           #loop log - corrected -- Good with 8536 events - 1000 cases  (used in comparison)
#     originalDependencies =  {'d': [['a'], ['*e']], 'tau': [['b']], 'h': [['g'], ['f']], 'f': [['tau']], 
#              'a': [], 'b': [['d']], 'e': [['b']], 'g': [['c']], 'c': [['tau']]}
#    
#     CleanTD = {'h': [['g'], ['f']], 'f': [['b']], 'e': [['b']], 'g': [['c']], 'd': [['a'], ['*e']], 'a': [], 'c': [['b']], 'b': [['d']]}
#########################################################################################################

#################################################################################
#     ## Paper example (Hospital check-up) - with silent transition, instead of K
#     originalDependencies  =  {'g': [['e']], 'b': [['a'], ['*n']], 'l': [['*tau'], ['*g']], 
#                 'd': [['b']], 'e': [['*d'], ['*h']], 'j': [['c']], 'c': [['b']], 'i': [['c']], 
#                 'm': [['l']], 'f': [['e']], 'h': [['f']], 'tau': [['i', 'j']], 'n': [['l']], 'a': []}
#     CleanTD = {'b': [['a'], ['*n']], 'f': [['e']], 'l': [['*g'], ['*i,*j']], 'n': [['l']], 
#      'e': [['*d'], ['*h']], 'd': [['b']], 'i': [['c']], 'c': [['b']], 'g': [['e']], 
#      'h': [['f']], 'm': [['l']], 'j': [['c']], 'a': []}
#################################################################################



##     BPI 2013 - concept name closed (used in comparison)
#     originalDependencies =  {
#         'tau2': [['accepted']], 
#         'unmatched': [], 
#         'queued': [['accepted']], 
#         'completed': [['tau2']], 
#         'accepted': [['tau1'], ['unmatched'], ['*queued']], 
#         'tau1': []}
#     cleanTD = {'queued': [['accepted']], 'accepted': [[],['unmatched'], ['*queued']], 'completed': [['accepted']], 'unmatched': []}
#  Should be   #Final Dependencies = {'queued': [['accepted']], 'accepted': [[],['unmatched'], ['*queued']], 'completed': [['accepted']], 'unmatched': []}
#################################################################################

# ##     BPI 2013 - concept name incidents on conceptnames
#     originalDependencies =  {'queued': [['tau from tree1'], ['*tau from tree3']], 
#             'tau from tree1': [], 
#             'tau from tree3': [['queued']], 
#             'tau from tree2': [['queued']], 
#             'tau from tree5': [['accepted']], 
#             'accepted': [['tau from tree1'], ['*tau from tree5']], 
#             'completed': [['tau from tree1']], 
#             'tau from tree4': [['accepted']], 
#             'tau from tree6': [['completed', 'tau from tree2', 'tau from tree4']]}
# #     #Final Dependencies = {'completed': [], 'queued': [['*queued']], 'accepted': [['*accepted']]}
#################################################################################

#     ##  BPI 2013 - VOLVO company - incidents on eventnames 
#     originalDependencies  =  {'tau from tree4': [['tau from tree3']], 
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



#################################################################################
#     ## WAB CoSeLoG
#     originalDependencies =  {'t15 print document x request unlicensed': [['t10 determine necessity to stop indication']], 
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


# cleanTD =  {  'confirmation of receipt': [],
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
# 't08 draft and send request for advice' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't09-1 process or receive external advice from party 1' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't09-2 process or receive external advice from party 2' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't09-3 process or receive external advice from party 3' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't09-4 process or receive external advice from party 4' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't11 create document x request unlicensed' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't16 report reasons to hold request' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't02 check confirmation of receipt' : [['t12 check document x request unlicensed'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'],  ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'],  ['*t09-4 process or receive external advice from party 4'], ['*t03 adjust confirmation of receipt'], ['*confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'], ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['t20 print report y to stop indication']] ,   
# 't04 determine confirmation of receipt' : [['t12 check document x request unlicensed'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'],  ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'],  ['*t09-4 process or receive external advice from party 4'], ['*t03 adjust confirmation of receipt'], ['*confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'], ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['t20 print report y to stop indication']] ,   
# 't05 print and send confirmation of receipt' : [ ['t04 determine confirmation of receipt' , 't02 check confirmation of receipt'],  ['t14 determine document x request unlicensed']],  
# 't06 determine necessity of stop advice' : [ ['t04 determine confirmation of receipt' , 't02 check confirmation of receipt'],  ['t14 determine document x request unlicensed'], ['t05 print and send confirmation of receipt']] }  

#######################################################################################
#     ## BPI 2012 model
#     originalDependencies  = {'t04': [['tau 5']], 
#           'tau 2': [['t06'], ['*t02'], ['*tau 9']], 
#           't03 ': [['tau 8']], 
#           'tau 10': [['tau 8']], 
#           'tau 9': [['t07-1'], ['t07-3'], ['*tau 6'], ['*tau 7'], ['*t07-4']], 
#           'tau 13': [['tau 11']], 
#           't11 ': [['tau 5']], 
#           't19 ': [['tau 5']], 
#           't09-1 ': [['tau 4']], 
#           't09-2 ': [['tau 4']], 
#           'tau 6': [['tau 4']], 
#           't01': [], 
#           't02 ': [['t01'], ['*tau 12']], 
#           't07-2 ': [['tau 4']], 
#           't10 ': [['tau 5']], 
#           't12 ': [['tau 5']], 
#           't07-5': [['tau 4']], 
#           't07-3 ': [['t06 '], ['*t02 '], ['*tau 9']], 
#           't20 ': [['tau 5']], 
#           't08 ': [['tau 4']], 
#           't07-4 ': [['t06 '], ['*t02 '], ['*tau 9']], 
#           't14 ': [['tau 5']], 
#           'tau 1': [['t01'], ['tau 12']], 
#           'tau 8': [['t07-1 '], ['t07-3 '], ['tau 6'], ['tau 7'], ['*t07-4 ']], 
#           't09-3 ': [['tau 4']], 
#           't09-4 ': [['tau 4']], 
#           'tau 5': [['t05 '], ['t10 '], ['t11 '], ['t12 '], ['t14 '], ['t16 '], ['t17 '], ['t18 '], ['t19 '], ['t20 '], ['*t04 '], ['*tau 3']], 
#           'tau 7': [['tau 5']], 
#           't15 ': [['tau 11']], 
#           'tau 3': [['t06 '], ['*t02 '], ['*tau 9']], 
#           'tau 11': [['tau 10'], ['t03 ']], 
#           'tau 4': [['t07-2 '], ['t07-5 '], ['t08 '], ['t09-1 '], ['t09-3 '], ['t09-4 '], ['*t09-2 '], ['*tau 2']], 
#           't18 ': [['tau 5']], 
#           't05 ': [['tau 5']], 
#           't17 ': [['tau 5']], 
#           't06 ': [['t01'], ['*tau 12']], 
#           't16 ': [['tau 5']], 
#           't07-1 ': [['t06 '], ['*t02 '], ['*tau 9']], 
#           'tau 12': [['tau 10'], ['*t03 ']]}

###################################################################################################################
#
#    ##BPI Challenge 2017    -- description 1,202,267 events - 31509 traces/cases  --> actual 561,672 events
#    originalDependencies =  {'tau6': [['*tau4'], ['*a_accepted']], 'o_create offer': [['tau3']], 
#     'a_complete': [['tau3']], 'tau3': [['a_complete'], ['w_assess potential fraud'], ['w_validate application'], 
#                                        ['w_call after offers'], ['a_cancelled'], ['o_returned'], ['a_incomplete'], ['o_created'], 
#                                        ['a_pending'], ['w_call incomplete files'], ['w_shortened completion'], 
#                                        ['o_sent (mail and online)'], ['a_validating'], ['o_create offer'], 
#                                        ['o_sent (online only)'], ['a_denied'], ['o_accepted'], ['o_cancelled'], 
#                                        ['w_personal loan collection'], ['*o_refused'], ['*tau2']], 
#     'a_create application': [], 'w_complete application': [['a_submitted'], ['tau1'], ['*tau7']], 
#     'w_call after offers': [['tau3']], 'tau4': [['tau3']], 'a_incomplete': [['tau3']], 
#     'w_handle leads': [['a_submitted'], ['tau1'], ['*tau7']], 'o_created': [['tau3']], 
#     'o_sent (mail and online)': [['tau3']], 'a_submitted': [['a_create application']], 
#     'w_validate application': [['tau3']], 
#     'tau2': [['w_complete application'], ['w_handle leads'], ['a_concept'], ['*tau5']], 
#     'tau1': [['a_create application']], 'o_refused': [['tau3']], 'tau5': [['*tau4'], ['*a_accepted']], 
#     'w_shortened completion': [['tau3']], 'tau7': [['tau6']], 'o_accepted': [['tau3']], 'tau8': [['tau6']], 
#     'a_cancelled': [['tau3']], 'a_pending': [['tau3']], 'w_call incomplete files': [['tau3']], 'o_returned': [['tau3']], 
#     'a_validating': [['tau3']], 'a_concept': [['a_submitted'], ['tau1'], ['*tau7']], 
#     'a_accepted': [['w_complete application'], ['w_handle leads'], ['*tau5'], ['*a_concept']], 
#     'o_sent (online only)': [['tau3']], 'w_personal loan collection': [['tau3']], 'o_cancelled': [['tau3']], 
#     'a_denied': [['tau3']], 'w_assess potential fraud': [['tau3']]}
#
#
#     CleanTD = {'a_create application': [],
#     'a_submitted': [['a_create application']], 
#     'w_complete application': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'w_handle leads': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'a_concept': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'a_accepted': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'a_complete': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'w_assess potential fraud': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'o_refused': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'w_validate application': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'w_call after offers': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'a_cancelled': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'o_returned': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'a_incomplete': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'o_created': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'a_pending': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'w_call incomplete files': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'w_shortened completion': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'o_sent (mail and online)': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'a_validating': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'o_create offer': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'o_sent (online only)': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'a_denied': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'o_accepted': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'o_cancelled': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']], 
#     'w_personal loan collection': [['a_create application'], ['a_submitted'], ['*w_complete application'], ['*w_handle leads'], ['*a_concept'], ['*a_accepted'], ['*a_complete'], ['*w_assess potential fraud'], ['*o_refused'], ['*w_validate application'], ['*w_call after offers'], ['*a_cancelled'], ['*o_returned'], ['*a_incomplete'], ['*o_created'], ['*a_pending'], ['*w_call incomplete files'], ['*w_shortened completion'], ['*o_sent (mail and online)'], ['*a_validating'], ['*o_create offer'], ['*o_sent (online only)'], ['*a_denied'], ['*o_accepted'], ['*o_cancelled'], ['*w_personal loan collection']] }
   


###########################################################################
##    Sepsis cases 2016
#    Noise 0.2
#  
#  
#     originalDependencies =  {'crp': [['tau3'], ['*tau7']], 'release a': [['tau10']], 
#                              'tau12': [['lacticacid', 'tau6', 'tau8', 'release a', 'tau11']], 
#                              'leucocytes': [['tau3'], ['*tau9']], 
#                              'tau2': [['er sepsis triage', 'er triage', 'er registration']], 
#                              'tau5': [['tau4']], 'release b': [], 
#                              'tau14': [['release d'], ['tau13'], ['release e'], ['release c']], 
#                              'admission ic': [], 'tau7': [['crp']], 'er registration': [['tau1']], 'iv liquid': [['tau4']], 
#                              'tau4': [['tau3']], 'tau6': [['crp']], 
#                              'tau10': [['admission nc', 'iv liquid', 'tau5', 'iv antibiotics']], 
#                              'tau1': [], 'tau3': [['tau2']], 'lacticacid': [['tau3']], 'er sepsis triage': [['tau1']], 
#                              'release e': [['tau12']], 'tau9': [['leucocytes']], 'admission nc': [['tau4']], 
#                              'iv antibiotics': [['tau4']], 'tau13': [['tau12']], 'er triage': [['tau1']], 
#                              'tau11': [['tau10']], 'release c': [['tau12']], 'tau8': [['leucocytes']], 
#                              'release d': [['tau12']], 
#                              'return er': [['release d'], ['tau13'], ['release e'], ['release c']]}
# 
#     Final Dependencies = {'release c': [['leucocytes']], 'release b': [], 
#                           'release e': [['admission nc', 'iv liquid', 'iv antibiotics', [[[['er sepsis triage', 'er triage', 'er registration']]]]]], 
#                           'lacticacid': [[['er sepsis triage', 'er triage', 'er registration']]], 
#                           'crp': [[['er sepsis triage', 'er triage', 'er registration']], ['*crp']], 
#                           'release a': [['er sepsis triage', 'er triage', 'er registration']], 'er registration': [], 
#                           'release d': [['crp']], 'er sepsis triage': [], 
#                           'admission nc': [[[['er sepsis triage', 'er triage', 'er registration']]]], 
#                           'iv liquid': [[[['er sepsis triage', 'er triage', 'er registration']]]], 
#                           'iv antibiotics': [[[['er sepsis triage', 'er triage', 'er registration']]]], 
#                           'leucocytes': [[['er sepsis triage', 'er triage', 'er registration']], ['*leucocytes']], 
#                           'admission ic': [], 'er triage': [], 
#                           'return er': [['release d'], ['release e'], ['release c'], ['lacticacid', 'release a', ['crp'], ['leucocytes'], [['admission nc', 'iv liquid', 'iv antibiotics', [[[['er sepsis triage', 'er triage', 'er registration']]]]]]]]}
###############################################################################
## Sepsis Cases 2016
## No Noise
# 
#     originalDependencies = {'tau16': [['tau1'], ['tau15']], 
#                             'tau10': [], 
#                             'release b': [['tau4'], ['tau13']], 
#                             'tau11': [['admission ic']], 
#                             'tau7': [['admission nc']], 
#                             'iv antibiotics': [['tau4']], 
#                             'iv liquid': [], 
#                             'return er': [['tau16'], ['release a'], ['release c'], ['release e'], ['release d']], 
#                             'tau18': [], 'admission nc': [['tau2'], ['*tau8']], 
#                             'tau5': [['tau4']], 'tau9': [['lacticacid']], 
#                             'release c': [['tau1'], ['tau15']], 
#                             'tau13': [['tau9', 'tau5', 'tau12']], 
#                             'release a': [['tau1'], ['tau15']], 
#                             'release e': [['tau1'], ['tau15']], 
#                             'tau4': [['tau2']], 
#                             'lacticacid': [['tau3']], 
#                             'tau1': [['iv liquid'], ['crp'], ['er triage'], ['leucocytes'], ['er sepsis triage'], ['er registration']], 
#                             'tau3': [['tau2']], 
#                             'tau2': [['iv liquid'], ['crp'], ['er triage'], ['leucocytes'], ['er sepsis triage'], ['er registration']], 
#                             'tau6': [['tau4']], 
#                             'leucocytes': [], 
#                             'tau12': [['admission ic']], 
#                             'tau14': [['tau4'], ['tau13']], 
#                             'er registration': [], 
#                             'er triage': [], 
#                             'tau17': [['tau16'], ['release a'], ['release c'], ['release e'], ['release d']], 
#                             'release d': [['tau1'], ['tau15']], 
#                             'er sepsis triage': [], 
#                             'tau8': [['admission nc']], 
#                             'crp': [], 
#                             'admission ic': [['tau6'], ['iv antibiotics'], ['*tau11']], 
#                             'tau15': [['release b', 'tau14', 'tau7']], 
#                             'tau19': []}
#         Final Dependencies = {
#         'iv liquid': [],
#         'crp': [],
#         'er triage': [],
#         'leucocytes': [],
#         'er sepsis triage': [],
#         'er registration': [],
#         'lacticacid': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['*lacticacid']],
#         'admission nc': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['*admission nc']],
#         'iv antibiotics': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes']],
#         'admission ic': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic']],
#         'release b': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid']],
#         'release a': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release b']],
#         'release c': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release b']],
#         'release d': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release b']],
#         'release e': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release b']],
#         'return er': [['er sepsis triage'], ['er triage'], ['er registration'], ['iv liquid'], ['crp'], ['leucocytes'], ['iv antibiotics'], ['*admission ic'], ['*admission nc'], ['*lacticacid'], ['release a'], ['release b'], ['release c'], ['release d'], ['release e']]
#         }

###############################################################################
    def findTau(self, parentDependencies):
        print('|--- inside findTau')
        indexTau = []
        for i in range(0,len(parentDependencies)):
            currentElem = parentDependencies[i]
            print('|---|--- currentElem ',currentElem, ' i = ',i)
            if [s for s in currentElem if 'tau' in s] :
                print('|---|---|--- currentElem has tau')
                for j in range(0,len(currentElem)):
                    print('|---|---|--- j = ',j)
                    if 'tau' in currentElem[j]: #currentElem[j].contains('tau'):
                        indexTau.append([i,j])
            print('|---|---|--- indexes of tau elements = ',indexTau)
        return indexTau


    def removeTau2(self,parentDependencies):
        print('|--- inside removeTau -- parentDependencies ', parentDependencies)
        
        flattenParents = [num  for elem in parentDependencies for num in elem] #flatten list
        print('|---|--- flattenParents = ',flattenParents)
        #tauList = [s for s in flattenParents if 'tau' in s]
        if not [s for s in flattenParents if 'tau' in s]: #'tau' not in flattenParents:
            print('|---parentDependencies for return ',parentDependencies)
            xList = [num  for num in parentDependencies ]
            return xList #parentDependencies
        else: #'tau' in flattenParents:
            index = self.findTau(parentDependencies)
            tempParents = [num  for num in parentDependencies ]
            print('|---|---|--- index = ',index)
            for k in range(0,len(index)):
                currentIndex = index[k]
                i = currentIndex[0]
                j = currentIndex[1]
                transitionTau = tempParents[i][j] ###
                print('|---|---|---|--- transitionTau = ',transitionTau)
                if 'tau' in transitionTau:
                    flagLoop = False
                    if transitionTau.startswith("*"):
                        flagLoop = True
                        print('|---|---|---|--- transitionTau startswith "*" = ',flagLoop)
                        transitionTau =  re.sub(r"\*",'',transitionTau)
                        print('regular expression removal of star * for ', transitionTau)
                        #transitionTau = transitionTau[1:]
                    currentDependencyTau = self.originalDependencies[transitionTau]
                    print('|---|---|---|--- transitionTau = ',transitionTau, ' currentDependencyTau= ',currentDependencyTau)
                    newDependencies = tempParents[i] ###
                    newDependencies.pop(j)
                    
                    tempDepWithoutLoop = [num  for num in currentDependencyTau]
                    tempDepWithoutLoop.extend(currentDependencyTau) 
                    if flagLoop:
                        loopFlaggedDep= self.addLoopFlag2(currentDependencyTau)
                        currentDependencyTau = [num  for num in loopFlaggedDep]
                        print('|---|---|---|--- updated currentDependencyTau = ',currentDependencyTau)
                    else:
                        currentDependencyTau = [num  for num in currentDependencyTau]
                    newDependencies.extend(currentDependencyTau)
                    
                    print('|---|---|---|--- updated newDependencies = ',newDependencies)
                    parentDependencies.pop(i)
                    print('|---|---|---|--- parentDependencies1 = ',parentDependencies)
                    if len(parentDependencies)>0 and len(newDependencies)>0:
                        print('|---|---|---|--- check for redundent transitions')
                        foundMatches = self.subfinder(parentDependencies, tempDepWithoutLoop)#newDependencies)
                        print('|---|---|---|--- redundent transitions = ',foundMatches)
                        if not foundMatches:
                            parentDependencies.extend(currentDependencyTau)
                        else:
                            currentDependencyTau.remove(foundMatches)
                            parentDependencies.extend(currentDependencyTau)
                    else:
                        parentDependencies.extend(currentDependencyTau)
                    print('|---|---|---|--- parentDependencies2 = ',parentDependencies)
                    self.removeTau2(parentDependencies)
                else:
                    continue

    def removeTau3(self,parentDependencies):
        print('|--- inside removeTau -- parentDependencies ', parentDependencies)
        
        flattenParents = [num  for elem in parentDependencies for num in elem] #flatten list
        print('|---|--- flattenParents = ',flattenParents)
        #tauList = [s for s in flattenParents if 'tau' in s]
        if not [s for s in flattenParents if 'tau' in s]: #'tau' not in flattenParents:
            print('|---parentDependencies for return ',parentDependencies)
            xList = [num  for num in parentDependencies ]
            return xList #parentDependencies
        else: #'tau' in flattenParents:
            index = self.findTau(parentDependencies)
            tempParents = [num  for num in parentDependencies ]
            print('|---|---|--- index = ',index)
            for k in range(0,len(index)):
                currentIndex = index[k]
                i = currentIndex[0]
                j = currentIndex[1]
                transitionTau = tempParents[i][j] ###
                print('|---|---|---|--- transitionTau = ',transitionTau)
                if 'tau' in transitionTau:
                    flagLoop = False
                    if transitionTau.startswith("*"):
                        flagLoop = True
                        print('|---|---|---|--- transitionTau startswith "*" = ',flagLoop)
                        transitionTau =  re.sub(r"\*",'',transitionTau)
                        print('regular expression removal of star * for ', transitionTau)
                        #transitionTau = transitionTau[1:]
                    currentDependencyTau = self.originalDependencies[transitionTau]
                    print('|---|---|---|--- transitionTau = ',transitionTau, ' currentDependencyTau= ',currentDependencyTau)
                    newDependencies = tempParents[i] ###
                    newDependencies.pop(j)
                    
                    tempDepWithoutLoop = [num  for num in currentDependencyTau]
                    tempDepWithoutLoop.extend(currentDependencyTau) 
                    if flagLoop:
                        loopFlaggedDep= self.addLoopFlag2(currentDependencyTau)
                        currentDependencyTau = [num  for num in loopFlaggedDep]
                        print('|---|---|---|--- updated currentDependencyTau = ',currentDependencyTau)
                        
                    else:
                        currentDependencyTau = [num  for num in currentDependencyTau]
                    newDependencies.extend(currentDependencyTau)
                    
                    print('|---|---|---|--- updated newDependencies = ',newDependencies)
                    parentDependencies.pop(i)
                    print('|---|---|---|--- parentDependencies1 = ',parentDependencies)
                    if len(parentDependencies)>0 and len(newDependencies)>0:
                        print('|---|---|---|--- check for redundent transitions')
                        foundMatches = self.subfinder2(parentDependencies, tempDepWithoutLoop)#newDependencies)
                        print('|---|---|---|--- redundent transitions = ',foundMatches)
                        if not foundMatches:
                            parentDependencies.extend(currentDependencyTau)
                        else:
                            currentDependencyTau.remove(foundMatches)
                            parentDependencies.extend(currentDependencyTau)
                    else:
                        parentDependencies.extend(currentDependencyTau)
                    print('|---|---|---|--- parentDependencies2 = ',parentDependencies)
                    self.removeTau2(parentDependencies)
                else:
                    continue


    def subfinder(self,myList, pattern):
        print ("|---|---|---|---|--- inside subfinder")
        redundent = []
        for p in pattern:
            for m in myList:
                if p == m:
                    if p not in redundent:
                        redundent.append(p)
        print("|---|---|---|---|--- redundents = ",redundent)
        redundent2 = [num for elem in redundent for num in elem] #flatten list
        return redundent2

    def subfinder2(self,myList, pattern):
        print ("|---|---|---|---|--- inside subfinder2")
        redundent = []
#         nonRedundent = [elem for elem in myList]
        for p in pattern:
            for m in myList:
                if p == m:
#                     nonRedundent.pop(p)
                    if p not in redundent:
                        redundent.append(p)
        print("|---|---|---|---|--- redundents = ",redundent)
        redundent2 = [num for elem in redundent for num in elem] #flatten list
        return redundent



    def addLoopFlag(self,currentDependencyTau): # For XOR only [['*t1'],['*t2']]
        print("|---|---|---|---|--- inside addLoopFlag ")
        depTauLoop = []
        depTau = [num  for num in currentDependencyTau]
        for i in range(0,len(depTau)):
            subDepTau = depTau[i]
            for j in range (0, len(subDepTau)):
                newSubDepTauStar = '*'+subDepTau[j]+'\''
                print("|---|---|---|---|--- newSubDepTauStar = ",newSubDepTauStar)
                depTauLoop.append([newSubDepTauStar])
        print("|---|---|---|---|--- depTauLoop = ",depTauLoop)
        return depTauLoop

    def addLoopFlag2(self,currentDependencyTau): #For AND [['*t1','*t2']] + XOR [['*t1'],['*t2']]
        print("|---|---|---|---|--- inside addLoopFlag ")
        depTauLoop = []
        depTau = [num  for num in currentDependencyTau]
        for item in depTau:
#             if len(item)>1:
#                 flattenItem = [num for elem in item for num in elem] # flatten for combining ANDs
#                 tempItemRE = [ re.sub(r"\*", '', x) for x in flattenItem]
#             else:
            tempItemNoBrackets = [ re.sub("\[*\]*", '', x) for x in item]
            tempItemRE = [ re.sub("\*", '', x) for x in tempItemNoBrackets]
            #tempItem = ["'{}'".format("','".join(tempItemRE))]
            my_string = ','.join('*'+str(x) for x in tempItemRE )
            tempItem = my_string.split(",")
            #tempItem = [','.join('*'+str(x) for x in tempItemRE )]
            depTauLoop.append(tempItem)
        print("|---|---|---|---|--- depTauLoop = ",depTauLoop)
        return depTauLoop

################################################

# # # # 
# # # # 
# # # # if __name__ == '__main__':
# # # #     
# # # #     c = CleanTD()
# # # #     for oT in  c.originalDependencies.keys():
# # # #         print('oT = ',oT)
# # # #         if 'tau' in oT:
# # # #             continue
# # # #         oTDependents = c.originalDependencies[oT]
# # # #         print('|--- oTDependents = ',oTDependents, ' its length = ',len(oTDependents))
# # # #         newParentDependencies = c.removeTau3(oTDependents)
# # # #         print('newParentDependencies ',newParentDependencies, ' oTDependents', oTDependents)
# # # #         c.finalDependencies[oT]=oTDependents #newParentDependencies
# # # #         print('|--- c.finalDependencies[',oT,'] = ',c.finalDependencies[oT])
# # # #     
# # # #     print('Final Dependencies =', c.finalDependencies)
# # # #     
    
    
    
    
        # BPI 2012
    # TD = {'t04 determine confirmation of receipt': [['tau from tree5']], 'tau from tree2': [['t06 determine necessity of stop advice'], ['*t02 check confirmation of receipt'], ['*tau from tree9']], 't03 adjust confirmation of receipt': [['tau from tree8']], 'tau from tree10': [['tau from tree8']], 'tau from tree9': [['t07-1 draft intern advice aspect 1'], ['t07-3 draft intern advice hold for aspect 3'], ['*tau from tree6'], ['*tau from tree7'], ['*t07-4 draft internal advice to hold for type 4']], 'tau from tree13': [['tau from tree11']], 't11 create document x request unlicensed': [['tau from tree5']], 't19 determine report y to stop indication': [['tau from tree5']], 't09-1 process or receive external advice from party 1': [['tau from tree4']], 't09-2 process or receive external advice from party 2': [['tau from tree4']], 'tau from tree6': [['tau from tree4']], 'confirmation of receipt': [], 't02 check confirmation of receipt': [['confirmation of receipt'], ['*tau from tree12']], 't07-2 draft intern advice aspect 2': [['tau from tree4']], 't10 determine necessity to stop indication': [['tau from tree5']], 't12 check document x request unlicensed': [['tau from tree5']], 't07-5 draft intern advice aspect 5': [['tau from tree4']], 't07-3 draft intern advice hold for aspect 3': [['t06 determine necessity of stop advice'], ['*t02 check confirmation of receipt'], ['*tau from tree9']], 't20 print report y to stop indication': [['tau from tree5']], 't08 draft and send request for advice': [['tau from tree4']], 't07-4 draft internal advice to hold for type 4': [['t06 determine necessity of stop advice'], ['*t02 check confirmation of receipt'], ['*tau from tree9']], 't14 determine document x request unlicensed': [['tau from tree5']], 'tau from tree1': [['confirmation of receipt'], ['tau from tree12']], 'tau from tree8': [['t07-1 draft intern advice aspect 1'], ['t07-3 draft intern advice hold for aspect 3'], ['tau from tree6'], ['tau from tree7'], ['*t07-4 draft internal advice to hold for type 4']], 't09-3 process or receive external advice from party 3': [['tau from tree4']], 't09-4 process or receive external advice from party 4': [['tau from tree4']], 'tau from tree5': [['t05 print and send confirmation of receipt'], ['t10 determine necessity to stop indication'], ['t11 create document x request unlicensed'], ['t12 check document x request unlicensed'], ['t14 determine document x request unlicensed'], ['t16 report reasons to hold request'], ['t17 check report y to stop indication'], ['t18 adjust report y to stop indicition'], ['t19 determine report y to stop indication'], ['t20 print report y to stop indication'], ['*t04 determine confirmation of receipt'], ['*tau from tree3']], 'tau from tree7': [['tau from tree5']], 't15 print document x request unlicensed': [['tau from tree11']], 'tau from tree3': [['t06 determine necessity of stop advice'], ['*t02 check confirmation of receipt'], ['*tau from tree9']], 'tau from tree11': [['tau from tree10'], ['t03 adjust confirmation of receipt']], 'tau from tree4': [['t07-2 draft intern advice aspect 2'], ['t07-5 draft intern advice aspect 5'], ['t08 draft and send request for advice'], ['t09-1 process or receive external advice from party 1'], ['t09-3 process or receive external advice from party 3'], ['t09-4 process or receive external advice from party 4'], ['*t09-2 process or receive external advice from party 2'], ['*tau from tree2']], 't18 adjust report y to stop indicition': [['tau from tree5']], 't05 print and send confirmation of receipt': [['tau from tree5']], 't17 check report y to stop indication': [['tau from tree5']], 't06 determine necessity of stop advice': [['confirmation of receipt'], ['*tau from tree12']], 't16 report reasons to hold request': [['tau from tree5']], 't07-1 draft intern advice aspect 1': [['t06 determine necessity of stop advice'], ['*t02 check confirmation of receipt'], ['*tau from tree9']], 'tau from tree12': [['tau from tree10'], ['*t03 adjust confirmation of receipt']]}
#     TD = {'t04': [['tau 5']], 
#           'tau 2': [['t06'], ['*t02'], ['*tau 9']], 
#           't03 ': [['tau 8']], 
#           'tau 10': [['tau 8']], 
#           'tau 9': [['t07-1'], ['t07-3'], ['*tau 6'], ['*tau 7'], ['*t07-4']], 
#           'tau 13': [['tau 11']], 
#           't11 ': [['tau 5']], 
#           't19 ': [['tau 5']], 
#           't09-1 ': [['tau 4']], 
#           't09-2 ': [['tau 4']], 
#           'tau 6': [['tau 4']], 
#           't01': [], 
#           't02 ': [['t01'], ['*tau 12']], 
#           't07-2 ': [['tau 4']], 
#           't10 ': [['tau 5']], 
#           't12 ': [['tau 5']], 
#           't07-5': [['tau 4']], 
#           't07-3 ': [['t06 '], ['*t02 '], ['*tau 9']], 
#           't20 ': [['tau 5']], 
#           't08 ': [['tau 4']], 
#           't07-4 ': [['t06 '], ['*t02 '], ['*tau 9']], 
#           't14 ': [['tau 5']], 
#           'tau 1': [['t01'], ['tau 12']], 
#           'tau 8': [['t07-1 '], ['t07-3 '], ['tau 6'], ['tau 7'], ['*t07-4 ']], 
#           't09-3 ': [['tau 4']], 
#           't09-4 ': [['tau 4']], 
#           'tau 5': [['t05 '], ['t10 '], ['t11 '], ['t12 '], ['t14 '], ['t16 '], ['t17 '], ['t18 '], ['t19 '], ['t20 '], ['*t04 '], ['*tau 3']], 
#           'tau 7': [['tau 5']], 
#           't15 ': [['tau 11']], 
#           'tau 3': [['t06 '], ['*t02 '], ['*tau 9']], 
#           'tau 11': [['tau 10'], ['t03 ']], 
#           'tau 4': [['t07-2 '], ['t07-5 '], ['t08 '], ['t09-1 '], ['t09-3 '], ['t09-4 '], ['*t09-2 '], ['*tau 2']], 
#           't18 ': [['tau 5']], 
#           't05 ': [['tau 5']], 
#           't17 ': [['tau 5']], 
#           't06 ': [['t01'], ['*tau 12']], 
#           't16 ': [['tau 5']], 
#           't07-1 ': [['t06 '], ['*t02 '], ['*tau 9']], 
#           'tau 12': [['tau 10'], ['*t03 ']]}
#     


# Wabo CoSeLoG
# cleanTD =  {  'confirmation of receipt': [],
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
# 't08 draft and send request for advice' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't09-1 process or receive external advice from party 1' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't09-2 process or receive external advice from party 2' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't09-3 process or receive external advice from party 3' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't09-4 process or receive external advice from party 4' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't11 create document x request unlicensed' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't16 report reasons to hold request' : [['*t03 adjust confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'] , ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'], ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'], ['*t09-4 process or receive external advice from party 4'], ['*confirmation of receipt']] ,
# 't02 check confirmation of receipt' : [['t12 check document x request unlicensed'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'],  ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'],  ['*t09-4 process or receive external advice from party 4'], ['*t03 adjust confirmation of receipt'], ['*confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'], ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['t20 print report y to stop indication']] ,   
# 't04 determine confirmation of receipt' : [['t12 check document x request unlicensed'], ['*t08 draft and send request for advice'], ['*t09-1 process or receive external advice from party 1'],  ['*t09-2 process or receive external advice from party 2'], ['*t09-3 process or receive external advice from party 3'],  ['*t09-4 process or receive external advice from party 4'], ['*t03 adjust confirmation of receipt'], ['*confirmation of receipt'], ['*t07-1 draft intern advice aspect 1'], ['*t07-2 draft intern advice aspect 2'], ['*t07-3 draft intern advice hold for aspect 3'], ['*t07-4 draft internal advice to hold for type 4'], ['*t07-5 draft intern advice aspect 5'], ['t20 print report y to stop indication']] ,   
# 't05 print and send confirmation of receipt' : [ ['t04 determine confirmation of receipt' , 't02 check confirmation of receipt'],  ['t14 determine document x request unlicensed']],  
# 't06 determine necessity of stop advice' : [ ['t04 determine confirmation of receipt' , 't02 check confirmation of receipt'],  ['t14 determine document x request unlicensed'], ['t05 print and send confirmation of receipt']] }  


## Wabo CoSeLoG -- CleanTD without loop for start event (in comparisons)
    cleanTD =  {  'confirmation of receipt': [],
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

