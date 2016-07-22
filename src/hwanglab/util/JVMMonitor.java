package hwanglab.util;

import java.io.IOException;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import javax.management.MBeanServer;

/**
 * A JVMMonitor collections information about the Java virtual machine.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class JVMMonitor {

	/**
	 * The interface for the thread system of the Java virtual machine.
	 */
	ThreadMXBean threadSystem;

	/**
	 * The interface for the runtime system of the Java virtual machine.
	 */
	RuntimeMXBean runtimeSystem;

	/**
	 * The interface for the memory system of the Java virtual machine.
	 */
	MemoryMXBean memorySystem;

	/**
	 * The number of processors available to the Java virtual machine.
	 */
	int numberProcessors;

	/**
	 * The previous up time.
	 */
	long previousUpTime;

	/**
	 * A map that associates each thread with the previous CPU time.
	 */
	HashMap<Long, Long> thread2CpuTime = new HashMap<Long, Long>();

	/**
	 * Constructs a JVMMonitor.
	 * 
	 * @throws IOException
	 *             if an error occurs in accessing the system information.
	 */
	public JVMMonitor() throws IOException {
		MBeanServer mbsc = ManagementFactory.getPlatformMBeanServer();
		threadSystem = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.THREAD_MXBEAN_NAME,
				ThreadMXBean.class);
		runtimeSystem = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.RUNTIME_MXBEAN_NAME,
				RuntimeMXBean.class);
		memorySystem = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.MEMORY_MXBEAN_NAME,
				MemoryMXBean.class);
		numberProcessors = ManagementFactory.newPlatformMXBeanProxy(mbsc,
				ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class).getAvailableProcessors();
	}

	/**
	 * Returns the current CPU utilization.
	 * 
	 * @return the current CPU utilization.
	 */
	public double cpuUtilization() {
		long upTime = runtimeSystem.getUptime();
		HashMap<Long, Long> thread2CpuTime = new HashMap<Long, Long>();
		double cpuUtilization = 0;
		for (ThreadInfo threadInfo : threadSystem.dumpAllThreads(false, false)) {
			long tid = threadInfo.getThreadId();
			Long cpuTime = threadSystem.getThreadCpuTime(tid);
			if (cpuTime != null) {
				if (thread2CpuTime.containsKey(tid)) {
					cpuUtilization += Math.min(1, (cpuTime - thread2CpuTime.get(tid))
							/ ((upTime - previousUpTime) * 1000000.0));
				}
				thread2CpuTime.put(tid, cpuTime);
			}
		}
		this.thread2CpuTime = thread2CpuTime;
		previousUpTime = upTime;
		return cpuUtilization / numberProcessors;
	}

	/**
	 * Returns the current memory used in bytes.
	 * 
	 * @return the current memory used in bytes.
	 */
	public long memoryUsed() {
		return memorySystem.getHeapMemoryUsage().getUsed() + memorySystem.getNonHeapMemoryUsage().getUsed();
	}

}
