
### Usage

#### Environment variables

#### Set up Trakt.tv App

#### Set up Spotify App

### Decisions

Used built-in HttpServer instead of Spring for the HTTP server used in the OAuth flow. The reason for this is that I only needed the HTTP server very briefly, just for the OAuth steps, so using Spring would just add unnecessary bulk to the application. It was a little more involved just to get it working by using the inbuilt HttpServer (since Spring abstracts a few things).

### To-dos

+ [x] Allow for incremental synchronization 
+ [x] Safe access tokens between executions
+ [ ] Improved filtering of insignificant soundtracks
+ [ ] Improved Spotify searching to reduce the number of invalid tracks added
+ [x] Store Spotify tracks in database