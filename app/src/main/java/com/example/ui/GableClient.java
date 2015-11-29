import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;
import java.net.*;

public class GableClient {
	private static int connectionPort = 4711;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	public static final int VISIT = 0;
	public static final int REQUEST = 1;

	/**
	 * Constructor
	 **/
	public GableClient () {
		try {
			socket = new Socket("143.248.177.124", connectionPort);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Unknown host");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't open connection");
			System.exit(1);
		}
	}

	/**
	 * @param int
	 *            message, the request type
	 * @throws IOException
	 *             if the message couldn't be sent over the stream
	 **/
	public void send(int message, int[] args) throws IOException {
		out.println(message);
		for (int i : args) {
			out.println(i);
		}
	}

	/**
	 * @return ArrayList<String> response, the received response
	 * @throws IOException
	 *             if the stream is closed
	 **/
	public ArrayList<Integer> response() throws IOException {
		ArrayList<Integer> response = new ArrayList<Integer>();
		String s;
		while (!(s = in.readLine()).equals("-1")) {
			response.add(Integer.parseInt(s));
		}
		return response;
	}

	/**
	 * @throws IOException
	 *             if the streams are closed
	 **/
	public void close() throws IOException {
		out.close();
		in.close();
		socket.close();
	}
}
