package org.jmt.mcmt.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class JMXRegistration {

	public static void register() {
	    try {
	    	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
			ObjectName debugName = new ObjectName("org.jmt.mcmt:type=MCMTDebug");
			MCMTDebug debugBean = new MCMTDebug();
			mbs.registerMBean(debugBean, debugName);
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
