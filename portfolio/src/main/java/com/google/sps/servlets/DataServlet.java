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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private Query query;
  private PreparedQuery results;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    query = new Query("Comment");
    results = datastore.prepare(query);

    int maxCommentLimit = getCommentLimitFromParam(request);

    List<Comment> comments = new ArrayList<>();
    Iterable<Entity> fetchedComments =
        results.asIterable(FetchOptions.Builder.withLimit(maxCommentLimit));

    for (Entity entity : fetchedComments) {
      String firstName = (String) entity.getProperty("firstName");
      String lastName = (String) entity.getProperty("lastName");
      String commentText = (String) entity.getProperty("commentText");
      String date = (String) entity.getProperty("date");
      String commentId = (String) entity.getProperty("commentId");

      Comment comment = new Comment(firstName, lastName, commentText, date, commentId);
      comments.add(comment);
    }

    Gson gson = new Gson();
    String json = gson.toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
  
  private int getCommentLimitFromParam(HttpServletRequest request) {
    int maxComment;

    try {
      maxComment = Integer.parseInt(request.getParameter("max-comment"));
    } catch (NumberFormatException e) {
      maxComment = Integer.MAX_VALUE;
    }
    return maxComment;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String firstName = getParameter(request, "first-name", "");
    String lastName = getParameter(request, "last-name", "");
    String commentText = getParameter(request, "comment", "");
    Date commentDate = new Date();
    String commentId = UUID.randomUUID().toString();

    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("firstName", firstName);
    commentEntity.setProperty("lastName", lastName);
    commentEntity.setProperty("commentText", commentText);
    commentEntity.setProperty("date", commentDate.toString());
    commentEntity.setProperty("commentId", commentId);

    datastore.put(commentEntity);
    
    response.sendRedirect("/index.html#connect");
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentId = request.getParameter("commentId");

    if (!commentId.equals("all")) {
      query =
          new Query("Comment")
              .setFilter(new Query.FilterPredicate("commentId", Query.FilterOperator.EQUAL, commentId));

      results = datastore.prepare(query);
      datastore.delete(results.asSingleEntity().getKey());
    } else {
      query = new Query("Comment").setKeysOnly();
      results = datastore.prepare(query);
    
      for (Entity entity : results.asIterable()) {
        datastore.delete(entity.getKey());
      }
    }

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

}
