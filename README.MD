


# How to use this. 


Publish to maven local to use it.


```
 ./gradlew --refresh-dependencies :finderLib:clean :finderLib:publishToMavenLocal
 ```


Add this to the project where it is using.
```
repositories {
	mavenLocal()
}

dependencies {
    implementation 'apri:finderLib'
}
```