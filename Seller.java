// Andrew Shaffer (G00688109)

import java.io.*;
import java.net.*;

public class Seller implements Runnable {

  private static Socket clientSocket = null;
  private static PrintStream os = null;
  private static InputStreamReader reader = null;
  private static BufferedReader br = null;
  private static BufferedReader inputLine = null;
  private static boolean closed = false;

  public static void main(String[] args) {

    int sellerPort = 2221;
    String host = "localhost";


    //Try to open socket
    try {
      clientSocket = new Socket(host, sellerPort);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      reader = new InputStreamReader(clientSocket.getInputStream());
      br = new BufferedReader(reader);

	} catch(Exception e){
    	e.printStackTrace();
	}


    // if all initialized
    if (clientSocket != null && os != null && br != null) {
      try {

        //start thread
        new Thread(new Seller()).start();

        //allow writing data to socket until closed
        while (!closed) {
          os.println(inputLine.readLine().trim());
        }

        //closes socket,streams, etc
        os.close();
        reader.close();
        br.close();
        clientSocket.close();

      } catch (IOException e) {
        	e.printStackTrace();
      }
    }
  }


  public void run() {

    String responseLine;
    try {

      //loop to read data from server and print
      while ((responseLine = br.readLine()) != null) {
        System.out.println(responseLine);

        //until this response received
        if (responseLine.indexOf("*** Disconnected") != -1)
          break;
      }
      closed = true;
    } catch (IOException e) {
      	e.printStackTrace();
    }
  }
}