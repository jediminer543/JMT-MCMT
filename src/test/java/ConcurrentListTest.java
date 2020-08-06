import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jmt.mcmt.paralelised.ConcurrentDoublyLinkedList;

public class ConcurrentListTest {

	public static void main(String[] args) {
		System.out.println("Testing...");
		for (int i = 0; i < 100; i++) {
			System.out.println("Test " + i + " of " + 100);
			Random r = new Random();
			List<Integer> golden = new LinkedList<Integer>();
			List<Integer> tested = new ConcurrentDoublyLinkedList<Integer>();
			try {
				for (int j = 0; j < 1000; j++) {
					int op = r.nextInt(4);
					if (op == 0) {
						int value = r.nextInt(256);
						golden.remove((Integer)value);
						tested.remove((Integer)value);
					} else {
						int value = r.nextInt(256);
						golden.add(value);
						tested.add(value);
					}
				}
				assert golden.size() == tested.size();
				for (int k = 0; k < golden.size(); k++) {
					if (!(golden.get(k).equals(tested.get(k)))) {
						System.out.println(k + ":" + golden.get(k) + "|" + tested.get(k));
						assert false;
					}
				}
				System.out.println("Test passed");
			} catch (AssertionError ae) {
				System.out.print("[");
				for (Integer k : golden) {
					System.out.print(k+",");
				}
				System.out.print("]\n[");
				for (Integer k : tested) {
					System.out.print(k+",");
				}
				System.out.print("]\n");
				ae.printStackTrace();
			}
		}
	}

}
