// Andrew Shaffer (G00688109)

public class AuctionItem {
	private  int itemNumber;
	private  String itemName;
	private int highestBid;
	private String highestBidder;

	public AuctionItem(int itemNum, String name)
	{
		itemNumber = itemNum;
		itemName = name;
		highestBid = 0;
		highestBidder = null;
	}

	public int getItemNumber()
	{ return itemNumber;}

	public void setItemNumber(int num)
	{ itemNumber = num; }

	public String getItemName()
	{ return itemName;}

	public void setItemName(String name)
	{ itemName = name; }

	public int getHighestBid()
	{ return highestBid;}

	public void setHighestBid(int num)
	{ highestBid = num; }

	public String getHighestBidder()
	{ return highestBidder;}

	public void setHighestBidder(String name)
	{ highestBidder = name; }

}