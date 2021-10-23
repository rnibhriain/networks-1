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

public class Actuator {

	static DatagramSocket socket;
	
	static InetSocketAddress dstAddress;


	final static int DEST_PORT = 49000;
	
	static final String DEFAULT_DST_NODE = "broker";

	static InetAddress address;  // InetAddress.getByName(args[0]);;
	static int port= DEST_PORT;

	final static int MTU = 1500;
	
	static String data;

	static int roomNo;
	static int floor;
	static int id;
	static boolean status;

	Actuator () {
		roomNo = 1;
		floor = 1;
		id = 140;
		status = false;
	}

	
	public static void connect () {


		try {
			System.out.println("Actuator is Connecting");

			// extract destination from arguments
			address= InetAddress.getLocalHost();   // InetAddress.getByName(args[0]);
			port= DEST_PORT;                       // Integer.parseInt(args[1]);

			dstAddress= new InetSocketAddress("broker", port);

			System.out.println("Actuator Connected: " + socket);


		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public String toString () {
		String data = "Actuator \nRoom Number: " + roomNo + "\nFloor Number: " + floor + "\nI.D. Number: " + id + "\nStatus: ";

		if (status) data += "on";
		else data += "off";
		
		return data;
		
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

			data = ostream.readUTF();
			
			// print data and end of program
			System.out.println("Data: " + data);
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
			System.out.println("Actuator is Sending");

			// convert string "Hello World" to byte array
			bstream= new ByteArrayOutputStream();
			ostream= new ObjectOutputStream(bstream);
			ostream.writeUTF(message);
			ostream.flush();
			buffer= bstream.toByteArray();

			// create packet addressed to destination
			packet= new DatagramPacket(buffer, buffer.length,
					address, port);

			// send packet
			socket.send(packet);

			System.out.println("Actuator sent packet '" + message + "'");
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		try {
			address= InetAddress.getLocalHost();
			socket= new DatagramSocket();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   // InetAddress.getByName(args[0]);
		catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Actuator actuator = new Actuator();
		
		send("subscribe " + actuator.toString()); // Integer.parseInt(args[1]);
		
		receive();
		
		if(data.equals("off")) status = false;
		else status = true;

		// create socket for the connection to broker



	}

}
