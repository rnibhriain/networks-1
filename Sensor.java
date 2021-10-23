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

public class Sensor {

	static DatagramSocket socket;

	static InetSocketAddress dstAddress;

	final static int DEST_PORT = 49000;

	static InetAddress address;  // InetAddress.getByName(args[0]);;
	static InetAddress server;
	static int port= DEST_PORT;
	
	static String DEFAULT_DST_NODE = "broker";


	final static int MTU = 1500;

	static int roomNo;
	static int floor;
	static int id;
	static int temp;

	Sensor () {
		roomNo = 1;
		floor = 1;
		id = 12345;
		temp = 22;
	}

	
	public static void connect () {


		try {
			System.out.println("Sensor is Connecting");

			// extract destination from arguments
			address= InetAddress.getLocalHost();   // InetAddress.getByName(args[0]);
			port= DEST_PORT;                       // Integer.parseInt(args[1]);

			dstAddress= new InetSocketAddress(DEFAULT_DST_NODE, port);
			server = InetAddress.getByName(DEFAULT_DST_NODE);

			System.out.println("Sensor Connected: " + socket);


		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public String toString () {

		return "Sensor \nRoom Number: " + roomNo + "\nFloor Number: " + floor + "\nI.D. Number: " + id + "\nTemperature: " + temp + "c";
	}

	public static void receive () {

		DatagramPacket packet;

		ObjectInputStream ostream;
		ByteArrayInputStream bstream;
		byte[] buffer;

		try {
			System.out.println("Sensor is receiving");

			// create buffer for data, packet and socket
			buffer= new byte[MTU];
			packet= new DatagramPacket(buffer, buffer.length);

			// attempt to receive packet
			System.out.println("Trying to receive");
			socket.receive(packet);

			// extract data from packet
			buffer= packet.getData();
			bstream= new ByteArrayInputStream(buffer);
			ostream= new ObjectInputStream(bstream);

			// print data and end of program
			System.out.println("Data: " + ostream.readUTF());
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
			System.out.println("Sensor is Sending");

			// convert string "Hello World" to byte array
			bstream= new ByteArrayOutputStream();
			ostream= new ObjectOutputStream(bstream);
			ostream.writeUTF(message);
			ostream.flush();
			buffer= bstream.toByteArray();

			// create packet addressed to destination
			packet= new DatagramPacket(buffer, buffer.length,
					server, port);

			// send packet
			socket.send(packet);

			System.out.println("Sensor sent packet '" + message + "'");
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		
		connect();
		
		try {
			socket= new DatagramSocket();
		}  // InetAddress.getByName(args[0]);
		catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Sensor sensor = new Sensor();
		
		send("publish " + sensor.toString()); // Integer.parseInt(args[1]);

		// create socket for the connection to broker



	}

}
