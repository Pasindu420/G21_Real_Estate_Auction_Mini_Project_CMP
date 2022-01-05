// Andrew Shaffer (G00688109)

public class Message {
	private  String name;
	private  String message;


	public Message(String recipient, String text)
	{
		name = recipient;
		message = text;
	}

	public String getName()
	{ return name;}

	public void setName(String recipient)
	{ name = recipient; }



	public String getMessage()
	{ return message;}

	public void setMessage(String text)
	{ message = text; }

}