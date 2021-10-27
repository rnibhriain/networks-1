import javax.xml.crypto.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SenderReceiver {

	static DatagramSocket socket;

	static final String DEFAULT_DST_NODE = "localhost";	// Name of the host for the server
	static final int HEADER_LENGTH = 3; // Length of the header of the packet

	public static final int DEFAULT_BROKER_PORT= 49000;


	static final int TYPE_POS = 0; // Position of the type within the header
	static final int LENGTH_POS = 1; // Position of the length of the buffer within the header
	static final int ID_POS= 2;

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_ACK = 1;
	static final byte TYPE_PUB = 2;
	static final byte TYPE_SUB = 3;
	static final byte TYPE_UNSUB = 4;

	SenderReceiver (DatagramSocket socket) { 
		this.socket = socket;
	}

	public synchronized void sendData(byte[] data, InetSocketAddress destinationAddress, byte ID) throws IOException {

		data[ID_POS] = ID;

		DatagramPacket packet= new DatagramPacket(data, data.length);

		packet.setSocketAddress(destinationAddress);
		socket.send(packet);
	}

	public static void sendAck (InetSocketAddress dstAddress, int ID, DatagramSocket socket) throws IOException {

		byte[] data = null;
		DatagramPacket packet = null;

		data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = TYPE_ACK;
		data[LENGTH_POS] = 0;

		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(dstAddress);
		socket.send(packet);
	}
	
	static byte[] concat(byte[] a, byte[] b) {
	    byte[] result = new byte[a.length + b.length]; 
	    System.arraycopy(a, 0, result, 0, a.length); 
	    System.arraycopy(b, 0, result, a.length, b.length); 
	    return result;
	}

	public static byte [] packPacket (int type, String message) {
		byte [] data = null;
		DatagramPacket packet;
		ObjectOutputStream ostream;
		ByteArrayOutputStream bstream;
		byte[] buffer;
		byte [] finalData = null;

		try {
			bstream= new ByteArrayOutputStream();
			ostream= new ObjectOutputStream(bstream);
			ostream.writeUTF(message);
			ostream.flush();
			buffer= bstream.toByteArray();
			
			data = new byte[HEADER_LENGTH];
			data[TYPE_POS] = (byte)type;
			data[LENGTH_POS] = (byte)buffer.length;
			
			concat(data, buffer);

			// create packet addressed to destination
			packet= new DatagramPacket(buffer, buffer.length);

			System.out.println("Dashboard sent packet '" + message + "'");
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return finalData;
	}

	public static void onReceipt(DatagramPacket packet) throws IOException, InterruptedException{
		if(packet.getData()[TYPE_POS]!=TYPE_ACK) {
			int ID = packet.getData()[ID_POS];
			sendAck(new InetSocketAddress(packet.getAddress(), packet.getPort()), ID, socket);
		} else {
			System.out.println("Received Ack");
		}



	}

}
