
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FTPServer {

	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private String currentDir = "";
	private static final String ACK = "ACK";

	public FTPServer(int port) {
		try {
			server = new ServerSocket(port);
			System.out.println("Server started");
		} catch (IOException e) {
			System.out.println("Error while initializing");
		}
	}

	private void start() {
		currentDir = System.getProperty("user.dir");
		try {
			socket = server.accept();
			System.out.println("Client accepted");

			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());

			String line = "";

			while (true) {
				line = in.readUTF();
				processCommands(line);
				if (line.equals("quit")) {
					break;
				}
			}

		} catch (IOException e) {
			try {
				close();
				this.server.close();
			} catch (IOException e1) {
				System.out.println("Error while shuttingdown");
			}
		}
		return;
	}

	private void close() {
		System.out.println("Closing connection");
		try {
			if(socket!=null) socket.close();
			if(in!=null) in.close();
			if(out!=null) out.close();
		} catch (IOException e) {
			System.out.println("IO Exception occured while closing");
		}
	}

	private void processCommands(String command) {

		if (command.contains("get")) {

			processGet(command);

		} else if (command.contains("put")) {

			processPut(command);

		} else if (command.contains("delete")) {

			processDel(command);

		} else if (command.contains("ls")) {

			processList();

		} else if (command.contains("cd")) {

			processChangeDir(command);

		} else if (command.contains("mkdir")) {

			processMakeDir(command);

		} else if (command.contains("pwd")) {

			processPwd();
		} else if (command.contains("quit")) {
			close();
		}
		return;
	}

	private void processGet(String command) {
		String[] vals = command.split("\\s+");
		String filepath = currentDir + "/" + vals[1].trim();
		int bytes = 0;

		File fl = new File(currentDir);
		List<String> files = Arrays.asList(fl.list());
		System.out.println(files);
		if (!files.contains(vals[1].trim())) {
			try {
				out.writeLong(0l);
			} catch (IOException e) {
				System.out.println("Error in get");
			}
			return;
		}

		File file = new File(filepath);
		System.out.println(filepath);
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(file);
			out.writeLong(file.length());
			byte[] buffer = new byte[4 * 1024];
			while ((bytes = fileInputStream.read(buffer)) != -1) {
				out.write(buffer, 0, bytes);
				out.flush();
			}
			fileInputStream.close();
			return;
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error in get");
		}

	}

	private void processPut(String command) {
		String[] vals = command.split("\\s+");
		String filepath = currentDir + "/" + vals[1].trim();

		try {
			int bytes = 0;
			FileOutputStream fileOutputStream = new FileOutputStream(filepath);

			long size = in.readLong();
			byte[] buffer = new byte[4 * 1024];
			while (size > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
				fileOutputStream.write(buffer, 0, bytes);
				size -= bytes;
			}
			fileOutputStream.close();
			return;
		} catch (IOException e) {
			System.out.println("Error while put");
		}
	}

	private void processDel(String command) {
		String[] vals = command.split("\\s+");
		File fl = new File(currentDir);
		List<String> files = Arrays.asList(fl.list());
		if (files.contains(vals[1].trim())) {

			File myObj = new File(currentDir + "/" + vals[1].trim());
			if (!myObj.delete()) {
				System.out.println("file not deleted");
			}
			try {
				out.writeUTF(ACK);
			} catch (IOException e) {
				System.out.println("error while deleting");
			}
		} else {
			try {
				out.writeUTF("Error:File not found");
			} catch (IOException e) {
				System.out.println("no file found");
			}
		}
		return;

	}

	private void processList() {

		File fl = new File(currentDir);
		String[] files = fl.list();
		try {
			for (int i = 0; i < files.length; i++) {
				out.writeUTF(files[i]);
			}
			out.writeUTF(ACK);
		} catch (IOException e) {
			try {
				out.writeUTF("Error:processing in ls");
			} catch (IOException e1) {
				return;
			}
		}
		return;
	}

	private void processChangeDir(String command) {
		String[] vals = command.split("\\s+");

		// usecases: what if absolute path
		
		vals[1] = vals[1].trim();
		if (vals[1].length() != 1 && vals[1].endsWith("/")) {
			vals[1] = vals[1].substring(0, vals[1].length() - 1);
		}

		if (vals[1].trim().equalsIgnoreCase("~")) {
			
			currentDir = System.getProperty("user.dir");
			
		} else if (vals[1].trim().equalsIgnoreCase("..")) {
			
			if (!currentDir.equals("/")) {
				int ind = currentDir.lastIndexOf("/");
				currentDir = currentDir.substring(0, ind);
			}
			
		} else if (vals[1].trim().startsWith("../")) {
			
			if (!currentDir.equals("/")) {
				int ind = currentDir.lastIndexOf("/");
				currentDir = currentDir.substring(0, ind);
				String cmd = vals[0] + " " + vals[1].trim().substring(3);
				processChangeDir(cmd);
				return;
			}
			
		} else {
			
			String temp = vals[1].trim().startsWith("/")? vals[1].trim():currentDir + "/" + vals[1].trim();
			File tempPath = new File(temp);
			if (tempPath.isDirectory()) {
				currentDir = temp;
			} else {
				try {
					out.writeUTF("Error:No Directory Found");
					return;
				} catch (IOException e1) {
					return;
				}
			}
		}
		
		if (currentDir.length() != 1 && currentDir.endsWith("/")) {
			currentDir = currentDir.substring(0, currentDir.length() - 1);
		}
		
		try {
			out.writeUTF(ACK);
		} catch (IOException e) {
			System.out.println("IO Exception occured while changeDir");
		}

	}

	private void processMakeDir(String command) {
		String[] vals = command.split("\\s+");
		File fl = new File(currentDir);
		List<String> files = Arrays.asList(fl.list());
		try {
			if (files.contains(vals[1])) {
				out.writeUTF(ACK);
				return;
			}
			Path path = Paths.get(currentDir + "/" + vals[1]);
			Files.createDirectory(path);
			out.writeUTF(ACK);
			return;
		} catch (IOException e) {
			try {
				out.writeUTF("Error:processing mkdir");
			} catch (IOException e1) {
				return;
			}
			System.out.println("IO Exception occured while mkdir");
		}
	}

	private void processPwd() {
		try {
			out.writeUTF(currentDir);
			out.writeUTF(ACK);
		} catch (IOException e) {
			try {
				out.writeUTF("Error:processing mkdir");
			} catch (IOException e1) {
				return;
			}
		}
		return;
	}

	public static void main(String[] args) throws Exception {
		
		if (args == null || args.length != 1) {
			System.out.println("give port number as argument");
		}
		FTPServer ftpserver = new FTPServer(Integer.valueOf(args[0]));

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					ftpserver.close();
					if(ftpserver.server != null) {
						ftpserver.server.close();
					}
					return;
				} catch (IOException e) {
					System.out.println("error while shutting down");
				}
			}
		}, "Shutdown-thread"));

		while (true) {
			ftpserver.start();
		}

	}

}
