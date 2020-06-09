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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that checks if user is loggin in. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json;");
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn()) {
      Gson gson = new Gson();
      User user = userService.getCurrentUser();
      String logoutUrl = userService.createLogoutURL("/");

      LoginServlet.UserAndLogoutInfo userInfo = 
          new LoginServlet.UserAndLogoutInfo(user, logoutUrl);

      String json = gson.toJson(userInfo);

      response.getWriter().println(json);
    } else {
      String loginUrl = userService.createLoginURL("/");
      response.sendRedirect(loginUrl);
    }
  }

  static class UserAndLogoutInfo {
    String email;
    String nickName;
    String userId;
    String logOutUrl;

    UserAndLogoutInfo(User user, String logOutUrl) {
      this.email = user.getEmail();
      this.nickName = user.getNickname();
      this.userId = user.getUserId();
      this.logOutUrl = logOutUrl;
    }
  }
}
