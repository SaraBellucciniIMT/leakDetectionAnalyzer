package rpstTest;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.CollaborativeAlg;
import io.BpmnParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import spec.mcrl2obj.mCRL2;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		
		return new TestSuite(AppTest.class);
	}



	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}
}
