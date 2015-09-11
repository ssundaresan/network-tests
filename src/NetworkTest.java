import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class NetworkTest{
	private long nanosInSec = 1000000000;
	public int cbrTest(
				String server,
				int port,
				int pps,
				int pktLen,
				int duration){
		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();
		ByteBuffer start;
		ByteBuffer buf = null;
		ByteBuffer end;
		long interPktDur = nanosInSec/pps;
		long startTime = System.nanoTime();
		int cnt = 0;
		int ncnt = 0;
		DatagramChannel channel = initChannel(server,port);
		if (channel == null)
			return -1;
		System.out.print("Out " + System.currentTimeMillis() + " " + System.nanoTime() + " " + interPktDur + " " + cnt + "\n");
		try {
			start = encoder.encode(CharBuffer.wrap("start"));
			end = encoder.encode(CharBuffer.wrap("end"));
			channel.write(start);
		} catch (Exception e1) {
			e1.printStackTrace();
			return -1;
		}
		long eTime = startTime + duration*nanosInSec;
		long busyWait = startTime;
		
		char [] payloadChar = new char[pktLen];
		Arrays.fill(payloadChar, '0');
		String payload = new String(payloadChar);
		while (System.nanoTime() < eTime){
			//cTime = System.nanoTime();
			busyWait += interPktDur;
			while (System.nanoTime() < busyWait){
			}
			try {
				buf = encoder.encode(CharBuffer.wrap(payload));
				channel.write(buf);
				cnt ++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ncnt++;
			}
		}
		System.out.print("Sent:"+cnt+" "+"Unsent:"+ncnt+" "+"dur:"+(System.nanoTime()-startTime)+" "+payload+"\n");
		try {
			channel.write(end);
			buf = ByteBuffer.allocate(1500);
			channel.read(buf);
			System.out.print(new String(buf.array())+"\n");
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	public int pingTest(
				String server,
				int port,
				int cnt){
		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();
		ByteBuffer sbuf = null;
		ByteBuffer rbuf = ByteBuffer.allocate(1500);
		ArrayList<Long> outArr = new ArrayList<Long>();
		int ncnt=0;
		long startTime = System.nanoTime();
		long rtt = 0;
		DatagramChannel channel = initChannel(server,port);
		if(channel == null)
			return -1;
		int rlen;
		long sum = 0;
		for (int i=0;i<cnt+1;i++){
			try {
				sbuf = encoder.encode(CharBuffer.wrap("ping"));
				startTime = System.nanoTime();
				channel.write(sbuf);
				rlen = 0;
				while (rlen <= 0){
					rlen = channel.read(rbuf);
				}
				rbuf.flip();
				byte [] rarr = new byte[rlen];
				rbuf.get(rarr);
				rbuf.clear();
				String rcv = new String(rarr);
				if (rcv.equals("pong")){
					rtt = System.nanoTime() - startTime;
					//System.out.print("rtt " + rtt + "\n");
				}
				Thread.sleep(10);
				if (i > 0){
					outArr.add(rtt);
					sum += rtt;
				}
			} catch (Exception e) {
				e.printStackTrace();
				ncnt++;
			}
		}
		Collections.sort(outArr);
		int len = outArr.size();
		double avg = (1.0d * sum)/len;
		System.out.print("avg:" + avg + " " + "median:" + outArr.get(len/2)+"\n"); 
		return 0;
	}

	
	private DatagramChannel initChannel(String server, int port){
		InetAddress inetServer;
		try {
			inetServer = InetAddress.getByName(server);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		DatagramChannel channel;
		try{
			channel = DatagramChannel.open();
			channel.configureBlocking(false);
			channel.connect(new InetSocketAddress(inetServer,port));
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return channel;
	}
	public int tcpConnTest(
				String server,
				int port,
				int cnt){
		ArrayList<Long> outArr = new ArrayList<Long>();
		long sum = 0;
		long startTime;
		long rtt;
		for(int i=0;i<cnt;i++){
			try {
				Socket s = new Socket();
				startTime = System.nanoTime();
				s.connect(new InetSocketAddress(server, port),2000);
				rtt = System.nanoTime() - startTime;
				s.close();
				outArr.add(rtt);
				sum += rtt;
				Thread.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(outArr);
		int len = outArr.size();
		double avg = (1.0d * sum)/len;
		System.out.print("avg:" + avg + " " + "median:" + outArr.get(len/2)+"\n");
		return 1;
	}
	
}