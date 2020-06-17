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
import java.util.HashSet;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long meetingDuration = request.getDuration();

    Collection<TimeRange> workableTimesForMandatoryAttendees = findOptimalTime(events, request, true);
    if (request.getOptionalAttendees().isEmpty()) return workableTimesForMandatoryAttendees;

    Collection<TimeRange> workableTimesForOptionalAttendees = findOptimalTime(events, request, false);
    if (request.getAttendees().isEmpty()) return workableTimesForOptionalAttendees;

    if (workableTimesForOptionalAttendees.size() == 0) return workableTimesForMandatoryAttendees;
    
    Collection<TimeRange> workableForBoth = new ArrayList<>();

    for (TimeRange workable4Optional : workableTimesForOptionalAttendees) {
      for (TimeRange workable4Mandatory : workableTimesForMandatoryAttendees) {
        if (workable4Optional.contains(workable4Mandatory) && !workableForBoth.contains(workable4Mandatory)) workableForBoth.add(workable4Mandatory);
      }
    }

    if (workableForBoth.isEmpty()) return workableTimesForMandatoryAttendees;

    return workableForBoth;
  }

  private Collection<TimeRange> findOptimalTime(Collection<Event> events, MeetingRequest request, Boolean mandatory) {
    long meetingDuration = request.getDuration();
    Collection<TimeRange> workable = new ArrayList<>();        
    List<TimeRange> conflictTimes = new ArrayList<>();
    for (Event event: events) {
      Set<String> attendeesForThisEvent = event.getAttendees();
      if (mandatory) {
        if (intersectingAttendee(attendeesForThisEvent, request.getAttendees())) {
          conflictTimes.add(event.getWhen());
        }
      } else {
        if (intersectingAttendee(attendeesForThisEvent, request.getOptionalAttendees())) {
          conflictTimes.add(event.getWhen());
        }
      }
    }
    Collections.sort(conflictTimes, TimeRange.ORDER_BY_START);
    int workableTimeStart = TimeRange.START_OF_DAY;
    int workableTimeEnd = 0;
    int previousLargeConflictEnd = 0;
    for (TimeRange conflictTime : conflictTimes) {
      workableTimeEnd = conflictTime.start();
      if (meetingDuration <= workableTimeEnd - workableTimeStart) {
        workable.add(TimeRange.fromStartEnd(workableTimeStart, workableTimeEnd, false));
      }
      if (previousLargeConflictEnd < conflictTime.end()) {
        previousLargeConflictEnd = conflictTime.end();
      }
      workableTimeStart = previousLargeConflictEnd > conflictTime.end() ? previousLargeConflictEnd : conflictTime.end();
    }
    if (TimeRange.END_OF_DAY - workableTimeStart >= meetingDuration) {
      workable.add(TimeRange.fromStartEnd(workableTimeStart, TimeRange.END_OF_DAY, true));
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
