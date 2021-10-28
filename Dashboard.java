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

public class Dashboard extends SenderReceiver {

	static DatagramSocket socket;

	static InetSocketAddress dstAddress;

	final static int DEST_PORT = 49000;

	static int port= DEST_PORT;

	final static int MTU = 1500;

	static final String DEFAULT_DST_NODE = "broker";

	Dashboard (DatagramSocket socket) {
		super(socket);
	}
	
	public static void main(String[] args) {

		try {
			socket= new DatagramSocket();
		} 
		catch (SocketException e) {
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
				int sensor = scanner.nextInt();
				System.out.println("Would you like to see humidity or temperature?");
				message = scanner.next();
				if (message.equals("humidity")) {
					send("Dash 1 :" + sensor + ": humidity", TYPE_SUB, dstAddress);
				} else if (message.equals("temperature")) {
					send("Dash 1 :" + sensor + ": temperature", TYPE_SUB, dstAddress);
				}
				dash.receive();
			} else if (message.equals("publish")){
				System.out.println("Which actuator would u like to communicate with?");
				int actuator = scanner.nextInt();
				System.out.println("Would you like to turn it on or off?");
				message = scanner.next();
				if (message.equals("on")) {
					send("Dash 1 :" + actuator + ": on", TYPE_PUB, dstAddress);
				} else if (message.equals("off")) {
					send("Dash 1 :" + actuator + ": off", TYPE_PUB, dstAddress);
				}
				dash.receive();
				dash.receive();
			}
		}

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
			String [] message = data.split(":");
			
			if (message[0].equals(Integer.toString(TYPE_ACK))) {
				System.out.println("Dashboard Received acknowledgement");
			} else if (message[0].equals(Integer.toString(TYPE_PUB))) {
				String string = "";
				for (int i = 1; i < message.length; i++) {
					string += message[i];
				}
				System.out.println("Dashboard Received data: " + string);
				
			} else {
				System.out.println("Error");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void send (String data, int type, InetSocketAddress address) {

		byte [] array = packPacket(type, data);
		DatagramPacket packet = new DatagramPacket(array, array.length);
		packet.setSocketAddress(address);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static void connect () {
		try {
			System.out.println("Dashboard is Connecting");
			
			port= DEST_PORT;

			dstAddress= new InetSocketAddress(DEFAULT_DST_NODE, port);

			System.out.println("Sensor Connected: " + dstAddress);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

