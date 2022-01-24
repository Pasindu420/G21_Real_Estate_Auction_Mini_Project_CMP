public class AuctionHouseManager{

ArrayList<AuctionHouseUsers> registeredUsers;
        ArrayList<AuctionHouseUsers> logedInUsers;


public AuctionHouseManager()
        {
        logedInUsers = new ArrayList<AuctionHouseUsers>();
        registeredUsers = new ArrayList<AuctionHouseUsers>();
        }
public void registerUser(String username, string password)
        {
        AuctionHouseUsers registeredUser = new AuctionHouseUser(username,password);
        registeredUsers.add(tempUser);
        }
public void logIn(String username, string password)
        {
        AuctionHouseUsers logedInUser = new AuctionHouseUser(username,password);
        logedInUsers.add(logedInUser);
        }}