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
