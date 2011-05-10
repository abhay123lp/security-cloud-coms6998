package coms6998.security;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MonitorThread implements Runnable {

	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	public static final String RESTART_TIME = " 22:";
	private String username;
	private String secGroupName;
	private String keyPairName;
	private String amiName;

	/**
	 * This method retrieves the current system time in the format specified
	 * 
	 * @return current time
	 */
	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	/**
	 * Default constructor for the monitor thread
	 * 
	 * @param user
	 *            - username used as key for creating/identifying a instance
	 * @param group
	 *            - name of the security group to be associated with the
	 *            instance
	 * @param key
	 *            - name of the key pair to be associated with the instance
	 * @param ami
	 *            - name of the AMI snapshot to be created
	 */
	public MonitorThread(String user, String group, String key, String ami) {
		this.username = user;
		this.amiName = ami;
		this.secGroupName = group;
		this.keyPairName = key;
	}

	public void run() {

		// provision the user for the first time
		AwsProvision user1 = AwsProvision.getInstance(this.username,
				this.keyPairName, this.secGroupName, this.amiName);

		while (true) {
			
			// start monitoring the VM for usage
			boolean state = true;
			do {
				// keep checking every 10 mins
				try {
					Thread.sleep(605000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// monitorVM returns false if cpu utilization
				// is less than the threshold to break out of while
				state = user1.monitorVM();

			} while (state);

			// shutdown the VM and bring it up the next morning
			if (!state) {

				System.out.println("Cpu Utilization less than threshold, " +
						"shutting down VM !!");

				// detach volume and create a snapshot before 
				// shutting down the VM
				user1.shutdownVM();

				// sleep till the instance is in terminated state
				try {
					Thread.sleep(120000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// keep checking time, bring it up again at 8:00 am
				String timeNow = now();
				do {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					timeNow = now();
				} while (!timeNow.contains(RESTART_TIME));

				// reload the VM in the morning
				user1.reloadVM();

			} // end of if (!state)

		} // end of while (true)

	} // end of run

} // end of class
