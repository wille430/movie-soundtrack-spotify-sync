## Usage

### Environment variables

| Env                 | Function                                                        | Required |
| ------------------- | --------------------------------------------------------------- | -------- |
| TRAKT_CLIENT_ID     | Client ID of your Trakt.tv application                          | Yes      |
| TRAKT_CLIENT_SECRET | Client secret of your Trakt.tv application                      | Yes      |
| SPOTIFY_CLIENT_ID   | CLient ID of your Spotify application                           | Yes      |
| PORT                | The port to use for the HTTP server that handles the OAuth flow | No       |

### Set up Trakt.tv App

Create a new app at https://trakt.tv/oauth/applications/new.

Name the app something suiting.

Set `Redirect uri` to `http://localhost:3000/trakt/redirect`. If you have specified the port to be something else, replace `3000` with the specified port.

Finally, set `Javascript (cors) origins` to `http://localhost:3000`. Change the port if necessary and save.

### Set up Spotify App

Follow this link: https://developer.spotify.com/dashboard/create.

Give the app a name.

Set `Redirect URIs` to `http://localhost:3000/spotify/redirect`, select Web API and click save.

### Docker compose

Create a docker-compose.yaml file.

```
version: "3"
services:
  app:
    image: movie-soundtrack-spotify-sync
    container_name: movie-soundtracks-spotify-sync
    build: ./
    restart: unless-stopped
    env_file: ./app.properties
    ports:
      - 3000:3000

```

Run `docker compose build` to build the image, and `docker compose up` to start the container.

Follow the instructions in the console to authenticate with Trakt and Spotify.

## Decisions

Used built-in HttpServer instead of Spring for the HTTP server used in the OAuth flow. The reason for this is that I only needed the HTTP server very briefly, just for the OAuth steps, so using Spring would just add unnecessary bulk to the application. It was a little more involved just to get it working by using the inbuilt HttpServer (since Spring abstracts a few things).

## To-dos

- [x] Allow for incremental synchronization
- [x] Safe access tokens between executions
- [ ] Improved filtering of insignificant soundtracks
- [ ] Improved Spotify searching to reduce the number of invalid tracks added
- [x] Store Spotify tracks in database
