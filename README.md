# Tracker

<b>Where can I find information about this project?</b>

- Official website of the OSP project: https://www.compadre.org/osp/<br>
- Official website of the Tracker project: https://physlets.org/tracker/


<b>How to build and run OpenSourcePhysics Tracker?</b>

<b>First step:</b><br>
Install Linux operating system on your computer (type and version can be any, for example Ubuntu LTE - https://ubuntu.com/download/desktop).

<b>Second step:</b><br>
Download and install the FFMpeg library on your Linux system:

`sudo apt-get update`<br>
`sudo apt-get upgrade`<br>
`sudo add-apt-repository ppa:jonathonf/ffmpeg-4`<br>

If you have any difficulties or the repository at this point has become unavailable for various reasons, you can tell the FFmpeg library from the official source: https://ffmpeg.org/

<b>Third step:</b><br>
Downlad and install Java RE and Java DK:

`sudo apt-get update`<br>
`sudo apt-get upgrade`<br>
JRE:
`sudo apt install openjdk-8-jre`<br>
JDK:
`sudo apt install openjdk-8-jdk-headless`<br>

<b>Fourth step:</b><br>
Install Gradle on Linux System

Step 1. Download the latest Gradle distribution: https://gradle.org/releases/
The distribution ZIP file comes in two flavors:
- Binary-only (bin)
- Complete (all) with docs and sources

Step 2. Unpack the distribution:
Unzip the distribution zip file in the directory of your choosing, e.g.:<br>

`❯ mkdir /opt/gradle`<br>
`❯ unzip -d /opt/gradle gradle-6.8.3-bin.zip`<br>
`❯ ls /opt/gradle/gradle-6.8.3`<br>

Step 3. Configure your system environment:<br>
Configure your PATH environment variable to include the bin directory of the unzipped distribution, e.g.:<br>

`❯ export PATH=$PATH:/opt/gradle/gradle-6.8.3/bin`<br>

Alternatively, you could also add the environment variable GRADLE_HOME and point this to the unzipped distribution. Instead of adding a specific version of Gradle to your `PATH`, you can add `$GRADLE_HOME/bin` to your `PATH`. When upgrading to a different version of Gradle, just change the `GRADLE_HOME` environment variable.

Step 4. Verifying installation:<br>

`❯ gradle -v`<br>
(the output should be gradle version).<br>

<b>Fifth step:</b><br>
Download the Tracker project from the GitHub repository: https://github.com/OpenSource-Physics-Tracker/Tracker <br>
If you do not clone, but download the `.zip` file, you will need to unzip it. <br>

<b>Sixth step:</b><br>
Build project(-jar file) with Gradle Wrapper

The Gradle Wrapper is the preferred way of starting a Gradle build. It consists of a batch script for Windows and a shell script for OS X and Linux. These scripts allow you to run a Gradle build without requiring that Gradle be installed on your system. This used to be something added to your build file, but it’s been folded into Gradle, so there is no longer any need. Instead, you simply use the following command.<br>

`gradle wrapper --gradle-version 6.0.1`

After this task completes, you will notice a few new files. The two scripts are in the root of the folder, while the wrapper jar and properties files have been added to a new gradle/wrapper folder.<br>

![image](https://user-images.githubusercontent.com/49695119/113479123-c7ea6f80-9495-11eb-962f-6bebb749be9c.png)
            
The Gradle Wrapper is now available for building your project. Add it to your version control system, and everyone that clones your project can build it just the same. It can be used in the exact same way as an installed version of Gradle. Run the wrapper script to perform the build task, just like you did previously:<br>

`./gradlew build`<br>

The first time you run the wrapper for a specified version of Gradle, it downloads and caches the Gradle binaries for that version. The Gradle Wrapper files are designed to be committed to source control so that anyone can build the project without having to first install and configure a specific version of Gradle.<br>

At this stage, you will have built your code. You can see the results here:<br>

In folder `libs` you can see your `jar`file.<br>

Folder `libs` path: `yourProjectFolder -> build -> libs`<br>

<b>Seventh step:</b><br>
Assign path to FFMpeg and Tracker. To do this, we need to execute the command, and assign our paths in the command itself:<br>

`java -cp build/libs/yourTrackerJarFile.jar:$FFMPEG_HOME/ffmpeg.jar:$FFMPEG_HOME/bridj.jar org.opensourcephysics.cabrillo.tracker.tracker.Tracker $*`

<b>Eighth step:</b><br>
Run Tracker application with jar file:

`java -jar your/jarFile/path/YourTrackerFile.jar`
