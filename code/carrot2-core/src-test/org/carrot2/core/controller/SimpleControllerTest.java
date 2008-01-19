/**
 * 
 */
package org.carrot2.core.controller;

import static org.easymock.EasyMock.createStrictControl;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.carrot2.core.*;
import org.carrot2.core.parameter.*;
import org.easymock.IMocksControl;
import org.junit.*;

/**
 *
 */
public class SimpleControllerTest
{
    private IMocksControl mocksControl;

    private ProcessingComponent processingComponent1Mock;
    private ProcessingComponent processingComponent2Mock;
    private ProcessingComponent processingComponent3Mock;

    private SimpleController controller;

    private Map<String, Object> parameters;
    private Map<String, Object> attributes;

    @Bindable
    public static class ProcessingComponent1 extends DelegatingProcessingComponent
    {
        @Parameter(policy = BindingPolicy.INSTANTIATION, key = "delegate1")
        private ProcessingComponent delegate1;

        @Override
        ProcessingComponent getDelegate()
        {
            return delegate1;
        }
    }

    @Bindable
    public static class ProcessingComponent2 extends DelegatingProcessingComponent
    {
        @Parameter(policy = BindingPolicy.INSTANTIATION, key = "delegate2")
        private ProcessingComponent delegate2;

        @Override
        ProcessingComponent getDelegate()
        {
            return delegate2;
        }
    }

    @Bindable
    public static class ProcessingComponent3 extends DelegatingProcessingComponent
    {
        @Parameter(policy = BindingPolicy.INSTANTIATION, key = "delegate3")
        private ProcessingComponent delegate3;

        @Override
        ProcessingComponent getDelegate()
        {
            return delegate3;
        }
    }

    @Bindable
    public static class ProcessingComponentWithoutDefaultConstructor extends
        ProcessingComponentBase
    {
        private ProcessingComponentWithoutDefaultConstructor()
        {
        }
    }

    @Before
    public void init()
    {
        mocksControl = createStrictControl();

        processingComponent1Mock = mocksControl.createMock(DocumentSource.class);
        processingComponent2Mock = mocksControl.createMock(DocumentSource.class);
        processingComponent3Mock = mocksControl.createMock(DocumentSource.class);

        parameters = new HashMap<String, Object>();
        parameters.put("delegate1", processingComponent1Mock);
        parameters.put("delegate2", processingComponent2Mock);
        parameters.put("delegate3", processingComponent3Mock);

        attributes = new HashMap<String, Object>();

        controller = new SimpleController();
    }

    @After
    public void verifyMocks()
    {
        mocksControl.verify();
    }

    @Test
    public void testNormalExecution1Component()
    {
        processingComponent1Mock.init();
        processingComponent1Mock.beforeProcessing();
        processingComponent1Mock.performProcessing();
        processingComponent1Mock.afterProcessing();
        processingComponent1Mock.dispose();

        mocksControl.replay();

        parameters.put("instanceParameter", "i");
        parameters.put("runtimeParameter", "r");

        attributes.put("data", "d");

        controller.process(parameters, attributes, ProcessingComponent1.class);

        mocksControl.verify();

        assertEquals("dir", attributes.get("data"));
    }

    @Test
    public void testNormalExecution3Components()
    {
        processingComponent1Mock.init();
        processingComponent2Mock.init();
        processingComponent3Mock.init();

        processingComponent1Mock.beforeProcessing();
        processingComponent2Mock.beforeProcessing();
        processingComponent3Mock.beforeProcessing();

        processingComponent1Mock.performProcessing();
        processingComponent2Mock.performProcessing();
        processingComponent3Mock.performProcessing();

        processingComponent1Mock.afterProcessing();
        processingComponent2Mock.afterProcessing();
        processingComponent3Mock.afterProcessing();

        processingComponent1Mock.dispose();
        processingComponent2Mock.dispose();
        processingComponent3Mock.dispose();

        mocksControl.replay();

        parameters.put("instanceParameter", "i");
        parameters.put("runtimeParameter", "r");

        attributes.put("data", "d");

        controller.process(parameters, attributes, ProcessingComponent1.class,
            ProcessingComponent2.class, ProcessingComponent3.class);

        assertEquals("diririr", attributes.get("data"));
    }

    @Test(expected = ProcessingException.class)
    public void testExceptionWhileCreatingInstances()
    {
        mocksControl.replay();

        controller.process(parameters, attributes, ProcessingComponent1.class,
            ProcessingComponentWithoutDefaultConstructor.class);
    }

    // TODO: The tests below fail for me. Is there anything special
    // about them?
    
    @Test(expected = InitializationException.class)
    public void testExceptionWhileInit()
    {
        processingComponent1Mock.init();
        processingComponent2Mock.init();
        mocksControl.andThrow(new InitializationException(null));
        processingComponent1Mock.dispose();
        processingComponent2Mock.dispose();
        processingComponent3Mock.dispose();
        mocksControl.replay();

        controller.process(parameters, attributes, ProcessingComponent1.class,
            ProcessingComponent2.class, ProcessingComponent3.class);
    }

    @Test(expected = ProcessingException.class)
    public void testExceptionBeforeProcessing()
    {
        processingComponent1Mock.init();
        processingComponent2Mock.init();
        processingComponent3Mock.init();
        processingComponent1Mock.beforeProcessing();
        processingComponent2Mock.beforeProcessing();
        mocksControl.andThrow(new ProcessingException("no message"));
        processingComponent1Mock.afterProcessing();
        processingComponent2Mock.afterProcessing();
        processingComponent3Mock.afterProcessing();
        processingComponent1Mock.dispose();
        processingComponent2Mock.dispose();
        processingComponent3Mock.dispose();
        mocksControl.replay();

        controller.process(parameters, attributes, ProcessingComponent1.class,
            ProcessingComponent2.class, ProcessingComponent3.class);
    }

    @Test(expected = ProcessingException.class)
    public void testExceptionDuringProcessing()
    {
        processingComponent1Mock.init();
        processingComponent2Mock.init();
        processingComponent3Mock.init();
        processingComponent1Mock.beforeProcessing();
        processingComponent2Mock.beforeProcessing();
        processingComponent3Mock.beforeProcessing();
        processingComponent1Mock.performProcessing();
        processingComponent2Mock.performProcessing();
        mocksControl.andThrow(new ProcessingException("no message"));
        processingComponent1Mock.afterProcessing();
        processingComponent2Mock.afterProcessing();
        processingComponent3Mock.afterProcessing();
        processingComponent1Mock.dispose();
        processingComponent2Mock.dispose();
        processingComponent3Mock.dispose();
        mocksControl.replay();

        controller.process(parameters, attributes, ProcessingComponent1.class,
            ProcessingComponent2.class, ProcessingComponent3.class);
    }
}
