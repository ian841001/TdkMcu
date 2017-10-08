package ian.main.jni;

public class JniTest {
	static {
        System.loadLibrary("HelloWorld");
    }
	private native void print();
	
	public static void test() {
		new JniTest().print();
	}
}
