/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

@SuppressWarnings({"UnusedDeclaration"})
public final class VerificationsWithPartialOrderingTest
{
   public static class Dependency
   {
      public void setSomething(int value) {}
      public void setSomethingElse(String value) {}
      public void editABunchMoreStuff() {}
      public void notifyBeforeSave() {}
      public void prepare() {}
      public void save() {}
   }

   @Mocked private Dependency mock;

   private void exerciseCodeUnderTest()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.editABunchMoreStuff();
      mock.notifyBeforeSave();
      mock.save();
   }

   @Test
   public void verifyFirstCallOnly()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
      }};
   }

   @Test
   public void verifyFirstCallWhichWasRecordedWithAConstraint()
   {
      new NonStrictExpectations() {{
         mock.prepare(); times = 1;
      }};

      exerciseCodeUnderTest();

      new VerificationsInOrder() {{
         mock.prepare();
         mock.setSomething(anyInt);
         unverifiedInvocations();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstCallWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.prepare();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
      }};
   }

   @Test
   public void verifyLastCallOnly()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test
   public void verifyLastTwoCalls()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyLastCallWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.save();
      mock.editABunchMoreStuff();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyLastTwoCallsWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.save();
      mock.notifyBeforeSave();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastCallsWhenOutOfOrder()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.notifyBeforeSave();
      mock.editABunchMoreStuff();
      mock.save();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test
   public void verifyFirstCallThenOthersInAnyOrder()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
      }};

      new Verifications()
      {{
         mock.setSomethingElse("anotherValue");
         mock.setSomething(123);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifySomeCallsInAnyOrderThenFirstCallWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.editABunchMoreStuff();
      mock.prepare();

      new Verifications()
      {{
         mock.setSomething(123);
      }};

      new VerificationsInOrder()
      {{
         mock.prepare(); times = 1;
         unverifiedInvocations();
      }};
   }

   @Test
   public void verifySomeCallsInAnyOrderThenLastCall()
   {
      exerciseCodeUnderTest();

      new Verifications()
      {{
         mock.setSomethingElse("anotherValue");
         mock.setSomething(123);
      }};

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.save(); times = 1;
      }};
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToUseOrderedVerificationsAsAlreadyVerified()
   {
      mock.prepare();
      mock.editABunchMoreStuff();
      mock.save();

      final Verifications verified = new VerificationsInOrder()
      {{
         mock.prepare();
         mock.editABunchMoreStuff();
      }};

      new VerificationsInOrder() {{
         verifiedInvocations(verified);
      }};
   }

   @Test
   public void verifyThatSomeCallsInAnyRelativeOrderOccurBeforeAllOthers()
   {
      // These can occur in any order, but before any others:
      mock.prepare();
      mock.setSomethingElse("anotherValue");
      mock.setSomethingElse(null);

      // Not verified, but must occur after all others:
      mock.setSomething(123);
      mock.save();

      final Verifications initialGroupOfInvocations = new Verifications() {{
         mock.prepare();
         mock.setSomethingElse(anyString);
      }};

      new VerificationsInOrder() {{
         verifiedInvocations(initialGroupOfInvocations);
         unverifiedInvocations();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyThatSomeCallsInAnyRelativeOrderOccurBeforeAllOthers_outOfOrder()
   {
      // Not verified, but should occur *after* the verified invocations:
      mock.save();

      // Verified:
      mock.prepare();
      mock.editABunchMoreStuff();

      // Not verified:
      mock.setSomething(123);

      final Verifications initialGroupOfInvocations = new Verifications() {{
         mock.prepare();
         mock.editABunchMoreStuff();
      }};

      new VerificationsInOrder() {{
         verifiedInvocations(initialGroupOfInvocations);
         unverifiedInvocations();
      }};
   }

   @Test
   public void verifyThatSomeCallsInAnyRelativeOrderOccurAfterAllOthers()
   {
      // Not verified:
      mock.prepare();
      mock.setSomething(123);

      // Verified in any order:
      mock.setSomethingElse("anotherValue");
      mock.save();
      mock.setSomethingElse(null);

      final Verifications finalGroupOfInvocations = new Verifications() {{
         mock.setSomethingElse(anyString);
         mock.save();
      }};

      new VerificationsInOrder() {{
         unverifiedInvocations();
         verifiedInvocations(finalGroupOfInvocations);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyThatSomeCallsInAnyRelativeOrderOccurAfterAllOthers_outOfOrder()
   {
      // Not verified:
      mock.prepare();

      // Verified in any order:
      mock.setSomethingElse("anotherValue");
      mock.setSomethingElse(null);
      mock.save();

      // Not verified, but should occur *before* the verified ones:
      mock.setSomething(123);

      final Verifications finalGroupOfInvocations = new Verifications() {{
         mock.setSomethingElse(anyString);
         mock.save();
      }};

      new VerificationsInOrder() {{
         unverifiedInvocations();
         verifiedInvocations(finalGroupOfInvocations);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyThatUnorderedGroupOfCallsOccursAfterOneOtherCall_outOfOrder()
   {
      mock.setSomething(123);
      mock.editABunchMoreStuff();
      mock.prepare();

      final Verifications v = new Verifications() {{
         mock.editABunchMoreStuff();
         mock.setSomething(anyInt);
      }};

      new VerificationsInOrder() {{
         mock.prepare();
         verifiedInvocations(v);
      }};
   }

   @Test
   public void verifyThatAnOrderedGroupOfCallsOccursBetweenTwoOtherGroupsOfCalls(final Runnable aCallback)
   {
      // First group, happening before everything else in any order:
      mock.prepare();
      aCallback.run();

      // Verified intermediate invocations, occurring in this order:
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.setSomethingElse(null);
      mock.editABunchMoreStuff();

      // Second group, happening after everything else in any order:
      mock.notifyBeforeSave();
      mock.save();

      final Verifications before = new Verifications() {{
         aCallback.run();
         mock.prepare();
      }};

      final Verifications after = new Verifications() {{
         mock.save();
         mock.notifyBeforeSave();
      }};

      new VerificationsInOrder() {{
         verifiedInvocations(before);
         mock.setSomething(anyInt);
         mock.setSomethingElse(anyString); times = 2;
         mock.editABunchMoreStuff();
         verifiedInvocations(after);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyThatAnOrderedGroupOfCallsOccursBetweenTwoOtherGroupsOfCalls_outOfOrder(final Runnable aCallback)
   {
      // First verified group, happening before everything else in any order:
      mock.prepare();
      aCallback.run();

      // Verified intermediate invocations, occurring in this order:
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.setSomethingElse(null);
      mock.editABunchMoreStuff();

      // Unexpected (and unverified):
      mock.setSomethingElse("");

      // Second verified group, happening after everything else in any order:
      mock.notifyBeforeSave();
      mock.save();

      final Verifications before = new Verifications() {{
         aCallback.run();
         mock.prepare();
      }};

      final Verifications after = new Verifications() {{
         mock.save();
         mock.notifyBeforeSave();
      }};

      new VerificationsInOrder() {{
         verifiedInvocations(before);
         mock.setSomething(anyInt);
         mock.setSomethingElse(anyString); times = 2;
         mock.editABunchMoreStuff();
         verifiedInvocations(after);
      }};
   }

   @Test
   public void verifyThatAnUnverifiedGroupOfCallsOccursBetweenTwoOtherGroupsOfCalls(final Runnable aCallback)
   {
      // Initial invocation:
      mock.prepare();

      // First group, happening before everything else in any order:
      mock.setSomething(123);
      mock.setSomething(-56);

      // Intermediate invocations, occurring in any order:
      mock.setSomethingElse("anotherValue");
      mock.editABunchMoreStuff();
      mock.setSomethingElse(null);

      // Second group, happening after everything else in any order:
      aCallback.run();
      mock.save();

      final Verifications before = new Verifications() {{
         mock.setSomething(anyInt);
      }};

      final Verifications after = new Verifications() {{
         mock.save();
         aCallback.run();
      }};

      new VerificationsInOrder() {{
         mock.prepare();
         verifiedInvocations(before);
         unverifiedInvocations();
         verifiedInvocations(after);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyThatAnUnverifiedGroupOfCallsOccursBetweenTwoOtherGroupsOfCalls_outOfOrder(final Runnable aCallback)
   {
      // First group, happening before everything else in any order:
      mock.setSomething(123);
      mock.setSomething(-56);

      // Unverified intermediate invocations, occurring in any order:
      mock.setSomethingElse("anotherValue");

      // Second group, happening after everything else in any order:
      aCallback.run();
      mock.editABunchMoreStuff(); // out of place
      mock.save();

      final Verifications before = new Verifications() {{
         mock.setSomething(anyInt);
         times = 2;
      }};

      final Verifications after = new Verifications() {{
         aCallback.run();
         mock.save();
      }};

      new VerificationsInOrder() {{
         verifiedInvocations(before);
         unverifiedInvocations();
         verifiedInvocations(after);
      }};
   }

   @Test
   public void verifyFirstAndLastCalls()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastCallsWithFirstOutOfOrder()
   {
      mock.editABunchMoreStuff();
      mock.prepare();
      mock.save();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastInvocationsWithSomeInvocationsInBetweenImplicitlyVerified()
   {
      new NonStrictExpectations()
      {
         {
            mock.setSomething(anyInt); minTimes = 1;
         }
      };

      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         // unverifiedInvocations() should be called here, even if verification occurs implicitly.
         mock.setSomethingElse(anyString);
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastCallsWithLastOutOfOrder()
   {
      mock.prepare();
      mock.editABunchMoreStuff();
      mock.save();
      mock.notifyBeforeSave();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test
   public void verifyFirstAndLastCallsWithOthersInBetweenInAnyOrderWithYetAnotherCallAnywhere()
   {
      mock.prepare(); // first call
      mock.editABunchMoreStuff();
      mock.notifyBeforeSave();
      mock.save(); // last call
      mock.setSomething(3); // could occur anywhere

      new Verifications() {{ mock.setSomething(anyInt); }};

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test
   public void verifyTwoOrderedCallsAfterVerifyingDifferentCallsToSameMethodInAnyOrder()
   {
      mock.setSomething(1);
      mock.prepare();
      mock.setSomething(2);
      mock.save();
      mock.setSomething(3);

      new Verifications() {{ mock.setSomething(anyInt); times = 3; }};

      new VerificationsInOrder()
      {{
         mock.prepare();
         mock.save();
      }};
   }

   @Test
   public void verifyFirstAndLastCallsWithOthersInBetweenInAnyOrder()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};

      new Verifications()
      {{
         mock.setSomething(123);
         mock.setSomethingElse("anotherValue");
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastCallsWithOthersInBetweenInAnyOrderWhenOutOfOrder()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.notifyBeforeSave();
      mock.editABunchMoreStuff();
      mock.save();

      new Verifications()
      {{
         mock.setSomethingElse("anotherValue");
         mock.setSomething(anyInt);
      }};

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test
   public void verifyConsecutiveInvocations()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.setSomething(123);
         mock.setSomethingElse("anotherValue");
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyConsecutiveInvocationsWhenNotConsecutive()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.setSomething(45);
      mock.save();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.setSomething(123);
         mock.setSomething(45);
         unverifiedInvocations();
      }};
   }

   @Test
   public void verifyConsecutiveInvocationsInTwoSequences()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.setSomething(123);
         mock.setSomethingElse(anyString);
         unverifiedInvocations();
         mock.notifyBeforeSave();
         unverifiedInvocations();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyConsecutiveInvocationsInTwoSequencesWhenNotConsecutive()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomething(45);
      mock.setSomethingElse("anotherValue");
      mock.notifyBeforeSave();
      mock.save();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.setSomething(123);
         mock.setSomething(45);
         unverifiedInvocations();
         mock.save();
         unverifiedInvocations();
      }};
   }
}
