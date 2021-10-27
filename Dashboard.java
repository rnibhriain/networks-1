import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Dashboard extends SenderReceiver {

	static DatagramSocket socket;

	static InetSocketAddress dstAddress;

	final static int DEST_PORT = 49000;

	static InetAddress address;  // InetAddress.getByName(args[0]);;

	static int port= DEST_PORT;

	final static int MTU = 1500;

	static final String DEFAULT_DST_NODE = "broker";

	Dashboard (DatagramSocket socket) {
		super(socket);
	}
	
	public static void main(String[] args) {

		try {
			socket= new DatagramSocket();
		}  // InetAddress.getByName(args[0]);
		catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Dashboard dash = new Dashboard(socket);
		Scanner scanner = new Scanner(System.in);

		dash.connect();

		String message = "";

		while (true) {
			System.out.println("would you like to subscribe or publish?");
			message = scanner.next();
			if (message.equals("subscribe")) {
				System.out.println("Which sensor would u like to see?");
				message += " " + scanner.next();
			} else if (message.equals("publish")){
				System.out.println("Which actuator would u like to communicate with?");
				message += " " + scanner.next();
			}
			dash.send(message);
			dash.receive();
			dash.receive();
		}

	}

	public static void receive () {

		DatagramPacket packet;

		ObjectInputStream ostream;
		ByteArrayInputStream bstream;
		byte[] buffer;

		try {
			System.out.println("Dashboard is receiving");

			// create buffer for data, packet and socket
			buffer= new byte[MTU];
			packet= new DatagramPacket(buffer, buffer.length);

			// attempt to receive packet
			System.out.println("Trying to receive");
			socket.receive(packet);

			// extract data from packet
			buffer= packet.getData();
			

			// print data and end of program
			System.out.println("Data: " + buffer);
			System.out.println("ReceiverProcess - Program end");
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void send (String message) {

		DatagramPacket packet;
		ObjectOutputStream ostream;
		ByteArrayOutputStream bstream;
		byte[] buffer;

		try {
			System.out.println("Dashboard is Sending");

			// convert string "Hello World" to byte array
			bstream= new ByteArrayOutputStream();
			ostream= new ObjectOutputStream(bstream);
			ostream.writeUTF(message);
			ostream.flush();
			buffer= bstream.toByteArray();

			// create packet addressed to destination
			packet= new DatagramPacket(buffer, buffer.length);

			// send packet
			
			packet.setSocketAddress(dstAddress);
			socket.send(packet);

			System.out.println("Dashboard sent packet '" + message + "'");
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void connect () {
		try {
			System.out.println("Dashboard is Connecting");
			
			port= DEST_PORT;                       // Integer.parseInt(args[1]);

			dstAddress= new InetSocketAddress(DEFAULT_DST_NODE, port);

			System.out.println("Sensor Connected: " + dstAddress);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

