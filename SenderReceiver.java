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


	public static byte [] packPacket (int type, String message) {
		byte [] data = null;
		DatagramPacket packet = null;
		ObjectOutputStream ostream;
		ByteArrayOutputStream bstream;
		byte[] buffer = null;
		String finalData = type + ":" + message;
		try {
			bstream= new ByteArrayOutputStream();
			ostream= new ObjectOutputStream(bstream);
			ostream.writeUTF(finalData);
			ostream.flush();
			buffer= bstream.toByteArray();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return buffer;
	}

}
