
public class HTTPHeader {
	private String mContentLength;
	private String mContentType;
	private int mStatusCode;
	private int mSequenceNum;
	private int mChecksum;
	private final String mHTTPversion = "HTTP/1.0";


	public HTTPHeader(String contentLength, int statusCode, String contentType, int sequenceNum, int checksum){
		mContentLength = contentLength;
		mStatusCode = statusCode;
		mContentType = contentType;
		mSequenceNum = sequenceNum;
		mChecksum = checksum;
	}

	public String statusCodePhraseGenerator(int code){
		switch(code){
		case 200:
			return "Document As Follows";
		case 404:
			return "File Not Found";
		case 400:
			return "Entered Invalid Request";
		default:
			return "Invalid Status Code";
		}
	}
		
	public int getmStatusCode() {
		return mStatusCode;
	}

	public void setmStatusCode(int mStatusCode) {
		this.mStatusCode = mStatusCode;
	}

	public String toString(){
		String header = mHTTPversion + " " + mStatusCode + " "+statusCodePhraseGenerator(mStatusCode) + "\r\n" +
	"Content-Type: " +mContentType+"\r\n"+"Content-Length: " + mContentLength+"\r\n" + "Sequence Number: " 
	+ mSequenceNum +"\r\n" + "Checksum value: " + mChecksum +"\r\n";
		return header;
	}
	
	
	
}
