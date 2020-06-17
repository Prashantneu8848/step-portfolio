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

package com.google.sps.servlets;

import com.google.auto.value.AutoValue;

/** Encapsulate Datastore entity as comment. */
@AutoValue
abstract class Comment {
  abstract String firstName();
  abstract String lastName();
  abstract String commentText();
  abstract String date();
  abstract String id();

  static Builder builder() {
    return new AutoValue_Comment.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setfirstName(String value);
    abstract Builder setLastName(String value);
    abstract Builder setCommentText(String value);
    abstract Builder setDate(String value);
    abstract Builder setId(String value);
    abstract Comment build();
  }
}
