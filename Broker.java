import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Broker {

	static Boolean subscribed = false;
	static DatagramSocket socket;

	final static int DEST_PORT = 60183;

	static InetAddress address;  // InetAddress.getByName(args[0]);;

	static InetAddress clientAdd;

	static int clientPort;
	
	static InetAddress actuatorAdd;

	static int actuatorPort;

	static int port= DEST_PORT;

	static InetAddress sensorAdd;

	static int sensorPort;

	static Sensor current = new Sensor();

	static String data;

	static final int PACKETSIZE = 65536;

	public static void receive () {

		DatagramPacket packet;

		ObjectInputStream ostream;
		ByteArrayInputStream bstream;
		byte[] buffer;

		try {
			System.out.println("Broker is receiving");

			// create buffer for data, packet and socket
			buffer= new byte[Dashboard.MTU];
			packet= new DatagramPacket(buffer, buffer.length);

			// attempt to receive packet
			System.out.println("Trying to receive");
			socket.receive(packet);

			// extract data from packet
			buffer= packet.getData();
			bstream= new ByteArrayInputStream(buffer);
			ostream= new ObjectInputStream(bstream);

			// print data and end of program

		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void send (String data, InetAddress address, int port) {

		DatagramPacket packet;

		ObjectOutputStream ostream;
		ByteArrayOutputStream bstream;
		byte[] buffer;

		try {
			System.out.println("Broker is Sending");

			// convert string to byte array
			bstream= new ByteArrayOutputStream();
			ostream= new ObjectOutputStream(bstream);
			ostream.writeUTF(data);
			ostream.flush();
			buffer= bstream.toByteArray();

			// create packet addressed to destination
			packet= new DatagramPacket(buffer, buffer.length,
					address, port);

			Thread.sleep(2000);
			// send packet
			socket.send(packet);

			System.out.println("Broker sent packet '" + data + "'");
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void listen () {

		String message ;

		try {
			// Endless loop: attempt to receive packet, notify receivers, etc
			while(true) {
				DatagramPacket packet;

				ObjectInputStream ostream;
				ByteArrayInputStream bstream;
				byte[] buffer;

				try {
					System.out.println("Broker is receiving");

					// create buffer for data, packet and socket
					buffer= new byte[Dashboard.MTU];
					packet= new DatagramPacket(buffer, buffer.length);

					// attempt to receive packet
					System.out.println("Trying to receive");
					socket.receive(packet);


					// extract data from packet
					buffer= packet.getData();
					bstream= new ByteArrayInputStream(buffer);
					ostream= new ObjectInputStream(bstream);
					
					message = ostream.readUTF();
					Scanner scanner = new Scanner(message);
					String mess = scanner.next();
					if (mess.equals("subscribe")) {
						subscribed = true;
						clientAdd = packet.getAddress();
						clientPort = packet.getPort();
						send(data,clientAdd,clientPort);
					} else {
						data = message;
					}
					
					System.out.println("Data: " + message);
					System.out.println("ReceiverProcess - Program end");
					
					parse(message);

				}
				catch(Exception e) {
					e.printStackTrace();
				}


			}
		} catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}

	}

	public static void parse (String message) {

		System.out.println("Data: " + message);
		System.out.println("ReceiverProcess - Program end");

	}

	public static void main(String[] args) {

		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 

		try {
			Broker.socket= new DatagramSocket(Dashboard.port, Dashboard.address);
		} catch (SocketException e) {
			e.printStackTrace();
		}


		listen();

	}


}
