// Andrew Shaffer (G00688109)

import java.io.*;
import java.net.*;
import java.util.*;

public class AuctionServer {

  private static ServerSocket serverBuyerSocket = null;
  private static ServerSocket serverSellerSocket = null;

  private static Socket buyerSocket = null;
  private static Socket sellerSocket = null;

  private static final int maxThreadCount = 7;
  private static final clientThread[] clientThreads = new clientThread[maxThreadCount];


  public static void main(String args[]) {

    int sellerPort = 2221;
    int buyerPort = 2222;

	LinkedList<AuctionItem> auctionList = new LinkedList<AuctionItem>();
	LinkedList<Message> messageQueue = new LinkedList<Message>();

    //Open a server socket for ports & set timeout
    try {
      System.out.println("TCP Server Initialized...");

      serverBuyerSocket = new ServerSocket(buyerPort);
      serverBuyerSocket.setSoTimeout(500);
      System.out.println("Waiting for client buyer connections...");

      serverSellerSocket = new ServerSocket(sellerPort);
      serverSellerSocket.setSoTimeout(500);
      System.out.println("Waiting for seller client connections...");

    } catch (IOException e) {
    	e.printStackTrace();
	}

	// Create a client socket for each connection and pass it to a new client thread.
    while (true) {

		// buyersocket connect attempt
		try {

			buyerSocket = serverBuyerSocket.accept();
			System.out.println("Buyer client has connected...");
			for (int i = 0; i < maxThreadCount; i++) {
			  if (clientThreads[i] == null) {
				(clientThreads[i] = new clientThread(buyerSocket, clientThreads,false,auctionList,messageQueue)).start();
				break;
			  }
			}
		}
		catch (IOException e) {
		//System.out.println(e);
		}

		//seller socket connect attempt
		try	{

		  sellerSocket = serverSellerSocket.accept();

		  if (ifSellerConnected()) {
		  //refuse connection if seller is already connected

			PrintStream os = new PrintStream(sellerSocket.getOutputStream());
			os.println("** Seller Client Connection Limit Reached. Please try again later");
			System.out.println("Seller client connection refused - Limit reached.");
			os.close();
			sellerSocket.close();
		  }
		  else {
		  //add seller to first empty thread

			  for (int i = 0; i < maxThreadCount; i++) {
				if (clientThreads[i] == null) {
				  (clientThreads[i] = new clientThread(sellerSocket, clientThreads,true,auctionList,messageQueue)).start();
				  System.out.println("Seller client has connected...");
				  break;
				}
			  }
		  }

		}
		catch (IOException e) {
			//System.out.println(e);
		}
      }
    }

	private static boolean ifSellerConnected() {

		for (int i = 0; i < maxThreadCount; i++) {

			if(clientThreads[i] != null) {

				if(clientThreads[i].isSeller == true)
					return true;
			}
		}
		return false;
	}

}

// The client thread used for both buyers and sellers.

class clientThread extends Thread {

  public boolean isSeller;
  public String name = "";
  private BufferedReader br = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  private boolean loggedIn = false;
  private LinkedList<AuctionItem> auctionList;
  private LinkedList<Message> messageQueue;

  public clientThread(Socket clientSocket, clientThread[] threads, boolean isSeller, LinkedList<AuctionItem> list, LinkedList<Message> mesQueue) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    this.isSeller = isSeller;
    maxClientsCount = threads.length;
    auctionList = list;
    messageQueue = mesQueue;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      // Create input and output streams for client
      br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      os = new PrintStream(clientSocket.getOutputStream());

      //prompt user to login
      this.os.println("*** Connected to server ***");
      this.os.println("*** Please login to continue ***");

	  String preLoginLine = "";
      while (!loggedIn) {
        preLoginLine = br.readLine().toLowerCase();
		if (preLoginLine.equals(""))
			// do nothing
			{ }
		else if (preLoginLine.startsWith("/quit")) {
			//quit program
			break;
		}
		else if(preLoginLine.startsWith("login")) {
			//attempt to login
			loggedIn = this.login(preLoginLine);
	    }
        else {
        	// any other command entered
			this.os.println("> You must login before the system will process any other commands");
			System.out.println("Unregistered client sent command: " + preLoginLine);
		}

      }

	  checkForMessages();

      // accept messages
      while (true) {
        String line = br.readLine().toLowerCase();
        if (line.equals(""))
        { /* do nothing */ }
        else if (line.startsWith("/quit")) {
          break;
        }
        else if(line.startsWith("login")) {
			this.os.println("> You are already logged in as " + name);
        }
        else if(line.startsWith("list")) {
			list(auctionList);
        }
        else if(line.startsWith("bid"))	{

			if(isSeller) {
				System.out.println("Seller attempted to call bid function: " + line);
				this.os.println("> Seller client cannot call the bid function");
			}
			else
				bid(line);
        }
        else if(line.startsWith("add"))	{

			if(!isSeller) {
				System.out.println("Buyer (" + name + ") attempted to call ADD function: " + line);
				this.os.println("> Buyer client cannot add items to the list");
			}
			else
				add(line);

        }
        else if(line.startsWith("sell")) {

			if(!isSeller) {
				System.out.println("Buyer (" + name + ") attempted to call SELL function: " + line);
				this.os.println("> Buyer client cannot close auctions in list");
			}
			else
				sell(line);
        }
		else {
			this.os.println(line + " is not a recongnized commmand");
			System.out.println(name + " client sent unrecognized command: " + line);
		}
	 }

