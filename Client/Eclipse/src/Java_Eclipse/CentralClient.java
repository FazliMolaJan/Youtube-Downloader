package Java_Eclipse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class CentralClient {
	public static void main(String[] args)
	{
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket ControlSocket = null;
		DataOutputStream outToServer = null;
		DataInputStream inFromServer = null;
		Scanner in = null;
		try {
			int port1 = 12345;
			//String ip = "192.168.0.7";
			String ip = "localhost";
			ControlSocket = new Socket(ip, port1);
			outToServer = new DataOutputStream(ControlSocket.getOutputStream());
			inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
			in = new Scanner(System.in);
			
			System.out.println("You are connected to the raspberry pi!");
			System.out.println(ip);
			
			System.out.print("Enter a URL:\t");
			// Send the command to the server.
			outToServer.writeBytes(in.nextLine() + '\n');
			// Read status code with readInt(), which blocks until all 3 bytes read.
			//Need only 3 bytes, as the status is 3 characters long
			byte[] statusBytes = new byte[3];
			inFromServer.read(statusBytes);
	        int statusCode = Integer.parseInt(new String(statusBytes, "UTF-8"));
			
			// If status code is 550 (error).
			if (statusCode == 550) {
				System.out.println("Did not work.");
			} 
			else if (statusCode == 200)
			{
				float percent_done = 0;
				while (percent_done < 100)
				{
					byte[] percentBytes = new byte[5];
					inFromServer.read(percentBytes);
					String tempPercent = new String(percentBytes, "UTF-8").trim();
					if (tempPercent.contentEquals("255"))
					{
						ControlSocket.close();
						in.close();
						return;
					}
			        percent_done = Float.parseFloat(tempPercent);
			        System.out.println(percent_done);
				}
				//System.out.println(inFromServer.skip(10));
				byte[] tempSizeBuffer = new byte[10];
				inFromServer.read(tempSizeBuffer);
				String tempSize = new String(tempSizeBuffer, "UTF-8").trim();
		        int size = Integer.parseInt(tempSize);
		        System.out.println("Size is " + Integer.toString(size));
				
		        DataInputStream inData = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
		        byte[] dataIn = new byte[size];
				// Reads bytes from the inData stream and places them in dataIn byte array.
				inData.readFully(dataIn);

				// Use FileOutputStream to write byes to new file.
				String filePath = System.getProperty("user.dir") + "/" + "test.mp4";
				try (FileOutputStream fos = new FileOutputStream(filePath)) {
					fos.write(dataIn);
				}
				
			}
			in.close();
			ControlSocket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
