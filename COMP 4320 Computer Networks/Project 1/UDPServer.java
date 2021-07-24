import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

class UDPServer {

	/**
	 * @param NONE
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		
		final int PORT_NUMBER = 80;
		final int BUFFER_AMT = 1024;
		
		DatagramSocket serverSocket = new DatagramSocket(PORT_NUMBER);
		HTTPHeader header = null;
		final String ERRORFILE = "error.html";
		byte[] receiveData = new byte[BUFFER_AMT];
		byte[] emptyDataSet = new byte[BUFFER_AMT]; 
		byte[] sendData  = new byte[BUFFER_AMT];
		int sequenceNum = 1;
		int checksum;
		
		
		while(true) {
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			
			String sentence = new String(receivePacket.getData());
			checksum = checksumCalc(receiveData);

			receiveData = null;
			receiveData = emptyDataSet.clone();

			String[] request = sentence.split("[ ]");
			
			boolean fileExist;
			boolean methodTokenValid;
			String htmlDocumentBuffer = readFile(ERRORFILE);

			methodTokenValid = isMethodTokenValid(request[0]);
			fileExist = checkFileExistence(request[1]);
		
			if (methodTokenValid && fileExist){
				header = new HTTPHeader(Integer.toString(contentLengthCalculator(request[1])), 200, MIMETypeGenerator(request[1]), sequenceNum, checksum);
				htmlDocumentBuffer = readFile(request[1]);
			} 
			else if(methodTokenValid && !fileExist){
				header = new HTTPHeader(Integer.toString(contentLengthCalculator(ERRORFILE)), 404, MIMETypeGenerator(ERRORFILE), sequenceNum, checksum);
			}
			else if(!methodTokenValid){
				header = new HTTPHeader(Integer.toString(contentLengthCalculator(ERRORFILE)), 400, MIMETypeGenerator(ERRORFILE), sequenceNum, checksum);
			}
			
			sequenceNum += 1;
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String headerInformation = header.toString();
			
			String headerAndData = new String(headerInformation + "\r\n"+ htmlDocumentBuffer);
			byte[] headerAndDataByteArray = headerAndData.getBytes();
			

			DatagramPacket sendPacket;
			int start = 0;
			int end = 1024;
			for(int i = 0; i < (double)headerAndDataByteArray.length/1024 ; i++){

				byte[] dataInformation = Arrays.copyOfRange(headerAndDataByteArray, start, end);
				System.out.println(new String(dataInformation));
				sendData = dataInformation;
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);
				start = end;
				end = end + 1024;
			}
			
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);

		}
	}

	/**
	 * @param requestMethodToken
	 * @return checks to make sure the token is GET
	 */
	private  static boolean isMethodTokenValid(String requestMethodToken){
		
		String[] MethodTokenList = {"GET"};
		for(String methodToken : MethodTokenList){
			if(requestMethodToken.toUpperCase().equalsIgnoreCase(methodToken)){
				return true;
			}	
		}
		return false;
	}

	/**
	 * @param fileName
	 * @return returns true if the file exists
	 */
	private  static boolean checkFileExistence(String fileName){
		if(fileNameAndPath == null){
			return false;
		}
		File file = new File(System.getProperty("user.dir"), fileName);
		return file.getAbsoluteFile().exists();
	}

	/**
	 * @param fileName
	 * @return length of the file in bytes
	 */
	private static int contentLengthCalculator(String fileName){
		if(fileName == null){
			return 0;
		}		
		File file = new File(System.getProperty("user.dir"), fileName);
		byte[] encoded = null;  
		try{
			encoded = Files.readAllBytes(file.toPath());
		} catch(IOException e){
			return 0;
		}

		return encoded.length;

	}

	/**
	 * @param fileName
	 * @return generates the cotent type for the HTTPHeader
	 */
	public static String MIMETypeGenerator(String fileName){
		String mimeType="text/plain";
		if (fileName.endsWith(".html") || fileName.endsWith(".htm"))
			mimeType="text/html";
		else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
			mimeType="image/jpeg";
		else if (fileName.endsWith(".gif"))
			mimeType="image/gif";
		else if (fileName.endsWith(".class"))
			mimeType="application/octet-stream";
		return mimeType;
	}
	
	/**
	 * @param fileName
	 * @return reading the file contents
	 */
	private static String readFile(String fileName) throws IOException{
		if(fileName == null){
			return "";
		}		
		File file = new File(System.getProperty("user.dir"), fileName);
		byte[] encoded = null;  
		
		encoded = Files.readAllBytes(file.toPath());

		return new String(encoded,"UTF-8");
	}

	/**
	 * @param packetData
	 * @return the checksum value
	 */
	public static int checksumCalc(byte[] packetData){

		int sum = 0;

		for (byte b: packetData) {
			sum += b;
		}
		return sum;
	}
}
