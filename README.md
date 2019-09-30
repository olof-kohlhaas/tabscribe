![](https://olof-kohlhaas.github.io/images/logo_title.png)
# TabScribe - Guitar Tab Transcription App
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

TabScribe is a tool for transcribing any music track to guitar tabs.

[Demo video on YouTube](https://youtu.be/BGP1hx7KGBg):

[![Demo video on YouTube](https://olof-kohlhaas.github.io/images/tabgif.gif)](https://youtu.be/BGP1hx7KGBg)

The pre-alpha version of TabScribe comes with the following features:
* decoding audio files with FFmpeg
* pitch-shifting: raising and lowering the pitch of the music track by semitones
* time-stretching: changing the playback speed without affecting the track's pitch
* playback of transcribed guitar tabs by audio synthesis with the Karplus-Strong-Algorithm
* translating tone frequencies to the twelve-tone equal temperament
* manual separation into beats and measures
* beatwise playback of the track and guitar tabs

To build and run from source, point to the JDK-11 in the `gradle.properties`

*e.g. on Windows:*
``` groovy
org.gradle.java.home=C:\\Program Files\\Java\\jdk-11.0.1 
```

*e.g. on OSX/Linux:*
``` groovy
org.gradle.java.home=/opt/jdk-11.0.1 
```

Then use the Gradle-Wrapper to run the application:

*on Windows:*
``` 
gradlew.bat sone-tabscribe:run
```

*on OSX/Linux:*
``` 
./gradlew sone-tabscribe:run
```

For upcoming binaries with integrated runtimes watch the [releases](https://github.com/olof-kohlhaas/tabscribe/releases).