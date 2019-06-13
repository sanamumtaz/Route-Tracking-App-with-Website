# Route-Tracking-App-with-Website
Android and Web development - Google APIs

This was my internship project. First experience with Google APIs.

Purpose of the application is to assign a travelling route to a user (employee) while bounding the user to travel within the described route. There are two sides of this project: Web and Android. There is an admin on web side who assigns routes, and there are users on Android side who have to follow the assigned route.
## Requirements:
**Admin - Web**: - Search locations, select starting and ending of journey
- Select a route among multiple routes suggested
- Monitor the travel progress of user
- Be notified when user diverts from the assigned route
**User - Android**: - User can view the assigned route using the route Id (already known to user)
- Start travelling on route, and upon reaching destination stop
- Be notified whenever user diverts from the path
## Implementation
### Google APIs: 
- Maps JavaScript API.   - Maps SDK for Android
### Admin - Web: 
HTML, CSS, Bootstrap, Javascript, AJAX, Maps Javascript API
- Start and destination markers drag listener
- Search: Places library, Geometry library
- Routes: Directions Services
- Upon confirmation, save the coordinates of highlighted polyline/route
- Retrieve all route Ids
- Retrieve admin selected route of selected route Id
- Retrieve user’s path for the selected route Id
- Continuously update
- Get notified if user diverted from path
### Server side:
All PHP
### User - Android app:
Java, XML, Maps SDK for Android
- Request location, Wi-Fi, GPS access
- Get updated location
- Obtain route and display polyline
- Start journey by sending coordinates of current location
- Geofencing on path
- Stop journey

Used Fast Android Networking Library: https://github.com/amitshekhariitbhu/Fast-Android-Networking#fast-android-networking-library
