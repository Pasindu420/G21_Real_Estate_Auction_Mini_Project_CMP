import java.io.Serializable;


public class AuctionHouseUsers implements Serializable {

    String userName;
    String password;

    public AuctionHouseUsers(String aUserName, String aPassword)
    {
        this.userName = aUserName;
        this.password = aPassword;
    }
    public String getUserName()
    {
        return userName;
    }
    public void setUserName(String aUserName)
    {
        this.userName = aUserName;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String aPassword)
    {
        this.password = aPassword;
    }
}