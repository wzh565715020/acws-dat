package com.tyyd.framework.dat.core.commons.utils;

import java.math.BigDecimal;
import java.math.MathContext;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.jvmmonitor.JVMConstants;
import com.tyyd.framework.dat.jvmmonitor.JVMMonitor;

public class ResourceUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtils.class);
	
	public static boolean isMachineResEnough(AppContext appContext) {

		boolean enough = true;
		try {
			// 1. Cpu usage
			Double maxCpuTimeRate = appContext.getConfig().getParameter(Constants.LB_CPU_USED_RATE_MAX, 90d);
			Object processCpuTimeRate = JVMMonitor.getAttribute(JVMConstants.JMX_JVM_THREAD_NAME, "ProcessCpuTimeRate");
			if (processCpuTimeRate != null) {
				Double cpuRate = Double.valueOf(processCpuTimeRate.toString()) / (Constants.AVAILABLE_PROCESSOR * 1.0);
				if (cpuRate >= maxCpuTimeRate) {
					LOGGER.info("Pause Pull, CPU USAGE is " + String.format("%.2f", cpuRate) + "% >= "
							+ String.format("%.2f", maxCpuTimeRate) + "%");
					enough = false;
					return false;
				}
			}

			// 2. Memory usage
			Double maxMemoryUsedRate = appContext.getConfig().getParameter(Constants.LB_MEMORY_USED_RATE_MAX, 90d);
			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long usedMemory = runtime.totalMemory() - runtime.freeMemory();

			Double memoryUsedRate = new BigDecimal(usedMemory / maxMemory, new MathContext(4)).doubleValue();

			if (memoryUsedRate >= maxMemoryUsedRate) {
				LOGGER.info("Pause Pull, MEMORY USAGE is " + memoryUsedRate + " >= " + maxMemoryUsedRate);
				enough = false;
				return false;
			}
			enough = true;
			return true;
		} catch (Exception e) {
			LOGGER.warn("Check Machine Resource error", e);
			return true;
		} finally {
			Boolean machineResEnough = appContext.getConfig().getInternalData(Constants.MACHINE_RES_ENOUGH, true);
			if (machineResEnough != enough) {
				appContext.getConfig().setInternalData(Constants.MACHINE_RES_ENOUGH, enough);
			}
		}
	}
}
