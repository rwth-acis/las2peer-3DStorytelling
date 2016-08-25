package i5.las2peer.services.myNewService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import i5.cae.semanticCheck.SemanticCheckResponse;
import i5.cae.simpleModel.SimpleModel;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.story3DSemanticCheck.Story3DSemanticCheck;
import i5.las2peer.services.story3DSemanticCheck.model.Model;
import i5.las2peer.services.story3DSemanticCheck.x3dst.Story;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
public class ServiceTest {

	private static final String HTTP_ADDRESS = "http://127.0.0.1";
	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;

	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static UserAgent testAgent;
	private static final String testPass = "adamspass";

	private static final String mainPath = "template/";

    private static String FILE_NAME_CORRECT = "./exampleModels/story_correct.json";
    private static String FILE_NAME_END_SKIP = "./exampleModels/story_end_skip.json";
    private static String FILE_NAME_NO_START = "./exampleModels/story_no_start.json";
    private static String FILE_NAME_DEAD_END = "./exampleModels/story_dead_end.json";
    private static Story story_correct = null;
    private static Story story_end_skip = null;
    private static Story story_no_start = null;
    private static Story story_dead_end = null;
	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used throughout the tests.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void startServer() throws Exception {

		JSONParser parser = new JSONParser();
		story_correct = new Story((SimpleModel) new Model(((JSONObject) parser.parse(new FileReader(FILE_NAME_CORRECT))).toJSONString()).getMinifiedRepresentation());
		story_end_skip = new Story((SimpleModel) new Model(((JSONObject) parser.parse(new FileReader(FILE_NAME_END_SKIP))).toJSONString()).getMinifiedRepresentation());
		story_no_start = new Story((SimpleModel) new Model(((JSONObject) parser.parse(new FileReader(FILE_NAME_NO_START))).toJSONString()).getMinifiedRepresentation());
		story_dead_end = new Story((SimpleModel) new Model(((JSONObject) parser.parse(new FileReader(FILE_NAME_DEAD_END))).toJSONString()).getMinifiedRepresentation());
	}

	/**
	 * Called after the tests have finished. Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer() throws Exception {
	}
	
	@Test 
	public void correctModel() {
		try {
			SemanticCheckResponse res =	story_correct.validate();
			assertEquals(res.getError(), 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: "+e);
		}
	}
	
	@Test 
	public void endSkipModel() {
		try {
			SemanticCheckResponse res =	story_end_skip.validate();
			assertEquals(Story.ERROR_INVALID_TRANSITION, res.getError());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: "+e);
		}
	}
	
	@Test 
	public void noStartModel() {
		try {
			SemanticCheckResponse res =	story_no_start.validate();
			assertEquals(Story.ERROR_NO_BEGIN, res.getError());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: "+e);
		}
	}	
	
	@Test 
	public void deadEndModel() {
		try {
			SemanticCheckResponse res =	story_dead_end.validate();
			assertEquals(Story.ERROR_DEAD_END, res.getError());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: "+e);
		}
	}		
	
	/**
	 * 
	 * Tests the validation method.
	 * 
	 */
	/*
	@Test
	public void testGet() {
		if (true) {
			return;
		}
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			ClientResponse result = c.sendRequest("GET", mainPath + "get", "");
			assertEquals(200, result.getHttpCode());
			assertTrue(result.getResponse().trim().contains("result")); // YOUR RESULT VALUE HERE
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}*/

	
	/**
	 * 
	 * Test the example method that consumes one path parameter which we give the value "testInput" in this test.
	 * 
	 */
	/*
	@Test
	public void testPost() {
		if (true) {
			return;
		}
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			ClientResponse result = c.sendRequest("POST", mainPath + "post/testInput", ""); // testInput is
																							// the pathParam
			assertEquals(200, result.getHttpCode());
			assertTrue(result.getResponse().trim().contains("testInput")); // "testInput" name is part of response
			System.out.println("Result of 'testPost': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}
	}*/

	/**
	 * Test the TemplateService for valid rest mapping. Important for development.
	 */
	/*
	@Test
	public void testDebugMapping() {
		MyNewService cl = new MyNewService();
		assertTrue(cl.debugMapping());
	}
*/
}
