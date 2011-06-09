/**
 * 
 */
package AP2DX;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jjwt
 *
 */
public class ConnectionTestCase extends TestCase {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link AP2DX.Connection#Connection(java.lang.String, int)}.
	 */
	@Test
	public void testConnection() {
		Connection conn = null;
		try {
			conn = new Connection("localhost", 9999);
			assertNotNull(conn);
		}
		catch (Exception ex) {
			fail("Constructor of Connection failed");
		}
	}

	/**
	 * Test method for {@link AP2DX.Connection#sendMessage(java.lang.String)}.
	 */
	@Test
	public void testSendMessage() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link AP2DX.Connection#readMessage()}.
	 */
	@Test
	public void testReadMessage() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link AP2DX.Connection#close()}.
	 */
	@Test
	public void testClose() {
		fail("Not yet implemented");
	}

}