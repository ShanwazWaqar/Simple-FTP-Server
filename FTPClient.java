
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FTPClient {

	private Socket socket = null;
	private static Scanner sysInput = null;
	private DataOutputStream serverOut = null;
	private DataInputStream serverIn = null;
	private static final String ACK = "ACK";

	public FTPClient(String address, int port) throws IOException {
		try {
			socket = new Socket(address, port);

			System.out.println("Connected");

			sysInput = new Scanner(System.in);
			serverOut = new DataOutputStream(socket.getOutputStream());
			serverIn = new DataInputStream(socket.getInputStream());

		} catch (IOException e) {
			System.out.println("IO Exception occured while connecting");
			return;
		}

		String inputLine = "";
		while (true) {
			System.out.print("mytftp>");
			inputLine = sysInput.nextLine();
			processCommands(inputLine);
			if (inputLine.trim().equals("quit")) {
				break;
			}
		}
		return;
	}

	private void processCommands(String command) {
		String[] tokens = command.split("\\s+");

		if (tokens == null || tokens.length <= 0) {
			return;
		}

		if (tokens[0].equals("get") && tokens.length == 2) {

			processGet(command);

		} else if (tokens[0].equals("put") && tokens.length == 2) {

			processPut(command);

		} else if (tokens[0].equals("delete") && tokens.length == 2) {

			processDel(command);

		} else if (tokens[0].equals("ls") && tokens.length == 1) {

			processList(command);

		} else if (tokens[0].equals("cd") && tokens.length == 2) {

			processChangeDir(command);

		} else if (tokens[0].equals("mkdir") && tokens.length == 2) {

			processMakeDir(command);

		} else if (tokens[0].equals("pwd") && tokens.length == 1) {

			processPwd(command);
		} else if (tokens[0].equals("quit") && tokens.length == 1) {

			try {
				serverOut.writeUTF(command);
			} catch (IOException e) {
				System.out.println("error while quit");
			}
		} else {
			System.out.println("Invalid command");
		}
		return;
	}

	private void processGet(String command) {

		try {
			serverOut.writeUTF(command);
			String[] vals = command.split("\\s+");
			int bytes = 0;

			long size = serverIn.readLong();
			if (size <= 0) {
				System.out.println("No file found");
				return;
			}
			FileOutputStream fileOutputStream = new FileOutputStream(vals[1]);
			byte[] buffer = new byte[4 * 1024];
			while (size > 0 && (bytes = serverIn.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
				fileOutputStream.write(buffer, 0, bytes);
				size -= bytes;
			}
			fileOutputStream.close();
			return;
		} catch (IOException e) {
			System.out.println("Error while get");
		}

	}

	private void processPut(String command) {

		String[] vals = command.split("\\s+");
		File fl = new File(System.getProperty("user.dir"));
		List<String> files = Arrays.asList(fl.list());
		if (!files.contains(vals[1].trim())) {
			System.out.println("File not Found");
			return;
		}
		try {
			serverOut.writeUTF(command);
			String filepath = System.getProperty("user.dir") + "/" + vals[1].trim();
			int bytes = 0;
			File file = new File(filepath);
			FileInputStream fileInputStream = new FileInputStream(file);
			serverOut.writeLong(file.length());
			byte[] buffer = new byte[4 * 1024];
			while ((bytes = fileInputStream.read(buffer)) != -1) {
				serverOut.write(buffer, 0, bytes);
				serverOut.flush();
			}
			fileInputStream.close();
			return;

		} catch (FileNotFoundException e) {
			System.out.println("File not Found");
		} catch (IOException e) {
			System.out.println("Error while put");
		}

	}

	private void process(String command) throws IOException {

		serverOut.writeUTF(command);
		while (true) {

			String msg = serverIn.readUTF();
			if (msg.equals(ACK)) {
				break;
			} else if (msg.contains("Error:")) {
				System.out.println(msg);
				break;
			}
			System.out.println(msg);
		}
		return;
	}

	private void processDel(String command) {
		
		try {
			process(command);
		} catch (IOException e) {
			System.out.println("Exception during delete");
		}
		return;
	}

	private void processList(String command) {
		
		try {
			process(command);
		} catch (IOException e) {
			System.out.println("Exception during ls");

		}
		return;
	}

	private void processChangeDir(String command) {

		try {
			process(command);
		} catch (IOException e) {
			System.out.println("Exception during cd");
		}
		return;
	}

	private void processMakeDir(String command) {

		try {
			process(command);
		} catch (IOException e) {
			System.out.println("Exception during makedir");
		}
		return;

	}

	private void processPwd(String command) {

		try {
			process(command);

		} catch (IOException e) {
			System.out.println("Exception during pwd");
		}
		return;
	}

	private void close() {
		try {
			sysInput.close();
			socket.close();
			serverIn.close();
			serverOut.close();
			System.out.println("shutdown");
		} catch (IOException i) {
			System.out.println("IO Exception occured while closing");
		}
	}

	public static void main(String[] args) throws Exception {

		if (args == null || args.length != 2) {
			System.out.println("give server and port number as arguments");
		}
		FTPClient client = new FTPClient(args[0], Integer.valueOf(args[1]));
		client.close();
	}

}
