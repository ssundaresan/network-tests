
public class test{
	public static void main(String args[]){
		NetworkTest nt = new NetworkTest();
		nt.cbrTest("127.0.0.1",10000,10000,20,5);
		nt.pingTest("127.0.0.1", 10000, 100);
		nt.tcpConnTest("127.0.0.1", 10000, 100);
	}
}