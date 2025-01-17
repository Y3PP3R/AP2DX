/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.paths;

import java.util.*;
import java.io.*;

public class Node implements Serializable
{
   private static final long serialVersionUID = 7521062699264845946L;

   private final transient ThreadLocal<Boolean> reached = new ThreadLocal<Boolean>();
   public final int line;
   protected int segment;

   private Node(int line)
   {
      this.line = line;
   }

   void setSegmentAccordingToPrecedingNode(Node precedingNode)
   {
      int currentSegment = precedingNode.segment;

      if (precedingNode instanceof Fork) {
         segment = currentSegment + 1;
      }
      else {
         segment = currentSegment;
      }
   }

   public final int getSegment()
   {
      return segment;
   }

   final void setReached(Boolean reached)
   {
      this.reached.set(reached);
   }

   final boolean wasReached()
   {
      return reached.get() != null;
   }

   @Override
   public final String toString()
   {
      return getClass().getSimpleName() + ':' + line + '-' + segment;
   }

   static final class Entry extends Node
   {
      private static final long serialVersionUID = -3065417917872259568L;
      Fork nextNode;

      Entry(int entryLine) { super(entryLine); }
   }

   interface ConditionalSuccessor extends Serializable
   {
      void addToPath(Path path);
   }

   interface GotoSuccessor extends Serializable
   {
      void setNextNodeAfterGoto(Join newJoin);
   }

   static final class Exit extends Node implements ConditionalSuccessor
   {
      private static final long serialVersionUID = -4801498566218642509L;
      final List<Path> paths = new ArrayList<Path>(4);

      Exit(int exitLine) { super(exitLine); }

      public void addToPath(Path path)
      {
         path.addNode(this);
         paths.add(path);
      }
   }

   static final class BasicBlock extends Node implements ConditionalSuccessor, GotoSuccessor
   {
      private static final long serialVersionUID = 2637678937923952603L;
      ConditionalSuccessor nextConsecutiveNode;
      Join nextNodeAfterGoto;

      BasicBlock(int startingLine) { super(startingLine); }

      public void setNextNodeAfterGoto(Join newJoin) { nextNodeAfterGoto = newJoin; }

      public void addToPath(Path path)
      {
         path.addNode(this);

         if (nextNodeAfterGoto != null) {
            assert nextConsecutiveNode == null;
            nextNodeAfterGoto.addToPath(path);
         }
         else {
            nextConsecutiveNode.addToPath(path);
         }
      }
   }

   public abstract static class Fork extends Node implements ConditionalSuccessor
   {
      Fork(int line) { super(line); }

      abstract void addNextNode(Join nextNode);

      final void createAlternatePath(Path parentPath, Join targetJoin)
      {
         Path alternatePath = new Path(parentPath);
         targetJoin.addToPath(alternatePath);
      }
   }

   static final class SimpleFork extends Fork
   {
      private static final long serialVersionUID = -521666665272332763L;
      ConditionalSuccessor nextConsecutiveNode;
      Join nextNodeAfterJump;

      SimpleFork(int line) { super(line); }

      @Override
      void addNextNode(Join nextNode) { nextNodeAfterJump = nextNode; }

      public void addToPath(Path path)
      {
         path.addNode(this);
         createAlternatePath(path, nextNodeAfterJump);
         nextConsecutiveNode.addToPath(path);
      }
   }

   static final class MultiFork extends Fork
   {
      private static final long serialVersionUID = 1220318686622690670L;
      final List<Join> caseNodes = new ArrayList<Join>();

      MultiFork(int line) { super(line); }

      @Override
      void addNextNode(Join nextNode) { caseNodes.add(nextNode); }

      public void addToPath(Path path)
      {
         path.addNode(this);

         for (Join caseJoin : caseNodes) {
            createAlternatePath(path, caseJoin);
         }
      }
   }

   static final class Join extends Node implements ConditionalSuccessor, GotoSuccessor
   {
      private static final long serialVersionUID = -1983522899831071765L;
      ConditionalSuccessor nextNode;

      Join(int joiningLine) { super(joiningLine); }

      public void setNextNodeAfterGoto(Join newJoin) { nextNode = newJoin; }

      public void addToPath(Path path)
      {
         path.addNode(this);
         nextNode.addToPath(path);
      }

      @Override
      void setSegmentAccordingToPrecedingNode(Node precedingNode)
      {
         segment = precedingNode.segment + 1;
      }
   }
}
