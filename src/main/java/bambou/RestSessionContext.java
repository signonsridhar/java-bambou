package bambou;

public class RestSessionContext {
	public static ThreadLocal<RestSession> session = new ThreadLocal<RestSession>();
}