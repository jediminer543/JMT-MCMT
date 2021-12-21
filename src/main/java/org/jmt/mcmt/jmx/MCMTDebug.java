package org.jmt.mcmt.jmx;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;

import org.jmt.mcmt.asmdest.DebugHookTerminator;

import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.ModList;

/**
 * MBean for debugging modlaunching issues
 * 
 * @author jediminer543
 *
 */
public class MCMTDebug extends NotificationBroadcasterSupport implements MCMTDebugMBean {

	ClassLoader ccl = null;
	
	public MCMTDebug() {
		// needed because classloading issues; classload all TRANSFORMER classes before stuff
		ccl = Thread.currentThread().getContextClassLoader();
	}
	
	@Override
	public String[] getLoadedMods() {
		Thread.currentThread().setContextClassLoader(ccl);
		return ModList.get().applyForEachModContainer(mc -> mc.getModId()+":"+mc.getModInfo().getVersion().toString()).toArray(String[]::new);
	}

	@Override
	public String getMainChunkLoadStatus() {
		Thread.currentThread().setContextClassLoader(ccl);
		return DebugHookTerminator.mainThreadChunkLoad.get() + ":" + DebugHookTerminator.mainThreadChunkLoadCount.get();
	}

	@Override
	public String[] getBrokenChunkList() {
		Thread.currentThread().setContextClassLoader(ccl);		
		return DebugHookTerminator.breaks.stream().map(bcl -> new ChunkPos(bcl.getChunkPos()).toString()).toArray(String[]::new);
	}
	
	@Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[]{
            AttributeChangeNotification.ATTRIBUTE_CHANGE
        };

        String name = AttributeChangeNotification.class.getName();
        String description = "An attribute of this MBean has changed";
        MBeanNotificationInfo info = 
                new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[]{info};
    }
		
}
