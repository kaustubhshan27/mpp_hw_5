# ECE/CS-5510 Homework 5 Part II Template (Fall 2024)

## Contact
Please use the Homework5 discussion forum on Canvas for any questions.

## Overview
Use this template to submit your solution to Part II of Homework 5. Please feel free to modify any artifacts in the src folder.

## Submission
You must zip this folder (with the updated artifacts) and submit to Canvas

## Building and Running the template
The below instructions should be run from the top-level directory.

### Running Unit Test
Run the following command to run the unit test:

Linux: `./gradlew build`

Windows: `./gradlew.bat build`

This will produce the binaries in the build folder.
There is no requirement to write or satisfy unit tests for this homework.

### Running Benchmark main programs:

__Benchmark__:
`java -cp build/libs/homework5.jar edu.vt.ece.hw5.Benchmark <YOUR_ARGS>`

Replace `<YOUR_ARGS>` with the arguments you would like to pass to the respective main programs. 
Note that the TA will only run the above command to verify if the submission works. 
Furthermore, the TA will also inspect your respective implementations.

## Intellij
This gradle project can be imported into Intellj by going to `File -> Open` and choosing this directory. As soon as you open the project, a pop-up dialog may appear in the bottom right of the screen asking to import the project with Gradle. Accept it.
For more instructions, go to https://www.jetbrains.com/help/idea/gradle.html#gradle_import_project_start