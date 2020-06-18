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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds a suitable time for a meeting to happen between the mandatory
 * and optional attendees.
 */
public final class FindMeetingQuery {
  
  /**
   * Finds the time that is the most workable time slot for the meetings.
   *
   * @param events  collection of events happening that day
   * @param request meeting that needs to be scheduled for the attendees
   * @return collection of time when the meeting can be scheduled.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    long meetingDuration = request.getDuration();

    // No meeting can happen more than a day.
    if (meetingDuration > 24*60) return Collections.emptyList();

    // If there are no attendees including optional, the meeting can happen anytime during the day.
    if (request.getAttendees().isEmpty() && request.getOptionalAttendees().isEmpty()) return Arrays.asList(TimeRange.WHOLE_DAY);

    // If there are no events for the attendees, the meeting can happen whole day.
    if (events.isEmpty()) return Arrays.asList(TimeRange.WHOLE_DAY);


    ArrayList<TimeRange> workableTimesForMandatoryAttendees = findOptimalTimes(events, request, true);

    // If there are no optional attendees, then provide time for mandatory attendees.
    if (request.getOptionalAttendees().isEmpty()) return workableTimesForMandatoryAttendees;

    ArrayList<TimeRange> workableTimesForOptionalAttendees = findOptimalTimes(events, request, false);

    // If there are no mandatory attendees, then provide time for optional attendees.
    if (request.getAttendees().isEmpty()) return workableTimesForOptionalAttendees;

    // If optional attendees could not attend, then provide time for mandatory attendees only.
    if (workableTimesForOptionalAttendees.isEmpty()) return workableTimesForMandatoryAttendees;
    
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
        
        // If allowable time range overlaps then find if meeting can happen during that time.
        int mandatoryWorkableTimeStart = workableForMandatoryAttendees.start();
        int optionalWorkableTimeStart = workableForOptionalAttendees.start();
        if (meetingDuration < Math.abs(mandatoryWorkableTimeStart - optionalWorkableTimeStart)) {
          workableForBoth.add(workableForMandatoryAttendees);
          mandatoryPointer++;
          optionalPointer++;
        }
      }
    }

    // If time conflicts for optional attendees to attend, then provide time for mandatory attendees only.
    if (workableForBoth.isEmpty()) return workableTimesForMandatoryAttendees;

    return workableForBoth;
  }

  /**
   * Finds the optimal time between attendees events and meeting duration.
   *
   * @param events  collection of events happening that day
   * @param request meeting that needs to be scheduled for the attendees
   * @param mandatory determines if the meeting request involves mandatory attendees.
   *
   * @return collection of time when the meeting can be scheduled.
   */
  private ArrayList<TimeRange> findOptimalTimes(Collection<Event> events, MeetingRequest request, boolean mandatory) {
    long meetingDuration = request.getDuration();
    ArrayList<TimeRange> workableTimes = new ArrayList<>();        
    List<TimeRange> conflictTimes = new ArrayList<>();
    for (Event event: events) {
      Set<String> attendeesForThisEvent = event.getAttendees();
      if (mandatory) {
        if (!Collections.disjoint(attendeesForThisEvent, request.getAttendees())) {
          conflictTimes.add(event.getWhen());
        }
      } else {
        if (!Collections.disjoint(attendeesForThisEvent, request.getOptionalAttendees())) {
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
        workableTimes.add(TimeRange.fromStartEnd(workableTimeStart, workableTimeEnd, false));
      }
      if (previousLargeConflictEnd < conflictTime.end()) {
        previousLargeConflictEnd = conflictTime.end();
      }
      workableTimeStart = previousLargeConflictEnd > conflictTime.end() ? previousLargeConflictEnd : conflictTime.end();
    }
    if (TimeRange.END_OF_DAY - workableTimeStart >= meetingDuration) {
      workableTimes.add(TimeRange.fromStartEnd(workableTimeStart, TimeRange.END_OF_DAY, true));
    }
    return workableTimes;
  }

}
