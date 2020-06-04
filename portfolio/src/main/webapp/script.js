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

/** 
 * Fetches a random element from an array.
 * 
 * @param {TYPE[]} array the source array
 * @returns TYPE
 * @template TYPE
 */
function getRandomElement(array) {
    return array[Math.floor(Math.random() * array.length)];
}

/** Adds a random quote to the page. */

function addRandomQuote() {
  const quoteContainer = document.getElementById('quote');
  const authorContainer = document.getElementById('author');
  fetch("https://type.fit/api/quotes")
    .then(response => response.json())
    .then(data => {
      const quote = getRandomElement(data);
      quoteContainer.innerText = quote.text;
      // Some quotes do not have author and the API returns none.
      authorContainer.innerText = quote.author || '';
    })
    .catch(error => {
      console.error(error);
      quoteContainer.innerText = "Do what you can, with what you have, where you are.";
      authorContainer.innerText = "Theodre Roosevelt";
    });
}

/** Adds an HTML element from Servlet. */
function showComments() {
  const commentContainer = document.getElementById('comments');
  commentContainer.innerHTML = '';
  const maxComment = document.getElementById("max-comment").value;
  fetch('/data?max-comment=' + maxComment)
    .then(response => response.json())
    .then(comments => {
      comments.forEach(comment => {
        console.log(comment);
        commentContainer.appendChild(createListElement(comment.propertyMap.firstName, comment.propertyMap.lastName,
            comment.propertyMap.commentText, comment.propertyMap.date));
      });
    })
    .catch(error => void console.error(error));
}

/** Creates an <li> element containing text. */
function createListElement(firstName, lastName, comment, date) {
  const liElement = document.createElement('li');
  liElement.setAttribute('class', 'list-group-item');
  liElement.innerText = firstName + " " + lastName + " commented " + comment + " at " + date;
  return liElement;
}

function populateDom() {
  showComments();
  addRandomQuote();
}

/** Removes comments from Datastore. */
function deleteComments() {
  fetch('/delete-data', {
    method: 'POST'
  })
    .then(response => response.text())
    .then(() => {
      showComments();
    })
    .catch(error => void console.error(error));
}
document.addEventListener('DOMContentLoaded', populateDom, false);
