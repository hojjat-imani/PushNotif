# Befrest
Easy To Use Push Notification Library

##Adding the library ##
add the following codes to your library so that you can use the library

1. Add 'http://dl.bintray.com/hojjat-imani/maven' as a repository to your project. your project's build.gradle should look like this:
   ```
 allprojects {
    repositories {
        jcenter()
        maven {
            //other maven repositories
            url 'http://dl.bintray.com/hojjat-imani/maven'
        }
    }
}
   ```
 
2. Add Befrest as a library to your application. Your application module's build.gradle should look like this: 
   ```
dependencies {
      //other libraries
      compile 'com.oddrun.libraries:befrest:0.0.1'
 }
   ```
 
3. Add the following to the Manifast right before the opening application tag
   ```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />


    <!--important! change 'com.oddrun.testlibrary.permission' to your applicatoin package name in the following two lines -->
    
    <permission android:protectionLevel="signature" android:name="com.oddrun.testlibrary.permission.PUSH_SERVICE" />
    <uses-permission android:name="com.oddrun.testlibrary.permission.PUSH_SERVICE" />
   ```
   
4. Add the following right before the closing application tag
  ```
    <service android:name="com.oddrun.befrest.PushService" />
  ```
  
##How To Use##
First of all You need to initialize Befrest. initializing needs to be done just once. You've better to put initialize command in onCreate()
method of you ApplicationConfig class (The class that extends android.app.Application). Also you can put it in onCreate method of your MainActivity
and your good to go.
  ```
  @Override
    public void onCreate() {
        super.onCreate();
        Befrest.initialize(this, APP_ID, AUTH, USER_ID);
    }
  ```
  
To use the Befrest callbacks you need to register BroadcastReceivers with an intent filter with "com.oddrun.befrest.broadcasts.PUSH_RECEIVED"
and "com.oddrun.befrest.broadcasts.UNAUTHORIZED" actions. Your BroadcastReceibver should extend com.oddrun.befrest.BefrestPushBroadcastReceiver and
implement onPushReceived() and onAuthorizeProblem(). (registering receivers in Manifest is recommended)
