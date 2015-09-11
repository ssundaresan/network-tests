import sys
import time
from socket import *

BUFSIZE = 1500
stats = {}

def start(port):
  s = socket(AF_INET,SOCK_DGRAM)
  s.bind(('',port))
  while 1:
    data,addr = s.recvfrom(BUFSIZE)
    if data == "start":
      stats[addr] = {'stime':time.time(),'cnt':0}
      continue
    if data == "end":
      try:
        s.sendto("Recd:%s dur %s"%(stats[addr]['cnt'],time.time()-stats[addr]['stime']),addr)
        print "recvd %s pkts from %s"%(stats[addr]["cnt"],addr)
      except:
        continue
      stats[addr] = None
      continue
    if data == "ping":
      print "recvd ping from ",addr
      try:
        s.sendto("pong",addr)
        continue
      except:
        continue
    try:
      stats[addr]["cnt"] += 1
    except:
      continue


if __name__ == '__main__':
  try:
    port = int(sys.argv[1])
  except:
    sys.stderr.write("Usage echo.py <port>\n")
    sys.exit()
  start(port)

