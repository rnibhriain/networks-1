import java.io.ByteArrayInputStream;
import java.util.Scanner;
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

public class Sensor extends SenderReceiver{

	static DatagramSocket socket;

	static InetSocketAddress dstAddress;

	final static int DEST_PORT = 49000;
	static int port= DEST_PORT;
	
	static String DEFAULT_DST_NODE = "broker";

	final static int MTU = 1500;
	static int roomNo;
	static int floor;
	static int id;
	static double temp;
	static double humidity;

	Sensor (DatagramSocket socket) {
		super(socket);
		roomNo = 1;
		floor = 1;
		id = 12345;
		temp = 22;
		humidity = 32;
	}

	
	public static void connect () {
		try {
			System.out.println("Sensor is Connecting");
			port= DEST_PORT;                       // Integer.parseInt(args[1]);
			dstAddress= new InetSocketAddress(DEFAULT_DST_NODE, port);
			System.out.println("Sensor Connected: " + dstAddress);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String toString () {

		return "Sensor " + id + " Room number : " + roomNo + " Floor number : " + floor;
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
			System.out.println(data);
			if (data.equals(Integer.toString(TYPE_ACK))) {
				System.out.println("Sensor Received acknowledgement");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void send (String message) {
		byte [] array = packPacket(TYPE_PUB, message);
		DatagramPacket packet = new DatagramPacket(array, array.length);
		packet.setSocketAddress(dstAddress);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		connect();
		try {
			socket= new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		Sensor sensor = new Sensor(socket);
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("What is the ID of this sensor?");
		id = scanner.nextInt();
		System.out.println("What is the room number of this sensor?");
		roomNo = scanner.nextInt();
		System.out.println("What is the floor number of this sensor?");
		floor = scanner.nextInt();
		
		while (true) {
			System.out.println("Which would you like to publish? humidity or temperature");
			String received = scanner.next();
			if (received.equals("humidity")) {
				System.out.println("What is the humidity?");
				humidity = scanner.nextDouble();
				sensor.send(sensor.toString() + " humidity: " + humidity + "%");
				
				// to receive ack
				sensor.receive();
			} else if (received.equals("temperature")) {
				System.out.println("What is the temperature?");
				temp = scanner.nextDouble();
				sensor.send(sensor.toString() + " temperature: " + temp + "c");
				
				// to receive ack
				sensor.receive();
			}
		}


	}

}
