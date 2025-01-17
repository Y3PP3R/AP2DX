package unitils.tutorial;

import java.util.*;

import org.unitils.*;
import org.unitils.mock.*;
import org.unitils.mock.core.proxy.*;
import org.unitils.mock.mockbehavior.*;

import org.junit.*;

import static org.junit.Assert.*;
import static org.unitils.mock.ArgumentMatchers.*;

/**
 * Based on the <a href="http://unitils.org/tutorial.html">Unitils Tutorial</a>.
 */
public final class AlertServiceTest extends UnitilsJUnit4
{
   private AlertService alertService;
   private Message alert1;
   private Message alert2;
   private List<Message> alerts;

   Mock<SchedulerService> mockSchedulerService;
   Mock<MessageService> mockMessageService;

   @Before
   public void init()
   {
      alertService = new AlertService(mockSchedulerService.getMock(), mockMessageService.getMock());
      alert1 = new Alert();
      alert2 = new Alert();
      alerts = Arrays.asList(alert1, alert2);
   }

   @Test
   public void sendScheduledAlerts()
   {
      mockSchedulerService.returns(alerts).getScheduledAlerts(null, 1, anyBoolean());

      alertService.sendScheduledAlerts();

      mockMessageService.assertInvoked().sendMessage(alert2);
      mockMessageService.assertInvoked().sendMessage(alert1);
   }

   @Test
   public void sendScheduledAlertsInProperSequence()
   {
      mockSchedulerService.returns(alerts).getScheduledAlerts(null, 1, anyBoolean());

      alertService.sendScheduledAlerts();

      // Test will also pass if these asserts are in reverse order: Unitils bug?
      mockMessageService.assertInvokedInSequence().sendMessage(alert1);
      mockMessageService.assertInvokedInSequence().sendMessage(alert2);
   }

   @Test
   public void sendNothingWhenNoAlertsAvailable()
   {
      alertService.sendScheduledAlerts();

      mockMessageService.assertNotInvoked().sendMessage(null);
   }

   @Test
   public void sendNothingWhenNoAlertsAvailable_usingAssertNoMoreInvocations()
   {
      alertService.sendScheduledAlerts();

      MockUnitils.assertNoMoreInvocations();
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToGetScheduledAlertsWithInvalidArguments()
   {
      mockSchedulerService.raises(IllegalArgumentException.class).getScheduledAlerts(
         "123", 1, true);

      alertService.sendScheduledAlerts();
   }

   @Test(expected = Exception.class)
   public void recordConsecutiveInvocationsToSameMethodWithSameArguments()
   {
      mockSchedulerService.onceReturns(alerts).getScheduledAlerts(null, 0, true);
      mockSchedulerService.onceRaises(new Exception()).getScheduledAlerts(null, 0, true);

      assertEquals(alerts, mockSchedulerService.getMock().getScheduledAlerts(null, 0, true));
      mockSchedulerService.getMock().getScheduledAlerts(null, 0, true);
   }

   @Test
   public void specifyingCustomMockBehavior()
   {
      mockSchedulerService.performs(new MockBehavior()
      {
         public Object execute(ProxyInvocation mockInvocation)
         {
            List<Object> args = mockInvocation.getArguments();
            assertEquals(3, args.size());
            assertEquals("123", args.get(0));
            assertEquals(1, args.get(1));
            assertEquals(true, args.get(2));

            return Arrays.asList(alert2);
         }
      }).getScheduledAlerts("123", 1, true);

      alertService.sendScheduledAlerts();

      mockMessageService.assertInvoked().sendMessage(alert2);
      mockMessageService.assertNotInvoked().sendMessage(alert1);
   }
}
