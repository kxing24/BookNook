# BookNook

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
<!--2. [Schema](#Schema)-->

## Overview
### Description
An app that helps you explore different books, find your next read, and interact with fans of the same books / authors.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Books / Social Networking
- **Mobile:** Mobile makes the app easily-accessible and convenient.
- **Story:** Allows exploration of various books. Connect readers who enjoy the same books / authors.
- **Market:** Anyone looking for their next read or wanting to connect with fans of the same books / authors.
- **Habit:** People use this when they're looking for a book to read or wanting to discuss a book.
- **Scope:** Allows users to search for books, rate / review books, join book / author groups, and post in groups.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Users can search for books (Open Library API, Google Books API, Internet Archive REST API)
* Users can rate / review books
* User can create an account
* User can login
* User can join groups
* User can post in groups

**Optional Nice-to-have Stories**

* User can shelve / sort books
* User can click on a link to purchase a book
* User can react to / comment on posts
* User can friend / follow other users
* Update reading progress on a book
* Book giveaways?
* Incorporate fanfiction?
* Include trigger / content warnings?
* Books on shelves sort by (date shelved, rating, etc.)

### 2. Screen Archetypes

* Login screen
    * User can login
    * User can create an account
* Book search
    * User can search for books
    * User can click on book for detail view
* Book details
    * User can see book rating
    * User can rate book
    * User can see reviews
    * User can write a review
    * User can join book / author group
* Book / author group
    * User can make a post

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home / Feed (show posts from groups, friends' posts)
* Shelves
* Groups
* Friends
* Search

**Flow Navigation** (Screen to Screen)

* Search
    * Book details
        * Book group
        * Author group
        * Reviews? (either a separate screen or part of this screen)
* Shelves
    * Shelf details
        * Book details
* Feed
    * Post details?
* Group
    * Compose post
    * Post details?

## Wireframes
<img src="https://github.com/kxing24/BookNook/raw/master/wireframes.png" width=600>

## Schema
[This section will be completed in Unit 9]
### Models
* Book
    * Shelf
    * User rating
    * Average rating
    * Title
    * Auther
    * Description / overview
    * Cover path
    * id
* User (ParseUser)
    * Username
    * Password
    * Profile picture (URL)
    * Groups
* Post
    * User
    * Description / body
    * Image
    * CreatedAt
    * Group
    * Liked
    * Like count
* Group
    * Book
    * Feed (list of posts)
    * Members (list of users)
    * Member count ?

### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
