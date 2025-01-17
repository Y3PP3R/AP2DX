/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest;

/**
 * A description of a Matcher. A Matcher will describe itself to a description
 * which can later be used for reporting.
 *
 * @see org.hamcrest.Matcher#describeTo(Description)
 */
public interface Description
{
   /**
    * A description that consumes input but does nothing.
    */
   Description NONE = new NullDescription();

   /**
    * Appends some plain text to the description.
    */
   Description appendText(CharSequence text);

   /**
    * Appends the description of a {@link org.hamcrest.SelfDescribing} value to this description.
    */
   Description appendDescriptionOf(SelfDescribing value);

   /**
    * Appends an arbitary value to the description.
    */
   Description appendValue(Object value);

   /**
    * Appends a list of values to the description.
    */
   <T> Description appendValueList(String start, String separator, String end, T... values);

   /**
    * Appends a list of values to the description.
    */
   <T> Description appendValueList(String start, String separator, String end, Iterable<T> values);

   final class NullDescription implements Description
   {
      public Description appendDescriptionOf(SelfDescribing value)
      {
         return this;
      }

      public Description appendText(CharSequence text)
      {
         return this;
      }

      public Description appendValue(Object value)
      {
         return this;
      }

      public <T> Description appendValueList(
         String start, String separator, String end, T... values)
      {
         return this;
      }

      public <T> Description appendValueList(
         String start, String separator, String end, Iterable<T> values)
      {
         return this;
      }

      @Override
      public String toString()
      {
         return "";
      }
   }
}
