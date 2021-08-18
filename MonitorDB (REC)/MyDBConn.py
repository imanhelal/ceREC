'''
Created on Mar 15, 2018

@author: Iman
'''
import sqlite3

'''################################################################
############# The one used with required results
'''
class MyDBConn(object):
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
    



'''################################################################
'''
