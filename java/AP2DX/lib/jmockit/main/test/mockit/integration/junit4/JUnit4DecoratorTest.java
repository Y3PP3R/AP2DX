/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4;

import org.junit.*;
import static org.junit.Assert.*;

import mockit.*;

public final class JUnit4DecoratorTest extends BaseJUnit4DecoratorTest
{
   public static final class RealClass2
   {
      public String getValue() { return "REAL2"; }
   }

   @MockClass(realClass = RealClass2.class)
   public static final class MockClass2
   {
      @Mock
      public String getValue() { return "TEST2"; }
   }

   @Test
   public void useClassScopedMockDefinedByBaseClass()
   {
      assertEquals("TEST0", new RealClass0().getValue());
   }

   @Test
   public void setUpAndUseSomeMocks()
   {
      assertEquals("TEST1", new RealClass1().getValue());
      assertEquals("REAL2", new RealClass2().getValue());

      Mockit.setUpMocks(MockClass2.class);

      assertEquals("TEST2", new RealClass2().getValue());
      assertEquals("TEST1", new RealClass1().getValue());
   }

   @Test
   public void setUpAndUseMocksAgain()
   {
      assertEquals("TEST1", new RealClass1().getValue());
      assertEquals("REAL2", new RealClass2().getValue());

      Mockit.setUpMocks(MockClass2.class);

      assertEquals("TEST2", new RealClass2().getValue());
      assertEquals("TEST1", new RealClass1().getValue());
   }

   @After
   public void afterTest()
   {
      assertEquals("REAL2", new RealClass2().getValue());
   }

   @Test
   public void classMockedInSecondTestClassMustNotBeMockedForThisTestClass()
   {
      assertEquals("REAL3", new SecondJUnit4DecoratorTest.RealClass3().getValue());
   }
}
