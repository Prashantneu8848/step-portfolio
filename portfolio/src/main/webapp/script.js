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

  fetch('/data')
    .then(response => response.json())
    .then(comments => {
      comments.forEach(comment => {
      commentContainer.appendChild(createListElement(comment));
      });
    })
    .catch(error => void console.error(error))
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.setAttribute('class', 'list-group-item');
  liElement.innerText = text;
  return liElement;
}

document.addEventListener('DOMContentLoaded',addRandomQuote, false);
