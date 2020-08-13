import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestClass {

	public void Test() {
		Map<Integer, Object> test = new ConcurrentHashMap<Integer, Object>();
		test.values();
		synchronized (test) {
			if (test.containsKey(new Object())) {
				return;
			}
			test.put(0, new Object());
		}
	}
	
	public static void main(String[] args) {
		// Solution to getting true from asm api is:
		// -Dcoremod.mcmt.synconly.entitiesbyid=true -Dtrue=true
		// BECAUSE OF COURSE IT IS
		System.out.println(System.getProperty("coremod."+"mcmt.synconly.entitiesbyid", "TRUE"));
		System.out.println(Boolean.getBoolean(System.getProperty("coremod."+"mcmt.synconly.entitiesbyid", "TRUE")));
		System.out.println(System.getProperties());
	}
}
