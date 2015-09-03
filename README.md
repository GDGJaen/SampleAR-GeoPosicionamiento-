#SampleAR(GeoPosicionamiento)
This is simple Augmented Reality browser application. These applications are used for showing points of interest with AR. 
You have to point with your device to somewhere and these apps will show you information about what you are seeing in your device. 

####For using the app:
* Download the app <a target="_blank" href="https://github.com/Jamargle/SampleAR-GeoPosicionamiento-/blob/master/app/build/outputs/apk/app-debug.apk?raw=true">here</a>.
* Allow installation of non-Market apps in Settings > Security
* Put this file into your Android device.
* Search app-debug.apk in your device and install it.
* Launch SampleAR(GeoPosicionamiento) and point with your device to some place with points of interest.

####For modifing SampleAR(GeoPosicionamiento) or doing something with it follow these steps:
* Download the project from [here](https://github.com/Jamargle/SampleAR-GeoPosicionamiento-/archive/master.zip).
* Unzip and go (in Android Studio) to File > New > Import project. Import the project.

#####Packages on src:
* Paintables: It contains some classes for drawing elements on screen as points, boxes, ...
* Utilities: It contains utilities for the whole application about camera, location, mathematics, ...
* Data: It contains DataSource and LocalDataSource which are classes for obtaining data of points of interest.
* Sync: Inside data, we have sync package that contains classes for downloading information from the Internet

#####Important Classes:
* Marker: The function update() contains other functions as PopulateMatrices() for calculating its position on screen and 
functions as updateRadar and updateView for updating and drawing that position. Also it contains a simple mechanism for 
adjust position in collisions between markers.
* SensorActivity: This class is for collecting and managing information of sensors.
* AugmentedView: This class is a view for drawing markers on it and contains the algorithm for managing collisions 
of markers.
* AugmentedActivity: Is a SensorActivity with an Augmented View.
* MainActivity: Is an activity that extends AugmentedActivity and it's able to manage controls like a menu, for example. 
Also it's the responsible for downloading data from data sources.
* ARData: It's an abstract class for managing a lot of stuff for the whole application. It contains variables for the device
orientation, the current location, zoom level, the list of markers in memory, it's the responsible for updating this list of 
markers on each moment and for updating the relative position of markers.


You can change what you need.


