/**
 * Copyright (c) 2006, Sun Microsystems, Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   * Neither the name of the TimingFramework project nor the names of its
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.animation.timing.triggers;

/**
 * Timing events; TimingTriggers can be set to fire when an animator 
 * starts, stops, or repeats.
 *
 * @author Chet
 */
public final class TimingTriggerEvent extends TriggerEvent {
    /** Event fired when Animator starts. */
    public static final TimingTriggerEvent START = new TimingTriggerEvent();

    /** Event fired when Animator stops. */
    public static final TimingTriggerEvent STOP = new TimingTriggerEvent();

    /**
     * Event fired when Animator finishes one cycle and starts another.
     */
    public static final TimingTriggerEvent REPEAT = new TimingTriggerEvent();

    private TimingTriggerEvent() {}

    /**
     * This method finds the opposite of the current event.: START -> STOP
     * and STOP -> START.  Note that REPEAT has no obvious opposite so
     * it simply returns REPEAT (this method should probably not be called
     * for that case).
     */
    @Override
    public TriggerEvent getOppositeEvent() {
        if (this == START) {
            return STOP;
        } else if (this == STOP) {
            return START;
        }
        // Possible to reach here for REPEAT action (but probably should not
        // have been called with this event)
        return this;
    }   
}
