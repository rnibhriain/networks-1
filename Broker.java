import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.ArrayList;
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


public class Broker extends SenderReceiver {

	static Boolean subscribed = false;
	
	static DatagramSocket socket;
	static InetAddress address;

	
	static InetAddress clientAdd;
	static int clientPort;

	//static HashMap <Integer,Subscriber>subscribers;
	static ArrayList <Subscriber>subscribers;

	
	static String data;
	static final int PACKETSIZE = 65536;
	
	static public class Subscriber {
		int id;
		InetSocketAddress dstAddress;
		String info;
		
		Subscriber (int id, InetSocketAddress address, String info) {
			this.id = id;
			this.info = info;
			this.dstAddress = address;
		}
	}
	
	Broker (DatagramSocket socket) {
		super(socket);
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
					
					clientAdd = packet.getAddress();
					clientPort = packet.getPort();
					InetSocketAddress dstaddress = new InetSocketAddress(clientAdd, clientPort);
					
					// send an ack
					send("", TYPE_ACK, dstaddress);
					
					parse(message);

				}
				catch(Exception e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}

	}
	
	public static Subscriber getSub (String [] data) {
		Subscriber sub = null;
		
		for (int i = 0; i < subscribers.size(); i++) {
			Subscriber current = subscribers.get(i);
			if (current.id == Integer.parseInt(data[3]) && current.info.equals(data[4])) {
				return current;
			}
		}
		return sub;
	}

	public static void parse (String message) {
		System.out.println("Received: " + message);
		InetSocketAddress add = new InetSocketAddress(clientAdd, clientPort);
		
		String [] data = message.split(":");
		if (data[0].equals(Integer.toString(TYPE_UNKNOWN))) {
			System.out.println("Error");
		} else if (data[0].equals(Integer.toString(TYPE_PUB))) {
			Subscriber sub = getSub(data);
			if (sub != null) {
				String string = "";
				for (int i = 3; i < data.length; i++) {
					string += data[i] + ":";
				}
				send(string, TYPE_PUB, sub.dstAddress);
			}
		} else if (data[0].equals(Integer.toString(TYPE_SUB))) {
			Subscriber sub = new Subscriber(Integer.parseInt(data[2]), add, data[4]);
			System.out.println("This is where the problem is: " + Integer.parseInt(data[2]));
			subscribers.add(sub);
		} else if (data[0].equals(Integer.toString(TYPE_UNSUB))) {
			Subscriber sub = new Subscriber(Integer.parseInt(data[2]), add, data[3]);
			subscribers.remove(sub);
		}
	}

	public static void main(String[] args) {
		try {
			socket= new DatagramSocket(DEFAULT_BROKER_PORT);
		} 
		catch (SocketException e) {
			e.printStackTrace();
		}
		
		Broker broker = new Broker(socket);
		subscribers = new ArrayList<Subscriber>();
		
		parse("3:Dash :1:12:temperature");
		parse("2:Sensor:12:1:32.0c");
		
		

		broker.listen();
	}


}
