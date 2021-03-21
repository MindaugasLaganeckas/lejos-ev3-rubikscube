package ev3.rubikscube.controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

	private static final int SERVER_PORT = 3333;
	private static final String SERVER_IP = "192.168.2.29";

	public static void main(String[] args) throws Exception {
		
		try (final Socket s = new Socket(SERVER_IP, SERVER_PORT);
				final DataInputStream din = new DataInputStream(s.getInputStream());
				final DataOutputStream dout = new DataOutputStream(s.getOutputStream());
				final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));) {
			String str = "", str2 = "";
			while (!str.equals("stop")) {
				str = br.readLine();
				dout.write(0);
				dout.flush();
				int out = din.read();
				System.out.println("Server says: " + out);
			}
		}

		/*final String scrambledCube = 
				  "WRGOOOOOG" //U
				+ "WGYYWWWYY" //R
				+ "WYRBGGYWB" //F
				+ "RROGRRYRO" //D
				+ "BBGWYYGWB" //L
				+ "RBRBBOBGO"; //B
		final String result = new Search().solution(scrambledCube, 21, 100000000, 10000, 0);
        System.out.println(result);*/
	}
}
