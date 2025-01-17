/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class BooleanExpressionsTest extends CoverageTest
{
   BooleanExpressions tested;

   @Test
   public void evalOnlySomeCombinations()
   {
      assertTrue(tested.eval1(true, true, 0));
      assertFalse(tested.eval1(true, false, 0));

      findMethodData(7, "eval1");
      assertPaths(4, 2, 2);
   }

   @Test
   public void evalBuggyCombination()
   {
      // Only this combination will fail if the third condition in eval1 is changed to "z < 0",
      // which demonstrates that a more sophisticated metric than branch coverage is needed.
      assertTrue(tested.eval1(true, false, 1));

      findMethodData(7, "eval1");
      assertPaths(4, 3, 3);
   }

   @Test
   public void evalAllCombinations()
   {
      assertTrue(tested.eval2(true, true, 0));
      assertTrue(tested.eval2(true, false, 1));
      assertFalse(tested.eval2(true, false, 0));
      assertFalse(tested.eval2(false, true, 0));

      findMethodData(12, "eval2");
      assertPaths(4, 4, 4);
   }

   @Test
   public void evalOnlyFirstBranch()
   {
      assertFalse(tested.eval3(false, true, false));

      findMethodData(17, "eval3");
      assertPaths(4, 1, 1);
   }

   @Test
   public void evalAllPaths()
   {
      assertFalse(tested.eval3(false, true, false));
      assertTrue(tested.eval3(true, true, false));
      assertTrue(tested.eval3(true, false, true));
      assertFalse(tested.eval3(true, false, false));

      findMethodData(17, "eval3");
      assertPaths(4, 4, 5);
   }

   @Test
   public void evalOnlyFirstAndSecondBranches()
   {
      assertFalse(tested.eval4(false, true, false));
      assertFalse(tested.eval4(false, false, false));
      assertFalse(tested.eval4(false, true, true));
      assertFalse(tested.eval4(false, false, true));
      assertTrue(tested.eval4(true, false, false));
      assertTrue(tested.eval4(true, false, true));

      findMethodData(22, "eval4");
      assertPaths(4, 2, 6);
   }

   @Test
   public void eval5()
   {
      assertFalse(tested.eval5(false, true, true));
      assertTrue(tested.eval5(false, false, false));

      findMethodData(27, "eval5");
      assertPaths(5, 2, 2);
   }

   @Test
   public void methodWithComplexExpressionWhichCallsAnotherInSameClass()
   {
      BooleanExpressions.isSameTypeIgnoringAutoBoxing(int.class, Integer.class);

      findMethodData(35, "isSameTypeIgnoringAutoBoxing");
      assertPaths(8, 1, 1);

      findMethodData(43, "isWrapperOfPrimitiveType");
      assertPaths(63, 1, 1);
   }
}
