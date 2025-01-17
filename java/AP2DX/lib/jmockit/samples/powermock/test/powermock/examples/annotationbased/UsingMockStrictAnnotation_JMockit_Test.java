/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.annotationbased;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;
import powermock.examples.annotationbased.dao.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/DocumentationExamples/src/test/java/powermock/examples/annotationbased/UsingMockStrictAnnotationTest.java">PowerMock version</a>
 */
public final class UsingMockStrictAnnotation_JMockit_Test
{
   @Mocked
   private SomeDao someDaoMock;

   private SomeService someService;

   @Before
   public void setUp()
   {
      someService = new SomeService(someDaoMock);
   }

   @Test
   public void assertThatStrictMockAnnotationWork()
   {
      final Object dataObject = new Object();
      final Object otherDataObject = new Object();

      new Expectations()
      {
         {
            someDaoMock.getSomeData(); result = dataObject;
            someDaoMock.getSomeOtherData(); result = otherDataObject;
         }
      };

      assertSame(dataObject, someService.getData());
      assertSame(otherDataObject, someService.getMoreData());
   }

   @Test(expected = AssertionError.class)
   public void assertThatStrictMockAnnotationWorkWhenStrictMatchingIsNotApplied()
   {
      final Object dataObject = new Object();
      final Object otherDataObject = new Object();

      new Expectations()
      {
         {
            someDaoMock.getSomeOtherData(); result = otherDataObject;
            someDaoMock.getSomeData(); result = dataObject;
         }
      };

      assertSame(dataObject, someService.getData());
   }
}
