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

	public static final int DEFAULT_BROKER_PORT= 49000;

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_ACK = 1;
	static final byte TYPE_PUB = 2;
	static final byte TYPE_SUB = 3;
	static final byte TYPE_UNSUB = 4;

	SenderReceiver (DatagramSocket socket) { 
		this.socket = socket;
	}

	public static void sendAck (InetSocketAddress dstAddress, int ID, DatagramSocket socket) throws IOException {
		String Ack = Integer.toString(TYPE_ACK);
		byte[] data = null;
		DatagramPacket packet = null;

		ObjectOutputStream ostream;
		ByteArrayOutputStream bstream;
		byte[] buffer;

		try {
			System.out.println("Dashboard is Sending");

			// convert string "Hello World" to byte array
			bstream= new ByteArrayOutputStream();
			ostream= new ObjectOutputStream(bstream);
			ostream.writeUTF(Ack);
			ostream.flush();
			buffer= bstream.toByteArray();

			packet= new DatagramPacket(data, data.length);
			packet.setSocketAddress(dstAddress);
			socket.send(packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static DatagramPacket packPacket (int type, String message) {
		byte [] data = null;
		DatagramPacket packet = null;
		ObjectOutputStream ostream;
		ByteArrayOutputStream bstream;
		byte[] buffer;
		message += type + ":";
		try {
			bstream= new ByteArrayOutputStream();
			ostream= new ObjectOutputStream(bstream);
			ostream.writeUTF(message);
			ostream.flush();
			buffer= bstream.toByteArray();

			packet= new DatagramPacket(buffer, buffer.length);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return packet;
	}

}
