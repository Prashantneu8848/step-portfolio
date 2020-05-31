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
 * Adds a random greeting to the page.
 */

function addRandomQuote() {
  const quoteContainer = document.getElementById('quote');
  const authorContainer = document.getElementById('author')
  fetch("https://type.fit/api/quotes")
    .then(response => {
      return response.json();
    })
    .then(data => {
      const quoteAndAuthor = data[Math.floor(Math.random() * data.length)];
      const author = quoteAndAuthor.author;
      const quote = quoteAndAuthor.text;
      // Some quotes do not have author and the API return none.
      if (!author) author = '';
      quoteContainer.innerText = quote;
      authorContainer.innerText = author;
    })
    .catch(error => {
      console.error(error);
      quoteContainer.innerText = "Do what you can, with what you have, where you are.";
      authorContainer.innerText = "Theodre Roosevelt";
    });
}

document.addEventListener('DOMContentLoaded', function() {
  addRandomQuote();
}, false);
