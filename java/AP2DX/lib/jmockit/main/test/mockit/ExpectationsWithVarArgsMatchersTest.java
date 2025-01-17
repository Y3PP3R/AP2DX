/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class ExpectationsWithVarArgsMatchersTest
{
   static class Collaborator
   {
      List<?> complexOperation(Object input1, Object... otherInputs)
      {
         return input1 == null ? Collections.emptyList() : Arrays.asList(otherInputs);
      }

      @SuppressWarnings({"UnusedDeclaration"})
      int anotherOperation(int i, boolean b, String s, String... otherStrings) { return -1; }
   }

   public interface Dependency
   {
      void doSomething(String... args);
   }

   @Mocked Collaborator mock;
   @Mocked Dependency mock2;

   @Test(expected = AssertionError.class)
   public void replayVarargsMethodWithDifferentThanExpectedNonVarargsArgument()
   {
      new Expectations()
      {
         {
            mock.complexOperation(1, 2, 3);
         }
      };

      mock.complexOperation(2, 2, 3);
   }

   @Test(expected = AssertionError.class)
   public void replayVarargsMethodWithDifferentThanExpectedNumberOfVarargsArguments()
   {
      new Expectations()
      {
         {
            mock.complexOperation(1, 2, 3);
         }
      };

      mock.complexOperation(1, 2);
   }

   @Test(expected = AssertionError.class)
   public void replayVarargsMethodWithDifferentThanExpectedNumberOfVarargsArguments_nonStrict()
   {
      new NonStrictExpectations()
      {
         {
            mock2.doSomething("1", "2", "3"); times = 1;
         }
      };

      mock2.doSomething("1", "2");
   }

   @Test(expected = AssertionError.class)
   public void replayVarargsMethodWithDifferentThanExpectedVarargsArgument()
   {
      new Expectations()
      {
         {
            mock.complexOperation(1, 2, 3);
         }
      };

      mock.complexOperation(1, 2, 4);
   }

   @Test(expected = AssertionError.class)
   public void replayVarargsMethodWithDifferentThanExpectedVarargsArgument_nonStrict()
   {
      new NonStrictExpectations()
      {
         {
            mock2.doSomething("1", "2", "3"); minTimes = 1;
         }
      };

      mock2.doSomething("1", "2", "4");
   }

   @Test
   public void expectInvocationOnMethodWithVarargsArgumentUsingArgumentMatchers()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withEqual(1), withNotEqual(2), withNull());
            mock2.doSomething(withPrefix("C"), withSuffix("."));
         }
      };

      mock.complexOperation(1, 3, null);
      mock2.doSomething("Cab", "123.");
   }

   @Test
   public void expectInvocationWithAnyNumberOfVariableArguments()
   {
      new Expectations()
      {
         {
            mock.complexOperation(any, (Object[]) null); times = 3;
            mock2.doSomething((String[]) any); minTimes = 2;
            mock.complexOperation(123, (Object[]) any);
         }
      };

      mock.complexOperation("test");
      mock.complexOperation(null, 'X');
      mock.complexOperation(1, 3, null);
      mock2.doSomething();
      mock2.doSomething("test", "abc");
      mock.complexOperation(123, true, "test", 3);
   }

   @Test
   public void expectInvocationOnVarargsMethodWithMatcherOnlyForRegularFirstParameter()
   {
      new Expectations()
      {
         {
            mock.complexOperation(any, 1, 2);
         }
      };

      mock.complexOperation("test", 1, 2);
   }

   @Test
   public void expectInvocationWithMatchersForRegularParametersAndAllVarargsValues()
   {
      new Expectations()
      {
         {
            mock.complexOperation(anyBoolean, anyInt, withEqual(2));
            mock.complexOperation(anyString, withEqual(1), any, withEqual(3), anyBoolean);
         }
      };

      mock.complexOperation(true, 1, 2);
      mock.complexOperation("abc", 1, 2, 3, true);
   }

   @Test
   public void recordExpectationsWithMatchersForSomeRegularParametersAndNoneForVarargs()
   {
      new NonStrictExpectations()
      {
         {
            mock.anotherOperation(1, anyBoolean, "test", "a"); result = 1;
            mock.anotherOperation(anyInt, true, withSubstring("X"), "a", "b"); result = 2;
         }
      };

      // Invocations that match a recorded expectation:
      assertEquals(1, mock.anotherOperation(1, true, "test", "a"));
      assertEquals(1, mock.anotherOperation(1, true, "test", "a"));
      assertEquals(1, mock.anotherOperation(1, false, "test", "a"));

      assertEquals(2, mock.anotherOperation(2, true, "aXb", "a", "b"));
      assertEquals(2, mock.anotherOperation(-1, true, "  X", "a", "b"));
      assertEquals(2, mock.anotherOperation(0, true, "XXX", "a", "b"));
      assertEquals(2, mock.anotherOperation(1, true, "X", "a", "b"));

      // Invocations that don't match any expectation:
      assertEquals(0, mock.anotherOperation(1, false, "test", null, "a"));
      assertEquals(0, mock.anotherOperation(1, false, "tst", "a"));
      assertEquals(0, mock.anotherOperation(0, false, "test", "a"));
      assertEquals(0, mock.anotherOperation(1, true, "test", "b"));
      assertEquals(0, mock.anotherOperation(1, true, "test"));

      assertEquals(0, mock.anotherOperation(2, false, "aXb", "a", "b"));
      assertEquals(0, mock.anotherOperation(1, true, "  X", "A", "b"));
      assertEquals(0, mock.anotherOperation(0, true, "XXX", "a"));
      assertEquals(0, mock.anotherOperation(0, true, "XXX", "b"));
      assertEquals(0, mock.anotherOperation(32, true, "-Xx", "a", null));
   }

   @Test
   public void expectInvocationsWithNonNullRegularArgumentAndAnyVarargs()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withNotNull(), (Object[]) any); times = 3;
         }
      };

      mock.complexOperation(new Object(), 1, "2");
      mock.complexOperation("", true, 'a', 2.5);
      mock.complexOperation(123);
   }

   @Test(expected = AssertionError.class)
   public void expectInvocationWithNonNullRegularArgumentAndAnyVarargsButReplayWithNull()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withNotNull(), (Object[]) any);
         }
      };

      mock.complexOperation(null, 1, "2");
   }

   @Test
   public void expectInvocationWithMatchersForSomeRegularParametersAndAllForVarargs()
   {
      new NonStrictExpectations()
      {
         {
            mock.anotherOperation(anyInt, true, withEqual("abc"), anyString, withEqual("test")); result = 1;
            mock.anotherOperation(0, anyBoolean, withEqual("Abc"), anyString, anyString, anyString); result = 2;
         }
      };

      assertEquals(0, mock.anotherOperation(1, false, "test", null, "a"));

      assertEquals(1, mock.anotherOperation(2, true, "abc", "xyz", "test"));
      assertEquals(1, mock.anotherOperation(-1, true, "abc", null, "test"));
      assertEquals(0, mock.anotherOperation(-1, true, "abc", null, "test", null));

      assertEquals(2, mock.anotherOperation(0, false, "Abc", "", "Abc", "test"));
      assertEquals(0, mock.anotherOperation(0, false, "Abc", "", "Abc", "test", ""));
   }

   @SuppressWarnings({"NullArgumentToVariableArgMethod"})
   @Test
   public void expectInvocationWithNoVarArgs()
   {
      @SuppressWarnings({"UnusedDeclaration"})
      class VarArgs
      {
         public void varsOnly(int... ints) {}
         public void mixed(String arg0, int... ints) {}
      }

      VarArgs varArgs = new VarArgs();

      new Expectations()
      {
         VarArgs bar;
         {
            bar.varsOnly();
            bar.varsOnly(null);
            bar.mixed("abcd");
            bar.mixed("abcd", null);
         }
      };

      varArgs.varsOnly();
      varArgs.varsOnly(null);
      varArgs.mixed("abcd");
      varArgs.mixed("abcd", null);
   }

   @Test
   public void expectInvocationWithNonPrimitiveVarArgs()
   {
      class VarArgs
      {
         @SuppressWarnings({"UnusedDeclaration"})
         public void mixed(String[] strings, Integer... ints) {}
      }

      VarArgs varArgs = new VarArgs();
      final String[] strings1 = new String[0];
      final String[] strings2 = {"efgh", "ijkl"};

      new Expectations()
      {
         VarArgs bar;
         {
            bar.mixed(null, 4, 5, 6);
            bar.mixed(strings1, 4, 5, 6);
            bar.mixed(strings2, 4, 5, 6);
            bar.mixed(null);
            bar.mixed(strings1);
            bar.mixed(strings2);
         }
      };

      varArgs.mixed(null, 4, 5, 6);
      varArgs.mixed(strings1, 4, 5, 6);
      varArgs.mixed(strings2, 4, 5, 6);
      varArgs.mixed(null);
      varArgs.mixed(strings1);
      varArgs.mixed(strings2);
   }

   @SuppressWarnings({"NullArgumentToVariableArgMethod"})
   @Test
   public void expectInvocationWithPrimitiveVarArgs()
   {
      @SuppressWarnings({"UnusedDeclaration"})
      class VarArgs
      {
         public void varsOnly(int... ints) {}
         public void mixed(String arg0, String[] strings, int... ints) {}
      }

      VarArgs varArgs = new VarArgs();
      final String[] strings1 = new String[0];
      final String[] strings2 = {"efgh", "ijkl"};

      new Expectations()
      {
         VarArgs bar;
         {
            bar.varsOnly(1, 2, 3);
            bar.varsOnly(null);
            bar.mixed("abcd", null, 4, 5, 6);
            bar.mixed("abcd", strings1, 4, 5, 6);
            bar.mixed("abcd", strings2, 4, 5, 6);
            bar.mixed("abcd", null);
            bar.mixed("abcd", strings1);
            bar.mixed("abcd", strings2);
            bar.mixed("abcd", null, null);
            bar.mixed(null, null, null);
         }
      };

      varArgs.varsOnly(1, 2, 3);
      varArgs.varsOnly(null);
      varArgs.mixed("abcd", null, 4, 5, 6);
      varArgs.mixed("abcd", strings1, 4, 5, 6);
      varArgs.mixed("abcd", strings2, 4, 5, 6);
      varArgs.mixed("abcd", null);
      varArgs.mixed("abcd", strings1);
      varArgs.mixed("abcd", strings2);
      varArgs.mixed("abcd", null, null);
      varArgs.mixed(null, null, null);
   }

   @Test
   public void expectInvocationWithPrimitiveVarArgsUsingMatchers()
   {
      class VarArgs
      {
         @SuppressWarnings({"UnusedDeclaration"})
         public void mixed(String[] strings, int... ints) {}
      }

      VarArgs varArgs = new VarArgs();
      final String[] strings1 = new String[0];
      final String[] strings2 = {"efgh", "ijkl"};

      new Expectations()
      {
         VarArgs bar;
         {
            bar.mixed((String[]) withNull(), withEqual(4), withEqual(5), withEqual(6));
            bar.mixed(withEqual(strings1), withEqual(4), withEqual(5), withEqual(6));
            bar.mixed(withEqual(strings2), withEqual(4), withEqual(5), withEqual(6));
            bar.mixed((String[]) withNull());
            bar.mixed(withEqual(strings1));
            bar.mixed(withEqual(strings2));
         }
      };

      varArgs.mixed(null, 4, 5, 6);
      varArgs.mixed(strings1, 4, 5, 6);
      varArgs.mixed(strings2, 4, 5, 6);
      varArgs.mixed(null);
      varArgs.mixed(strings1);
      varArgs.mixed(strings2);
   }
}
