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
		byte[] emptyDataSet = new byte[BUFFER_AMT]; //used to reset the incoming data.
		byte[] sendData  = new byte[BUFFER_AMT];
		int sequenceNum = 1;
		int checksum;
		
		
		while(true) {
			//Receiving packet from client
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			System.out.println(receivePacket.getData());
			
			//extracting data from the packet
			String sentence = new String(receivePacket.getData());
			checksum = checksumCalc(receiveData);

			//resetting the receive Data variable
			receiveData = null;
			receiveData = emptyDataSet.clone();

			//splitting the request into separate parts to check if it is valid.
			String[] request = sentence.split("[ ]");
			
			boolean fileExist;
			boolean methodTokenValid;
			String htmlDocumentBuffer = readFile(ERRORFILE);
			
			//checking if the response is valid and constructing a header in response to the invalid request
			if(request.length !=3){
				//error not a proper length of a request
				//send 400 as a status code
				header = new HTTPHeader(Integer.toString(contentLengthCalculator(ERRORFILE)), 400, MIMETypeGenerator(ERRORFILE), sequenceNum, checksum);
			}
			else {
				//proper length now, need to check  if each element is authentic
				methodTokenValid = isMethodTokenValid(request[0]);
				fileExist = checkFileExistence(request[1]);
				if (methodTokenValid && fileExist){
					header = new HTTPHeader(Integer.toString(contentLengthCalculator(request[1])), 200, MIMETypeGenerator(request[1]), sequenceNum, checksum);
					htmlDocumentBuffer = readFile(request[1]);
				} 
				if(methodTokenValid && !fileExist){
					header = new HTTPHeader(Integer.toString(contentLengthCalculator(ERRORFILE)), 404, MIMETypeGenerator(ERRORFILE), sequenceNum, checksum);
				}
				if(!methodTokenValid){
					header = new HTTPHeader(Integer.toString(contentLengthCalculator(ERRORFILE)), 400, MIMETypeGenerator(ERRORFILE), sequenceNum, checksum);
				}
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
			sendData = new byte[1];
			sendData[0] = 0;
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);

		}
	}

	/**
	 * @param fileNameAndPath
	 * @return true if the file exist false otherwise
	 */
	private  static boolean checkFileExistence(String fileNameAndPath){
		if(fileNameAndPath == null){
			return false;
		}
		File file = new File(System.getProperty("user.dir"), fileNameAndPath);
		return file.getAbsoluteFile().exists();
	}

	/**
	 * @param requestMethodToken
	 * @return true if the request has a valid method token such as GET
	 */
	private  static boolean isMethodTokenValid(String requestMethodToken){
		//Kept this way to be able to check for more tokens.
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
	 * @return Content length in the number of bytes the file has
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
	 * @return String of the file in ASCII encoding
	 */
	private static String readFile(String fileName){
		if(fileName == null){
			return "";
		}		
		File file = new File(System.getProperty("user.dir"), fileName);
		byte[] encoded = null;  
		try{
			encoded = Files.readAllBytes(file.toPath());

		} catch(IOException e){
			return "";
		}
		try{
			return new String(encoded,"UTF-8");
		} catch(UnsupportedEncodingException e){
			return "UTF IS NOT SUPPORTED";
		}
	}

	/**
	 * @param fileName
	 * @return Content type
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
	 * @param packetData
	 * @return int
	 */
	public static int checksumCalc(byte[] packetData){

		int sum = 0;

		for (byte b: packetData) {
			sum += b;
		}
		return sum;
	}
}
