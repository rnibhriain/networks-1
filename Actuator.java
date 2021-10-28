import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Actuator extends SenderReceiver{

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

	Actuator (DatagramSocket socket) {
		super(socket);
		roomNo = 1;
		floor = 1;
		id = 140;
		status = false;
	}


	public static void connect () {
		try {
			System.out.println("Actuator is Connecting");

			port= DEST_PORT;                       // Integer.parseInt(args[1]);

			dstAddress= new InetSocketAddress(DEFAULT_DST_NODE, port);

			System.out.println("Actuator Connected: " + dstAddress);
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
			buffer= new byte[MTU];
			packet= new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);

			buffer= packet.getData();
			bstream= new ByteArrayInputStream(buffer);
			ostream= new ObjectInputStream(bstream);

			String data =  ostream.readUTF();
			String[]  splitString = data.split(":");

			if (splitString[splitString.length-1].equals("on")) {
				status = true;
				System.out.println("Sensor has been turned on");
			} else if (splitString[splitString.length-1].equals("off")) {
				status = false;
				System.out.println("Sensor has been turned off");
			} else if (data.split(":")[0].equals(Integer.toString(TYPE_ACK))) {
				System.out.println("Sensor Received acknowledgement");
			} else {
				System.out.println("Error");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void send (int type, String message) {
		byte [] array = packPacket(type, message);
		DatagramPacket packet = new DatagramPacket(array, array.length);
		packet.setSocketAddress(dstAddress);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		try {
			socket= new DatagramSocket();
		}
		catch (SocketException e) {
			e.printStackTrace();
		}

		Actuator act = new Actuator(socket);

		act.connect();

		Scanner scanner = new Scanner(System.in);

		System.out.println("What is the ID of this actuator?");
		id = scanner.nextInt();
		System.out.println("What is the room number of this actuator?");
		roomNo = scanner.nextInt();
		System.out.println("What is the floor number of this actuator?");
		floor = scanner.nextInt();
		act.send(TYPE_SUB, "Actuator :" + id + ":" + "1" + ":status");
		act.receive();
		boolean finished = false;

		while (!finished) {
			System.out.println("Would you like to wait or unsubscribe?");
			String received = scanner.next();
			if (received.equals("wait")) {
				act.receive();
			} else if (received.equals("unsubscribe")){
				act.send(TYPE_UNSUB, "Actuator :" + id + ":" + "1" + " :status");
				act.receive();
				finished = true;
			}

		}

	}
}