package ev3.rubikscube.controller;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Client implements Closeable {

	private static final Map<String, Integer> communicationCodes = new LinkedHashMap<>() {
		private static final long serialVersionUID = 1L;
	{
		put("B", 1);
		put("B2", 2);
		put("B'", 3);

		put("D", 4);
		put("D2", 5);
		put("D'", 6);

		put("F", 7);
		put("F2", 8);
		put("F'", 9);

		put("L", 10);
		put("L2", 11);
		put("L'", 12);

		put("R", 13);
		put("R2", 14);
		put("R'", 15);

		put("U", 16);
		put("U2", 17);
		put("U'", 18);
		
		put("FINISH", 100);
	}};
	
	private final Socket socket;
	private final DataInputStream din;
	private final DataOutputStream dout;
	
	public static void main(String[] args) throws Exception{
		try (final Client client = new Client("192.168.1.130", 3333);) {
			final List<String> list = new LinkedList<>(communicationCodes.keySet());
			final Random rand = new Random();
			int counter = 0;
			while (counter  < 20) {
				final String code = list.get(rand.nextInt(list.size() - 1));
				client.sendCommand(code);
				counter++;
			}
			client.sendCommand("FINISH");
		}
	}
	
	public Client(final String address, final int port) throws Exception {
		this.socket = new Socket(address, port);
		this.din = new DataInputStream(socket.getInputStream());
		this.dout = new DataOutputStream(socket.getOutputStream());
	}
	
	public void sendCommand(final String command) throws Exception {
		System.out.println("Sending " + command);
		final int code = communicationCodes.get(command);
		dout.write(code);
		dout.flush();
		final int returnCode = din.read();
		if (returnCode != 0) {
			throw new RuntimeException("Server returned " + returnCode);
		}
	}
	
	@Override
	public void close() throws IOException {
		if (socket != null) {
			socket.close();
		}
		if (din != null) {
			din.close();
		}
		if (dout != null) {
			dout.close();
		}
	}
}
