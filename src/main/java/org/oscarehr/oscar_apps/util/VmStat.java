/*
 * Copyright (c) 2007-2008. MB Software Vancouver, Canada. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2 
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 
 * 
 * This software was written for 
 * MB Software, margaritabowl.com
 * Vancouver, B.C., Canada 
 */

package org.oscarehr.oscar_apps.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 */
public class VmStat
{
	private static final Logger logger=LogManager.getLogger(VmStat.class);
	
	private static List<MemoryPoolMXBean> memoryPools=ManagementFactory.getMemoryPoolMXBeans();
	private static ThreadMXBean threadMXBean=ManagementFactory.getThreadMXBean();
	private static OperatingSystemMXBean operatingSystemMXBean=ManagementFactory.getOperatingSystemMXBean();
	private static List<GarbageCollectorMXBean> garbageCollectorMXBeans=ManagementFactory.getGarbageCollectorMXBeans();

	private static Timer timer=new Timer(VmStat.class.getName(), true);
	private static TimerTask timerTask=null;
	private static int counter=0;	
	
	public static synchronized void startContinuousLogging(long logPeriodMilliseconds)
	{
		if (timerTask!=null) throw(new IllegalStateException("ContinuousLogging already started."));
		
		timerTask=new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					logAll();
				}
				catch (Exception e)
				{
					logger.error("Unexpected error.", e);
				}
			}
		};
		
		timer.schedule(timerTask, 0, logPeriodMilliseconds);
	}
	
	public static synchronized void stopContinuousLogging()
	{
		if (timerTask!=null)
		{
			timerTask.cancel();
			timerTask=null;
		}
	}
	
	public static String getMemoryFormat()
	{
		StringBuilder sb=new StringBuilder();
		
		for (MemoryPoolMXBean memoryPool : memoryPools)
		{
			if (sb.length()>0) sb.append(',');
			sb.append(memoryPool.getName());
			sb.append(".maxAllowed(bytes),");
			sb.append(memoryPool.getName());
			sb.append(".currentUsage(bytes)");
		}		
		
		return(sb.toString());
	}
	
	public static String getMemoryInfo(MemoryPoolMXBean memoryPool)
	{
		StringBuilder sb=new StringBuilder();
		
		MemoryUsage memoryUsage=memoryPool.getUsage();

		sb.append(memoryUsage.getMax());
		sb.append(',');

		sb.append(memoryUsage.getUsed());

		return(sb.toString());
	}
	
	public static String getMemoryInfo()
	{
		StringBuilder sb=new StringBuilder();
		
		for (MemoryPoolMXBean memoryPool : memoryPools)
		{
			if (sb.length()>0) sb.append(',');
			sb.append(getMemoryInfo(memoryPool));
		}		
		
		return(sb.toString());
	}
	
	public static String getThreadFormat()
	{
		return("PeakThreadCount,ThreadCount(includes deamons),DaemonThreadCount");
	}
	
	public static String getThreadInfo()
	{
		StringBuilder sb=new StringBuilder();

		sb.append(threadMXBean.getPeakThreadCount());
		sb.append(',');
		sb.append(threadMXBean.getThreadCount());
		sb.append(',');
		sb.append(threadMXBean.getDaemonThreadCount());
		
		return(sb.toString());
	}
	
	public static String getCpuFormat()
	{
		return("Load Average (last minute)");
	}
	
	public static String getCpuInfo()
	{
		return(String.valueOf(operatingSystemMXBean.getSystemLoadAverage()));
	}
	
	public static String getGCFormat()
	{
		StringBuilder sb=new StringBuilder();
		
		for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans)
		{
			if (sb.length()>0) sb.append(',');
			sb.append(garbageCollectorMXBean.getName());
			sb.append(".CollectionTime(ms),");
			sb.append(garbageCollectorMXBean.getName());
			sb.append(".CollectionCount");
		}

		return(sb.toString());
	}
	
	public static String getGCInfo(GarbageCollectorMXBean garbageCollectorMXBean)
	{
		StringBuilder sb=new StringBuilder();

		sb.append(garbageCollectorMXBean.getCollectionTime());
		sb.append(',');
		sb.append(garbageCollectorMXBean.getCollectionCount());
		
		return(sb.toString());
	}
	
	public static String getGCInfo()
	{
		StringBuilder sb=new StringBuilder();
		
		for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans)
		{
			if (sb.length()>0) sb.append(',');
			sb.append(getGCInfo(garbageCollectorMXBean));
		}
		
		return(sb.toString());
	}
	
	public static void logAll()
	{
		if (counter%10==0) logAllFormat();
		logAllData();
		counter++;
	}

	public static void logAllData()
	{
		logger.info("DATA,"+getMemoryInfo()+','+getGCInfo()+','+getThreadInfo()+','+getCpuInfo());
	}
	
	public static void logAllFormat()
	{
		logger.info("HEADER,"+getMemoryFormat()+','+getGCFormat()+','+getThreadFormat()+','+getCpuFormat());
	}
	
	public static void main(String... argv) throws Exception
	{
		startContinuousLogging(5000);
		
		Thread.sleep(60000);
	}
}