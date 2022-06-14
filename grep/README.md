# **Grep Extension**
Shenyi Li, Felianne Teng, Jia Yue Wu

## **What is it?**
A user-interactive graphical representation of grep’s NFA. The user can enter a regex and string and watch the NFA be 
constructed and traversed in live time. The user can also watch a force-spring animation of the NFA. 

## **Why this?**
From our experience of writing grep, the debugging process took a very long time because we had to hand draw every NFA.
This gave us the idea of creating an NFA visualization. Our graphical interface is extremely useful for debugging, 
educational, and demonstration purposes. The force-spring algorithm helps spread out the NFA nodes in and allows us to 
use the “mass” (number of children) of NFA states to determine the velocity of each node.

## **Implementation**
1. Designed force-spring algorithm with suitable force constants and dampening behavior. 
2. Integrated graphics updates/ticks with NFA construction/traversal algorithms
3. Created graphics and user input using Kotlin-based JFrame in a user-friendly and clear way

## **Original planned items**
1. Graphical representation of the NFA of a regex
2. User input of regex and live NFA drawing/updates
3. User input of string and live traversal of NFA

## **Completed items**
1. Graphical representation of the NFA of a regex
a. Displays step by step procedure of the NFA construction
2. User input of regex and live NFA drawing/updates
a. Updates calculated positions of nodes using a force-spring algorithm
b. Allows user to input regex using GUI
3. User input of string and live traversal of NFA

## **Rework Improvements**
As per Adam's comments, our first interface was not very organized and hard to read. This was because our initialization
of position was originally random. For this new implementation, we make sure that the nodes are evenly placed so that 
initial configuration is neat and easy to read. Another problem we addressed was the construction, which we also fixed 
to update one level of node at a time, so it is easier to follow. We added a _Reset positions_ button so that after 
playing the animation, the user can reset the NFA to the initial configuration which is easier to see. Furthermore, we
added boundaries, so that during the animation, the NFA nodes do not go off the screen like it used to. With these 
improvements, our graphical interface is much clearer and easier to use. 

## **Our Team**
Shenyi: graphics abstraction (4 + 20 hours)

Felianne: force-spring algorithm and debugging/testing (4 + 16 hours)

Jia: data structure abstractions and demo implementation (4 + 18 hours)

## **How to run the demo**
Jar file download: https://gitlab.caltech.edu/jwu7/grep/-/blob/master/out/artifacts/grep_jar/grep.jar

Note: During the animation, no buttons can be pressed. Also, keep the screen size the same ie. if the user has it as a small
screen do not maximize and vice versa.

To build an NFA, click on _Click to input a regular expression_. A smaller window will pop up where the user can enter
a regular expression. An NFA will be built, its epsilon transitions deleted, and a neat initial configuration of the 
NFA will be shown with the nodes spaced evenly.

To witness the construction of the NFA by level, click on _Click to construct NFA_ one time each for each level. The user
will be able to see the step-by-step process of the NFA construction. After the NFA is constructed, a new button will
appear in place called _Click to play animation_ where the user can see the force-spring animation for the NFA.

To reset back to the easy-to-read configuration after playing the animation, click _Reset positions_.

To traverse an existing NFA with an input string to see if the string is accepted: click on _Click to input a sentence 
to traverse_. The path of the string matching algorithm through the NFA will be highlighted. Yellow nodes indicates 
nodes that were traversed while a green node is the accepted node.
