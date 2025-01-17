/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package unitils.tutorial;

import java.util.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.assertEquals;

/**
 * These tests are equivalent to the ones in {@link AlertServiceTest}.
 * <p/>
 * The Unitils Mock API has a {@code @TestedObject} annotation similar to JMockit's {@code @Tested}, but it does not
 * support constructor injection.
 */
public final class AlertService_JMockit_Test
{
   @Tested private AlertService alertService;
   private Message alert1;
   private Message alert2;
   private List<Message> alerts;

   @Injectable SchedulerService mockSchedulerService;
   @Injectable MessageService mockMessageService;

   @Before
   public void init()
   {
      alert1 = new Alert();
      alert2 = new Alert();
      alerts = Arrays.asList(alert1, alert2);
   }

   @Test
   public void sendScheduledAlerts()
   {
      new NonStrictExpectations()
      {{
         mockSchedulerService.getScheduledAlerts(null, 1, anyBoolean); result = alerts;
      }};

      alertService.sendScheduledAlerts();

      new Verifications()
      {{
         mockMessageService.sendMessage(alert2);
         mockMessageService.sendMessage(alert1);
      }};
   }

   @Test
   public void sendScheduledAlertsInProperSequence()
   {
      new NonStrictExpectations()
      {{
         mockSchedulerService.getScheduledAlerts(null, 1, anyBoolean); result = alerts;
      }};

      alertService.sendScheduledAlerts();

      new VerificationsInOrder()
      {{
         mockMessageService.sendMessage(alert1);
         mockMessageService.sendMessage(alert2);
      }};
   }

   @Test
   public void sendNothingWhenNoAlertsAvailable()
   {
      alertService.sendScheduledAlerts();

      new Verifications()
      {
         {
            mockMessageService.sendMessage((Message) any); times = 0;
         }
      };
   }

   @Test
   public void sendNothingWhenNoAlertsAvailable_usingFullVerifications()
   {
      alertService.sendScheduledAlerts();

      new FullVerifications()
      {
         {
            mockSchedulerService.getScheduledAlerts(null, anyInt, anyBoolean);
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToGetScheduledAlertsWithInvalidArguments()
   {
      new Expectations()
      {
         {
            mockSchedulerService.getScheduledAlerts("123", 1, true);
            result = new IllegalArgumentException();
         }
      };

      alertService.sendScheduledAlerts();
   }

   @Test(expected = Exception.class)
   public void recordConsecutiveInvocationsToSameMethodWithSameArguments()
   {
      new Expectations()
      {
         {
            mockSchedulerService.getScheduledAlerts(null, 0, true); result = alerts;
            mockSchedulerService.getScheduledAlerts(null, 0, true); result = new Exception();
         }
      };

      assertEquals(alerts, mockSchedulerService.getScheduledAlerts(null, 0, true));
      mockSchedulerService.getScheduledAlerts(null, 0, true);
   }

   @Test
   public void specifyingCustomMockBehavior()
   {
      new NonStrictExpectations()
      {
         {
            mockSchedulerService.getScheduledAlerts("123", 1, true);
            result = new Delegate()
            {
               List<Message> getScheduledAlerts(Object arg0, int arg1, boolean arg2)
               {
                  assert arg0 == "123";
                  assert arg1 == 1;
                  assert arg2;

                  return Arrays.asList(alert2);
               }
            };
         }
      };

      alertService.sendScheduledAlerts();

      new Verifications()
      {
         {
            mockMessageService.sendMessage(alert1); times = 0;
            mockMessageService.sendMessage(alert2);
         }
      };
   }
}
