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

import com.google.appengine.api.users.User;

public class UserAndLogoutInfo {
    private String email;
    private String nickName;
    private String userId;
    private String logOutUrl;

    UserAndLogoutInfo(User user, String logOutUrl) {
      this.email = user.getEmail();
      this.nickName = user.getNickname();
      this.userId = user.getUserId();
      this.logOutUrl = logOutUrl;
    }
}