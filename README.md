GPSSample
=========

Dependencies
=========

## Google Maps
Google Maps integration is an important part of GPSSample.  Sign up for a key [here](https://developers.google.com/maps/documentation/android-sdk/signup)
Add your api key to the project by adding a file `geo_api.properties` to your local source
```
#Contains API Key. Commit to internal source control; avoid making secret public.
apiKey=#{Your key here}
```
*Google has an API key available for their `android-maps-utils` project


## Fabric
Fabric is used for crash reporting. You must provide a valid API Key to build the project

To enable crash reporting, add a file `fabric.properties` to your local source
```
#Contains API Secret used to validate your application. Commit to internal source control; avoid making secret public.
#Mon Aug 06 20:15:06 EDT 2018
apiSecret=#{Your key here}
apiKey=#{Your key here}
```
