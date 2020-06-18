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
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that checks if user is logged in and sets nickname. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  
  private final UserService userService = UserServiceFactory.getUserService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {


    if (userService.isUserLoggedIn()) {
      Gson gson = new Gson();
      User user = userService.getCurrentUser();
      String logoutUrl = userService.createLogoutURL("/");
      Entity userInfoEntity = getUserinfoEntity(user.getUserId());

      String nickname = userInfoEntity == null ? "" : (String) userInfoEntity.getProperty("nickname");

      UserInfo userInfo = new UserInfo(user, nickname, logoutUrl);

      String json = gson.toJson(userInfo);
      
      response.setContentType("application/json;");
      response.getWriter().println(json);
    } else {
      String loginUrl = userService.createLoginURL("/");
      response.sendRedirect(loginUrl);
    }
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String nickname = getParameter(request, "nickname").orElse("");
    String id = userService.getCurrentUser().getUserId();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity userInfoEntity = getUserinfoEntity(id);

    if (userInfoEntity == null) {
      userInfoEntity = new Entity("UserInfo");
      userInfoEntity.setProperty("id", id);
    }

    userInfoEntity.setProperty("nickname", nickname);
    datastore.put(userInfoEntity);

    response.sendRedirect("/index.html");
  }

  private Optional<String> getParameter(HttpServletRequest request, String name) {
    return Optional.ofNullable(request.getParameter(name));
  }

  /**
   * Returns the UserInfo entity with user id.
   * Given id is not of UserInfo kind but a field of that kind.
   */
  private Entity getUserinfoEntity(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    return results.asSingleEntity();
  }
}
