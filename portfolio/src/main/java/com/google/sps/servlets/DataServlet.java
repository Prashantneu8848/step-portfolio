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
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap; 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  List<HashMap<String, String>> comments = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    Gson gson = new Gson();
    String json = gson.toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String firstName = getParameter(request, "first-name", "");
    String lastName = getParameter(request, "last-name", "");
    String commentText = getParameter(request, "comment", "");
    Date commentDate = new Date();

    Entity taskEntity = new Entity("comment");
    taskEntity.setProperty("firstName", firstName);
    taskEntity.setProperty("lastName", lastName);
    taskEntity.setProperty("commentText", commentText);
    taskEntity.setProperty("date", commentDate);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    HashMap<String, String> commentsData = makeHashmapOfFields(firstName, lastName, commentText);

    comments.add(commentsData);
    
    response.sendRedirect("/index.html#connect");
  }

/**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

/**
   * @return  HashMap conatining the first name, last name, comment and date when comment
   *           was made
   */
  private HashMap<String, String> makeHashmapOfFields(String firstName, String lastName, String comment) {
    HashMap<String, String> fieldValues = new HashMap<>();
    Date commentDate = new Date();

    fieldValues.put("firstName", firstName);
    fieldValues.put("lastName", lastName);
    fieldValues.put("comment", comment);
    fieldValues.put("commentDate", commentDate.toString());
    
    return fieldValues;
  }
}
