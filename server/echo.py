import sys
import time
import threading
from socket import *

BUFSIZE = 1500

class udpThread(threading.Thread):
  port = 0
  stats = {}
  def __init__(self,port):
    print 'starting udp'
    super(udpThread,self).__init__()
    self.port = port 

  def run(self):
    s = socket(AF_INET,SOCK_DGRAM)
    s.bind(('',self.port))
    while 1:
      data,addr = s.recvfrom(BUFSIZE)
      if data == "start":
        self.stats[addr] = {'stime':time.time(),'cnt':0}
        continue
      if data == "end":
        try:
          s.sendto("Recd:%s dur %s"%(self.stats[addr]['cnt'],time.time()-self.stats[addr]['stime']),addr)
          print "recvd %s pkts from %s"%(self.stats[addr]["cnt"],addr)
        except:
          continue
        self.stats[addr] = None
        continue
      if data == "ping":
        print "recvd ping from ",addr
        try:
          s.sendto("pong",addr)
          continue
        except:
          continue
      try:
        self.stats[addr]["cnt"] += 1
      except:
        continue

class tcpThread(threading.Thread):
  port = 0
  def __init__(self,port):
    print 'starting tcp'
    super(tcpThread,self).__init__()
    self.port = port 

  def run(self):
    s = socket(AF_INET,SOCK_STREAM)
    s.bind(('',self.port))
    s.listen(1)
    while 1:
      conn,addr = s.accept()
      print "accepting from ",addr
      conn.close()
 
  
if __name__ == '__main__':
  try:
    port = int(sys.argv[1])
  except:
    sys.stderr.write("Usage echo.py <port>\n")
    sys.exit()
  udpThread(port).start()
  tcpThread(port).start()

