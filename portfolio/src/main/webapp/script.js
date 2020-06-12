// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import { MAP_STYLE, PLACES } from './constant.js';

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
  fetch('https://type.fit/api/quotes')
    .then(response => response.json())
    .then(data => {
      const quote = getRandomElement(data);
      quoteContainer.innerText = quote.text;
      // Some quotes do not have author and the API returns none.
      authorContainer.innerText = quote.author || '';
    })
    .catch(error => {
      console.error(error);
      quoteContainer.innerText = 'Do what you can, with what you have, where you are.';
      authorContainer.innerText = 'Theodore Roosevelt';
    });
}

/** Fetches comments from servlet and adds in DOM. */
function showComments() {

  const maxComment = sessionStorage.getItem('max-comment') || 1;
  document.getElementById('max-comment').value = maxComment;
  document.getElementById('comments').innerHTML = '';
  
  fetch('/data?max-comment=' + maxComment)
    .then(response => response.json())
    .then(comments => {
      comments.forEach(comment => {
        renderListComments(comment);
      });
    })
    .catch(error => void console.error(error));
}

/** Resets session storage value and shows that number of comments. */
function refreshComments() {
  const maxComment = document.getElementById('max-comment').value;

  sessionStorage.setItem('max-comment', maxComment);
  showComments();
}

/** 
 * Creates an <li> element containing text.
 * 
 * @param {string} firstName First Name of the commenter
 * @param {string} lastName Last Name of the commenter
 * @param {string} commentText comment made by the commenter
 * @param {string} date date when the comment was made
 */
function renderListComments({firstName, lastName, commentText, date}) {
  const template = document.getElementById('item-template');
  const content = template.content.cloneNode(true);

  content.querySelector('.first-name').innerText = firstName;
  content.querySelector('.last-name').innerText = lastName;
  content.querySelector('.comment-text').innerText = commentText;
  content.querySelector('.date').innerText = date;

  document.getElementById('comments').appendChild(content);
}

function createMap() {
  const map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 19.741755, lng: -155.844437},
    zoom: 2,
    disableDefaultUI: true,
    styles: MAP_STYLE
  });
  showInfoWindowAndMarker(map, PLACES);
}

/** 
 * Creates a window with some context about a place at given marker on the map.
 * 
 * @param {google.maps.Map} map map where to show the window
 * @param {object[]} PLACES array which contains object with place location and info
 */
function showInfoWindowAndMarker(map, places) {
  places.forEach((place) => {

    const marker = new google.maps.Marker({
      position: place.coordinate,
      map,
      title: place.contentText
    });

    const infoWindow = new google.maps.InfoWindow({content: place.contentText});
    infoWindow.open(map, marker);
  });
}

/** Handles user login. */
function login() {

  fetch('/login')
    .then(response => response.json())
    .then(userInfo => {
      sessionStorage.setItem('logged-in', userInfo.nickname);
      fillDropDownMenu(userInfo.nickname, userInfo.logOutUrl, '#')
    })
    .catch(() => {
      sessionStorage.setItem('logged-in', '');
      showCommentInfo();
      displayLoginOption();
    });

}

/** 
 * Fill information in Dropdown menu in DOM.
 * 
 * @param {string} nickname nickname of the user loggen in
 * @param {string} logOutUrl url to log out the user
 * @param {string} setNicknameUrl url to take user to nickname setup page.
 */
function fillDropDownMenu(nickname, logOutUrl, setNicknameUrl) {
  const dropDownContainer = document.querySelector('.login');
  dropDownContainer.querySelector('.item-1').innerText = nickname;
  dropDownContainer.querySelector('.item-2').setAttribute('href', logOutUrl);
  dropDownContainer.querySelector('.item-3').setAttribute('href', setNicknameUrl);
}

/** Displays login button when user is not signed in. */
function displayLoginOption() {
  const userInfoContainer = document.querySelector('.login');
  userInfoContainer.innerHTML = '';

  const loginStatus = document.createElement('a');
  loginStatus.setAttribute('class', 'nav-link');
  loginStatus.innerText = 'LOG IN';
  loginStatus.setAttribute('href', '/login');

  userInfoContainer.appendChild(loginStatus);
}


/** Displays text to login to see comment section. */
function showCommentInfo() {
  document.getElementById('comment-section').style.display = 'none';
  document.getElementById('comment-info').innerText= 'Log In to add and view comments';
}


function populateDom() {
  login();
  showComments();
  addRandomQuote();
  createMap();
}

/** Removes comments from Datastore. */
function deleteComments() {
  if (window.confirm("Do you really want to delete all comments ?")) {
    fetch('/data', {method: 'DELETE'})
      // Call showComments function for the server to be in sync with the lost data.
      .then(showComments)
      .catch(error => void console.error(error));
  }
}

document.addEventListener('DOMContentLoaded', populateDom, false);
document.getElementById('max-comment').addEventListener('change', refreshComments);
document.getElementById('delete-comment').addEventListener('click', deleteComments);
