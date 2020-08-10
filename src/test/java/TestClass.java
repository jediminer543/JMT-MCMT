import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestClass {

	public void Test() {
		Map<Integer, Object> test = new ConcurrentHashMap<Integer, Object>();
		test.values();
		test.put(0, new Object());
	}
}
