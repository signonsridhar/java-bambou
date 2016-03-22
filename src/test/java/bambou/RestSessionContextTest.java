package bambou;

import org.junit.Assert;
import org.junit.Test;

import bambou.testobj.TestRestSession;

public class RestSessionContextTest {
	
	@Test
	public void testSetSession1() throws InterruptedException {
		// Main thread - new session
		RestSession session = new TestRestSession();
		RestSessionContext.session.set(session);
		Assert.assertEquals(session, RestSessionContext.session.get());
		RestSessionContext.session.set(null);
	}

	@Test
	public void testSetSession2() throws InterruptedException {
		// Main thread - new session
		RestSession session = new TestRestSession();
		RestSessionContext.session.set(session);
		Assert.assertEquals(session, RestSessionContext.session.get());
		RestSessionContext.session.set(null);

		// Thread #1 - no session
		MyThread thread = new MyThread();
		thread.start();
		thread.join();
		Assert.assertNull(thread.getSession());
	}

	@Test
	public void testSetSession3() throws InterruptedException {
		// Main thread - no session
		Assert.assertNull(RestSessionContext.session.get());

		// Thread #1 - new session
		RestSession session = new TestRestSession();
		MyThread thread1 = new MyThread(session);
		thread1.start();
		thread1.join();
		Assert.assertEquals(session, thread1.getSession());

		// Thread #2 - no session
		MyThread thread2 = new MyThread();
		thread2.start();
		thread2.join();
		Assert.assertNull(thread2.getSession());
	}
	
	private class MyThread extends Thread {
		private RestSession session;

		public MyThread() {
			this.session = null;
		}

		public MyThread(RestSession session) {
			this.session = session;
		}

		@Override
		public void run() {
			if (session != null) {
				RestSessionContext.session.set(session);
			}

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}

			session = RestSessionContext.session.get();
		}

		public RestSession getSession() {
			return session;
		}
	}
}
