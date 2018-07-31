# erxes-android

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.batorshih:erxes-android:v1.5'
	}
	
Step 3. Add in your own code

	Config.Init(this,"yPv5aN","your_host_address"); // like 103.90.1.2
	Config.Start();
