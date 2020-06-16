// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long meetingDuration = request.getDuration();
    Collection<TimeRange> workable = new ArrayList<>();
    
    List<TimeRange> conflictTimes = new ArrayList<>();
    int previousLargeMeetingPoint = 0;
    for (Event event: events) {
      Set<String> attendeesForThisEvent = event.getAttendees();
      if (intersectingAttendee(attendeesForThisEvent, request.getAttendees())) {
        conflictTimes.add(event.getWhen());
      }
    }
    Collections.sort(conflictTimes, TimeRange.ORDER_BY_START);
    int freeTimeStampStart = TimeRange.START_OF_DAY;
    int freeTimeStampEnd = 0;
    for (TimeRange conflictTime : conflictTimes) {
      freeTimeStampEnd = conflictTime.start();
      if (meetingDuration <= freeTimeStampEnd - freeTimeStampStart) {
        workable.add(TimeRange.fromStartEnd(freeTimeStampStart, freeTimeStampEnd, false));
      }
      if (previousLargeMeetingPoint < freeTimeStampEnd) {
        previousLargeMeetingPoint = freeTimeStampStart;
      }
      freeTimeStampStart = previousLargeMeetingPoint > conflictTime.end() ? previousLargeMeetingPoint : conflictTime.end();
    }
     if (TimeRange.END_OF_DAY - freeTimeStampStart >= meetingDuration) {
      workable.add(TimeRange.fromStartEnd(freeTimeStampStart, TimeRange.END_OF_DAY, true));
    }

    return workable;
  }

  private boolean intersectingAttendee(Set<String> eventAttendees, Collection<String> meetingAttendees) {
    for (String attendee: eventAttendees) {
      if (meetingAttendees.contains(attendee)) return true;
    }
    return false;
  }
}
