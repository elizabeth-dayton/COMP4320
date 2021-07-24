import java.io.*;
import java.net.*;
import java.util.*;

class UDPClient {
	public static void main(String args[]) throws Exception {

		//Reads in the keyboard stream by bytes and converts them into ASCII.
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = null;
		byte[] sendData = new byte[1024];
		byte[] emptyData = new byte[1024];
		byte[] receiveData = new byte[1024];
		String serverIP;
		int serverPortNumber;
		Scanner scan = new Scanner(System.in);
		double probabilityOfDamage;
		int checksum = 0;

		while(true){		
			System.out.print("Enter the server IP address: ");
			serverIP = scan.nextLine();
			try{
				IPAddress = InetAddress.getByName(serverIP);
			} catch(UnknownHostException e){
				System.out.println("Invalid host name or the host name is unreachable.");
				continue;
			}
			break;
		}

		System.out.print("Enter the server port number: ");
		serverPortNumber = scan.nextInt();

		System.out.print("Enter the probability of packet being damaged: ");
		probabilityOfDamage = scan.nextDouble();

		String sentence;
		String[] request;
		do{
			System.out.println("Type your HTTP Request and press Enter: ");
			sentence = inFromUser.readLine();
			request = sentence.split("[ ]");
		}while(request.length != 3);

		File file = new File(System.getProperty("user.dir"), request[1]);

		//sending the data to the server.
		sendData = sentence.getBytes();

		//before we do anything with the packet, we need to send the data through our gremlin function
		//to potentially damage it
		gremlinFunction(sendData, probabilityOfDamage);

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPortNumber);
		System.out.print(sendPacket);

		System.out.println(sendPacket.getData());
		clientSocket.send(sendPacket); 
		scan.close();

		DatagramPacket receivePacket;

		receivePacket = new DatagramPacket(receiveData, receiveData.length); 
		clientSocket.setSoTimeout(2000);
		try{
		clientSocket.receive(receivePacket);
		} catch(SocketTimeoutException e){
			System.out.println("Too much time has gone by, connection is closed.");
			clientSocket.close();
		}
		
		if(!clientSocket.isClosed()){			
		

		String receivedPacketData = new String(receivePacket.getData());
		System.out.print(receivedPacketData);
		String[] receivedPacketHeaderData = receivedPacketData.split("[ ]");
		String receivedChecksumData = receivedPacketHeaderData[10];
		int receivedChecksum = Integer.parseInt(receivedChecksumData.replaceAll("[^0-9]",""));

		if(receivedPacketHeaderData.length > 11){

			//Receiving the header information
			int indexOfcrlf = 0;
	
			for(int i = 0; i < 6; i++){
				indexOfcrlf = receivedPacketData.indexOf('\n', indexOfcrlf + 1);
		
			}
			String fileData = receivedPacketData.substring(indexOfcrlf + 1);
			int eos = -1;
			while(eos == -1){

				receivePacket = new DatagramPacket(receiveData, receiveData.length); 
				checksum = checksumCalc(receiveData);

				clientSocket.receive(receivePacket);
				receivedPacketData = new String(receivePacket.getData());

				if (checksum != receivedChecksum) {
					System.out.println("\nThere is an error in this packet!!!\n");
				}

				System.out.println(receivedPacketData);
				//clear the received data
				receiveData = emptyData.clone();				

				//eos - end of stream, which indicates the first occurrence of an null byte 
				eos = receivedPacketData.indexOf(0);
				fileData = fileData + receivedPacketData;
			}
			clientSocket.close();
			
		} else {
			file = null;
			System.out.println("Invalid response from Server.");
		}
	} 
	}


	/**
	 * @param probability
	 * @return Datagrampacket to be sent to server
	 */
	public static byte[] gremlinFunction(byte[] sendData, double probability) {

		double probOneByte = 0.5;
		double probTwoBytes = 0.3;
		double probThreeBytes = 0.2;

		double max = 1D;
		double min = 0D;
		Random random = new Random();
		double randomProbabilityDamage = random.nextDouble() * (max - min) + min;
		double randomProbabilityBytes = random.nextDouble() * (max - min) + min;

		if (randomProbabilityDamage <= probability) {

			int newMax = sendData.length;
			int newMin = 0;
			byte[] randomByte = new byte[1];
			int randomByteLocation;

			if (randomProbabilityBytes <= probThreeBytes) {

				randomByteLocation = random.nextInt((newMax - newMin) + 1) + newMin;
				random.nextBytes(randomByte);
				sendData[randomByteLocation] = randomByte[0];
				randomByte[0] = 0;

				randomByteLocation = random.nextInt((newMax - newMin) + 1) + newMin;
				random.nextBytes(randomByte);
				sendData[randomByteLocation] = randomByte[0];
				randomByte[0] = 0;

				randomByteLocation = random.nextInt((newMax - newMin) + 1) + newMin;
				random.nextBytes(randomByte);
				sendData[randomByteLocation] = randomByte[0];

			}

			else if (randomProbabilityBytes <= probTwoBytes) {

				randomByteLocation = random.nextInt((newMax - newMin) + 1) + newMin;
				random.nextBytes(randomByte);
				sendData[randomByteLocation] = randomByte[0];
				randomByte[0] = 0;

				randomByteLocation = random.nextInt((newMax - newMin) + 1) + newMin;
				random.nextBytes(randomByte);
				sendData[randomByteLocation] = randomByte[0];

			}

			else {

				randomByteLocation = random.nextInt((newMax - newMin) + 1) + newMin;
				random.nextBytes(randomByte);
				sendData[randomByteLocation] = randomByte[0];
			}

		}

		return sendData;
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