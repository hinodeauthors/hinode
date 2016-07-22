package hwanglab.util;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

import javax.management.MBeanServer;

/**
 * A ThreadMonitor periodically prints information about all live Threads.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class ThreadMonitor {

	/**
	 * Constructs a ThreadMonitor.
	 * 
	 * @param s
	 *            a PrintStream on which information about live Threads are printed.
	 */
	public ThreadMonitor(final PrintStream s) {

		new Thread() {

			public void run() {
				try {
					MBeanServer mbsc = ManagementFactory.getPlatformMBeanServer();
					ThreadMXBean threadProxy = ManagementFactory.newPlatformMXBeanProxy(mbsc,
							ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
					while (true) {
						ThreadInfo[] tis = threadProxy.dumpAllThreads(false, false);
						for (int i = 0; i < tis.length; i++) {
							ThreadInfo t = tis[i];
							s.println("Thread: " + t.getThreadName() + " " + Arrays.toString(t.getStackTrace()));
						}
						System.out.println();
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.start();
	}

}
