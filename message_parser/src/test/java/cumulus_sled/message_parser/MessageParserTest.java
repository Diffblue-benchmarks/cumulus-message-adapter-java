package cumulus_message_adapter.message_parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Unit test for Message Parser test.
 */
public class MessageParserTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MessageParserTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MessageParserTest.class );
    }

    /**
     * Test that the message handler is hitting all of the correct functions and converting the params
     * to JSON correctly
     */
    public void testMessageAdapter()
    {
        MessageParser parser = new MessageParser(new TestMessageAdapter());
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}";
        String expectedOutput = "{\"message_config\":null,\"schemas\":null,\"handler_response\":{\"task\":\"complete\"},\"event\":{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}}}";

        try
        {
            assertEquals(expectedOutput, parser.RunCumulusTask(inputJson, null, new TestTask(false)));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that when passing in schema locations they are serialized to JSON correctly
     */
    public void testSchemaLocations()
    {
        MessageParser parser = new MessageParser(new TestMessageAdapter());
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}";
        String expectedOutput = "{\"message_config\":null,\"schemas\":{\"input\":\"input.json\",\"output\":\"output.json\",\"config\":\"config.json\"},\"handler_response\":{\"task\":\"complete\"},\"event\":{\"schemas\":{\"input\":\"input.json\",\"output\":\"output.json\",\"config\":\"config.json\"},\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}}}";

        try
        {
            System.out.println(parser.RunCumulusTask(inputJson, null, new TestTask(false), "input.json", "output.json", "config.json"));
            assertEquals(expectedOutput, parser.RunCumulusTask(inputJson, null, new TestTask(false), "input.json", "output.json", "config.json"));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that LoadRemoteEvent converts input to JSON correctly
     */
    public void testLoadRemoteEvent()
    {
        TestMessageAdapter messageAdapter = new TestMessageAdapter();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String expectedOutput = "{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";
        
        try
        {
            assertEquals(expectedOutput, messageAdapter.LoadRemoteEvent(inputJson, null));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that LoadNestedEvent converts input to JSON correctly
     */
    public void testLoadNestedEvent()
    {
        TestMessageAdapter messageAdapter = new TestMessageAdapter();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String expectedOutput = "{\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";
        
        try
        {
            assertEquals(expectedOutput, messageAdapter.LoadNestedEvent(inputJson, null, null));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test that CreateNextEvent converts input to JSON correctly
     */
    public void testCreateNextEvent()
    {
        TestMessageAdapter messageAdapter = new TestMessageAdapter();
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}";
        String nestedEventJson = "{\"input\": {\"anykey\": \"anyvalue\"}, \"config\": {\"bar\": \"baz\"}}";
        String taskOutput = "{\"task\":\"complete\"}";
    
        String expectedOutput = "{\"message_config\":null,\"schemas\":null,\"handler_response\":{\"task\":\"complete\"},\"event\":{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}},\"cumulus_meta\":{\"task\":\"Example\",\"message_source\":\"local\",\"id\":\"id-1234\"},\"meta\":{\"foo\":\"bar\"},\"payload\":{\"anykey\":\"anyvalue\"}}}";

        try
        {
            assertEquals(expectedOutput, messageAdapter.CreateNextEvent(inputJson, nestedEventJson, taskOutput, null));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }

    /**
     * Test the response when there is an exception
     */
    public void testException()
    {
        MessageParser parser = new MessageParser(new TestMessageAdapter());
        String inputJson = "{\"workflow_config\":{\"Example\":{\"bar\":\"baz\"}}}";
        String expectedOutput = "{\"payload\":null,\"exception\":\"workflow exception\"}";

        try
        {
            assertEquals(expectedOutput, parser.RunCumulusTask(inputJson, null, new TestTask(true)));
        }
        catch(MessageAdapterException e)
        {
            fail();
        }
    }
}
