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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long meetingDuration = request.getDuration();

    ArrayList<TimeRange> workableTimesForMandatoryAttendees = findOptimalTime(events, request, true);

    // If there are no optional attendees, then provide time for mandatory attendees.
    if (request.getOptionalAttendees().isEmpty()) return workableTimesForMandatoryAttendees;

    ArrayList<TimeRange> workableTimesForOptionalAttendees = findOptimalTime(events, request, false);

    // If there are no mandatory attendees, then provide time for optional attendees.
    if (request.getAttendees().isEmpty()) return workableTimesForOptionalAttendees;

    // If optional attendees could not attend, then provide time for mandatory attendees only.
    if (workableTimesForOptionalAttendees.size() == 0) return workableTimesForMandatoryAttendees;
    
    ArrayList<TimeRange> workableForBoth = new ArrayList<>();

    int mandatoryPointer = 0;
    int optionalPointer = 0;

    while (mandatoryPointer < workableTimesForMandatoryAttendees.size()
        && optionalPointer < workableTimesForOptionalAttendees.size()) {
      
      TimeRange workableForMandatoryAttendees = workableTimesForMandatoryAttendees.get(mandatoryPointer);
      TimeRange workableForOptionalAttendees = workableTimesForOptionalAttendees.get(optionalPointer);


      if (workableForMandatoryAttendees.start() >= workableForOptionalAttendees.end()) {
        optionalPointer++;
      } else if (workableForOptionalAttendees.start() >= workableForMandatoryAttendees.end()) {
        mandatoryPointer++;
      } else if (workableForOptionalAttendees.contains(workableForMandatoryAttendees)) {
        workableForBoth.add(workableForMandatoryAttendees);
        mandatoryPointer++;
      } else if (workableForMandatoryAttendees.contains(workableForOptionalAttendees)) {
        workableForBoth.add(workableForOptionalAttendees);
        optionalPointer++;
      } else if (workableForOptionalAttendees.overlaps(workableForMandatoryAttendees)) {
        
        // If allowable time range overlaps then find if meeting can happend during that time.
        int mandatoryWorkableTimeStart = workableForMandatoryAttendees.start();
        int optionalWorkableTimeStart = workableForOptionalAttendees.start();
        if (meetingDuration < Math.abs(mandatoryWorkableTimeStart - optionalWorkableTimeStart)) {
          workableForBoth.add(workableForMandatoryAttendees);
          mandatoryPointer++;
          optionalPointer++;
        }
      }
    }

    // If time conflicts for optional attendees to attend, then provide time for mandatory attendee only.
    if (workableForBoth.isEmpty()) return workableTimesForMandatoryAttendees;

    return workableForBoth;
  }

  private ArrayList<TimeRange> findOptimalTime(Collection<Event> events, MeetingRequest request, Boolean mandatory) {
    long meetingDuration = request.getDuration();
    ArrayList<TimeRange> workable = new ArrayList<>();        
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