	 this.os.println("*** Disconnected from server ***");

     //remove thread from array
     synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }

      //close connections
      br.close();
      os.close();
      clientSocket.close();
      System.out.println(name + ": disconnected");

    } catch (IOException e) {
    }
  }

  boolean login(String input)
  {
	String[] loginName = input.split(" ",2);

	try {
		//if username is valid (in authorized list)
		if(isSeller && loginName[1].equals("seller") || !isSeller && "alice,bob,dave,pam,susan,tom,mike".contains(loginName[1])) {

			synchronized (this) {

				//check to see if already logged on
				for (int i = 0; i < maxClientsCount; i++) {
					// if so
					if (threads[i] != null && threads[i].name.equals(loginName[1])) {
						// refuse connection
						this.os.println("> Username: " + loginName[1] + " already logged in. ");
						System.out.println("Duplicate username login attempt for username: " + loginName[1]);
						return false;
					}
				}
			}

			//else continue logging in
			this.name = loginName[1];

			if(name.equals("seller")) {
				this.os.println("> Logged in to Seller Client of Auction Service as : " + this.name );
				System.out.println(this.name + ": logged in as seller client" );
			}
			else {
				this.os.println("> Logged in to Buyer Client of Auction Service as " + this.name);
				System.out.println(this.name + ": logged in as buyer client" );
	 		}
			return true;
		}
		else {
			// username is not valid
			this.os.println("> Invalid username: " + loginName[1] + ".  Please try again.");
			System.out.println("Invalid login attempt for username: " + loginName[1]);
			return false;
		}

	} catch (ArrayIndexOutOfBoundsException ex) {
		this.os.println("> Invalid use of command: Login");
		this.os.println("> Syntax: login <username>");
		System.out.println("Invalid use of command: " + input);
		return false;
	}

  }

  void add(String input) {

	String[] element = input.split(" ",3);

	try {
		System.out.println(name + ": sent add command: " + input);

		 synchronized (this) {
			 AuctionItem poop = new AuctionItem(Integer.parseInt(element[1]),element[2]);
			ListIterator<AuctionItem> iterator = auctionList.listIterator();
			AuctionItem temp = null;
			boolean found = false;

			//iterate through auction list
			while(iterator.hasNext()) {

				temp = iterator.next();
				if(temp.getItemNumber() == poop.getItemNumber()) {
					//if item number already in use update boolean and end loop
					found = true;
					break;
				}
			}

			if(!found) {
				//add new item to auction list if item number is not in use
				auctionList.add(poop);
				this.os.println("> Added " + poop.getItemNumber() + "/" + poop.getItemName() + " to Auction List");
				System.out.println(name + ": added " + poop.getItemNumber() + "/" + poop.getItemName() + " to Auction List");
			}
			else {
				// item number already in use
				System.out.println("Seller to call ADD function for item number that already exists: " + input);
				this.os.println("> Item Number already exists for : " + input);
			}
		}

	} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
			this.os.println("> ** Invalid use of command: Add");
			this.os.println("> ** Syntax: add <item number> <item description>");
			System.out.println(name + ": Invalid use of command: " + input);
	}

  }

  void list(LinkedList<AuctionItem> auctionList) {

	System.out.println(name + ": sent list command");

	if(auctionList.size() == 0)
		// AuctionList empty
		this.os.println("> There are no items currently for auction.");
	else {
		ListIterator<AuctionItem> iterator = auctionList.listIterator();
		try {
			os = new PrintStream(clientSocket.getOutputStream());
			AuctionItem temp;

			while(iterator.hasNext()) {
				//iterate through and print list
				temp = iterator.next();
				this.os.print("Item Number: " + temp.getItemNumber() + "  Item Name: " + temp.getItemName() + "  Highest Bid: " + temp.getHighestBid() + "  Highest Bidder: ");

				if (temp.getHighestBidder() != null)
					this.os.println(temp.getHighestBidder());
				else
					this.os.println("> None");

			}
		}
		catch (IOException e)
		{ System.out.println(e); }

	}
  }

  void bid(String input) {

	try{
		String[] element = input.split(" ",3);
		System.out.println(name + ": sent bid command: " + input);

		synchronized (this) {
			if(auctionList.size() == 0)
				//empty list
				this.os.println("> There are no items currently for auction.");
			else {
				ListIterator<AuctionItem> iterator = auctionList.listIterator();
				try {
					os = new PrintStream(clientSocket.getOutputStream());
				}
				catch (IOException e)
				{ System.out.println(e); }

				boolean found = false;
				AuctionItem temp;
				while(iterator.hasNext()) {

					temp = iterator.next();

					if (temp.getItemNumber() == Integer.parseInt(element[1])) {
						//if current node has same item number as bid attempt
						found = true;
						if(temp.getHighestBidder() == null || temp.getHighestBid() < Integer.parseInt(element[2])) {
							//if no one bid on or new bid is higher than current highest bid

							// find and notify current highest bidder

							//update previous bidder
							String message = "> You've been outbid for item " + temp.getItemNumber();
							sendMessages(temp,message);

							//udpate highest bidder + amout for item
							temp.setHighestBidder(name);
							temp.setHighestBid(Integer.parseInt(element[2]));
							//and inform the bidder
							this.os.println("> You have the highest bid of " + Integer.parseInt(element[2]) + " for item " + temp.getItemNumber());
						}
						else
						// bid was lower than current highest
							this.os.println("> You have underbid for item " + temp.getItemNumber() + ".  Current highest bid: " + temp.getHighestBid());

					}
				}
				if(!found) {
					this.os.println("> Item number " + Integer.parseInt(element[1]) + " does not exist");
				}
			}
		}

	} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
			this.os.println("> ** Invalid use of command: Bid");
			this.os.println("> ** Syntax: bid <item number> <bid amount>");
			System.out.println(name + ": Invalid use of command: " + input);
	}
  }


  void sell(String input) {

	try{
		String[] element = input.split(" ",2);
		System.out.println(name + ": sent sell command: " + input);

		try {

			boolean itemFound = false;
			os = new PrintStream(clientSocket.getOutputStream());
			AuctionItem temp;
			synchronized (this) {
				ListIterator<AuctionItem> iterator = auctionList.listIterator();

				while(iterator.hasNext()) {
				// loop through auction list

					temp = iterator.next();
					if (temp.getItemNumber() == Integer.parseInt(element[1])) {

						itemFound = true;

						//remove it from list
						auctionList.remove(temp);

						// if has been bid on
						if(temp.getHighestBidder() != null) {

							//update winner
							String message = "You've won the auction for item " + temp.getItemNumber();
							sendMessages(temp,message);

							//update seller and server
							this.os.println("Auction closed for item: " + temp.getItemNumber() + " Winner: " + temp.getHighestBidder());
							System.out.println("Auction closed for item: " + temp.getItemNumber() + " Winner: " + temp.getHighestBidder());

						}

						else {
							System.out.println(name + ": closed auction for item: " + temp.getItemNumber());
							this.os.println("> No one bid on auction for item " + temp.getItemNumber());
						}

						break;

					}

				}
			}
			if(!itemFound) {
				this.os.println("> Item number " + element[1] + " does not exist in list");
				System.out.println(name + ": item number " + element[1] + " does not exist");
			}
		}
		catch (IOException e)
		{ System.out.println(e); }


	} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
			this.os.println("> ** Invalid use of command: Sell");
			this.os.println("> ** Syntax: sell <item number>");
			System.out.println(name + ": Invalid use of command: " + input);
	}

  }

  int isConnected(String recipient) {
  // returns index in arraylist if recipient string is connected

	synchronized (this) {
		for (int i = 0; i < maxClientsCount; i++) {
			if (threads[i] != null && threads[i].name.equals(recipient))
				return i;
		}
	}

	return -1;
  }

	void checkForMessages() {
	// checks messageQueue for any messages for logged in user and
	// displays to user and server

		if(messageQueue.size() > 0)
		{
			synchronized (this) {
				ListIterator<Message> messageIterator = messageQueue.listIterator();
				Message tempMessage = null;

				//iterate through message list
				while(messageIterator.hasNext()) {

					tempMessage = messageIterator.next();
					if(tempMessage.getName().equals(name)) {

						System.out.println(tempMessage.getName() +": received Queued Message: " + tempMessage.getMessage());
						this.os.println(tempMessage.getMessage());
						messageIterator.remove();
						System.out.println("Message Queue Size: " + messageQueue.size() );
					}
				}
			}
		}
	}

	void sendMessages(AuctionItem temp, String message)
	// attempts to send message to a different user
	// if connected, sends to user otherwise
	// saves to message queue until user connnects.
	{
		if(temp.getHighestBidder() != null)
			if(isConnected(temp.getHighestBidder()) != -1 )
			{
				threads[isConnected(temp.getHighestBidder())].os.println(message);
			}
			else
			{
				Message msg = new Message(temp.getHighestBidder(), message);
				messageQueue.add(msg);
				System.out.println(temp.getHighestBidder() + ": message delivery failed");
				System.out.println("queue: message added. size: " + messageQueue.size() );
			}
	}
}