/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;

import org.junit.*;
import static org.junit.Assert.assertTrue;

final class FinalReusableExpectations extends Expectations
{
   PrintWriter mock;

   FinalReusableExpectations()
   {
      mock.flush();
      minTimes = 2;
   }
}

class NonFinalReusableExpectations extends Expectations
{
   PrintWriter mock;

   NonFinalReusableExpectations()
   {
      mock.flush();
      times = 2;
   }
}

public final class ReusableTopLevelInvocationsTest
{
   @Test
   public void useTopLevelAndFinalExpectationsSubclass() throws Exception
   {
      PrintWriter pw = new PrintWriter(System.out);

      new FinalReusableExpectations();

      pw.flush();
      pw.flush();
   }

   @Test
   public void useTopLevelAndNonFinalExpectationsSubclass() throws Exception
   {
      final PrintWriter pw = new PrintWriter(System.out);

      new NonFinalReusableExpectations() {{
         pw.checkError(); result = true;
      }};

      pw.flush();
      pw.flush();
      assertTrue(pw.checkError());
   }

   @Test(expected = AssertionError.class)
   public void useTopLevelAndFinalVerificationsSubclass(PrintWriter mock) throws Exception
   {
      mock.flush();

      new FinalReusableVerifications(mock);
   }

   @Test
   public void useTopLevelAndFinalSubclassOfVerificationsSubclass(PrintWriter mock) throws Exception
   {
      mock.flush();
      mock.flush();

      new FinalSubclassOfReusableVerifications(mock);
   }

   @Test(expected = AssertionError.class)
   public void useTopLevelAndNonFinalVerificationsSubclass(PrintWriter mock) throws Exception
   {
      mock.flush();

      new NonFinalReusableVerifications(mock) {};
   }
}

final class FinalReusableVerifications extends Verifications
{
   FinalReusableVerifications(PrintWriter mock)
   {
      mock.flush();
      times = 2;
   }
}

final class FinalSubclassOfReusableVerifications extends NonFinalReusableVerifications
{
   FinalSubclassOfReusableVerifications(PrintWriter mock) { super(mock); }
}

class NonFinalReusableVerifications extends Verifications
{
   NonFinalReusableVerifications(PrintWriter mock)
   {
      mock.flush();
      minTimes = 2;
      maxTimes = 2;
   }
}