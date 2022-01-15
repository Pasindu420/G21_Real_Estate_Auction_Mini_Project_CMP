public class timer extends Thread {
    long end;
    static String a;
    public timer(long end) {
        this.end = end;
    }

    public void run() {

//    long end = System.currentTimeMillis() + givenTime[1];

    try {
        while (end > System.currentTimeMillis()) {
            int second = 0;
            try {
                String str = Long.toString(end - System.currentTimeMillis()).substring(0, Long.toString(end - System.currentTimeMillis()).length()-3);//calculate remain time for not extend items.
                second = Integer.parseInt(str); // string convert to integer
            } catch (Exception e) {
                second =0; // if there is a exception in the try block second going to zero.
            }
            //Seconds convert to hours minutes and seconds format.
            int h = second / 3600;
            int m = second / 60;
            int s = second % 60;
            System.out.print("Remaining Time to BID is "+h+":"+m+":"+s+"\r");

            a="Remaining BID-Time for not extended items "+h+":"+m+":"+s; // make a this time.

            sleep(1000);// sleep this thread in one second after start.
        }
        System.out.print("\r");
        System.out.println("Bidding Time is Over for Not extended items "); // after reaching the end time show this message to sever.
    } catch (InterruptedException e) {
        e.printStackTrace(); // if there is a exception print the error.
    }
    }}

