# Point Cloud Fractals

Since this is public, I might as well write a real Readme, I think this will also help me keep track of what I have implemented so far and what I still want to add. First what is this Project. The name is a bit misleading because it’s not actually any Fractals, you won’t be able to visualize the Menger Sponge, but rather Julia Sets of quaternion Functions.

![image](https://github.com/user-attachments/assets/6aead1e9-6aca-472a-a3ec-f8afa6e04169)

I will try to explain what that means. Imagine a Graph of a function, let’s say f(x) = x². The Graph are all the Points of the x axis after applying the function. No instead of applying the function once, apply it over and over and observe what value it approaches. For 2, it would go f(2) = 4, f(4) = 8, approaching infinity. For values greater than -1 and smaller than 1 it approaches 0. The Julia set are the sudden jumps, where instead of approaching 0 it suddenly approaches 1 and vice versa.

You an do the same for complex functions using the complex plane. The only difference is you won’t be able to draw a graph anymore. So instead colour each point according to which value it approaches. For the Function f(z) = z - (z³-1)/(3z²). The white lines representing the Julia set, so the sudden jump. Whereby moving the initial value slightly, you approach a different point. And the 3 colours representing the 3 different points each value approaches. I highly recommend watching this video for more details:

(https://www.youtube.com/watch?v=-RdOwhmqP5s)

![image](https://github.com/user-attachments/assets/776b4bc7-630d-4b47-ac62-2ff1c9e1f1f2)

Again, you can do the same for quaternion functions this time in 4 dimensions. The reason why you can’t use 3 Dimensions is because there doesn’t exist a well-defined multiplication the same as quaternion and complex multiplication. So instead visualize a 3-dimensional slice through 4-Dimensional space, where every point in space gets value associated with it. The point at (1,1,1) gets the quaternion 1 + i +j +(0*k) and so on. A difference is that you can only draw the Julia set else you wouldn’t be able to see anything, the quaternion plane would be “solid”. The function used here is f(q) = q² -1 + 0.2i

![image](https://github.com/user-attachments/assets/a0ec30f5-86a9-4776-8671-abfaf4da1d1d)


# Implementation
My actual implementation works like this. I separate a 3D (2D) slice of the quaternion plane into voxels (pixels), for each voxel (pixel) I calculate its approaching value and one of a very close neighbour. If the values stay close after n Iteration, I do nothing. If the distance between gets high, I draw a Point at the Voxel (Pixel). Since I see approaching infinity as approaching a Point, I must be careful to let enough iterations pass for their floating-point value to be both reach infinite.

Functions:  <br>
You can enter and render your own custom Functions, doing this requires the shader to be recompiled. Functions are allowed to have this Form:
*	q: is the iterated Value, i.e. The functions start with f(q) = “your input”
*	quant(a,b,c,d): is a quaternion. a,b,c,d being some real number of Variable
*	n: real Variable
*	t: real Variable
*	c: quaternion of the corresponding pixel or Voxel 
*	a $ b: binary Operation. a and b are allowed to be quaternion, real numbers or variable. $ being any of (*, \, +,  -, ^) For power (^) if b is a quaternion only its real part.
* unary(x): unary operator, x being a quaternion, real number or variable. unary being any of(sin, cos, exp, ln)
*	The result of an operation is always a quaternion or a real number and can therefore be inside any other operation. For example: sin(q²) or (q+n)*2
*	Parentheses are allowed.



# Settings

You can visualize the Julia Set as a 2D or 3D slice. 2D slices have the advantage that except changing the function every setting can be changed in real Time. The reason for this is that all calculations are done in live, so it also allows to move and zoom into the complex plane. Its also much faster to compile. There is also the raymarching option which tries the same in 3D using raymarching but is painfully slow and I don’t recommend using it. These are the Features in 2D:
*	Variable t: goes between -1 and 1 is floating point
*	Timer Auto: t automatically goes between -1 and 1 (sine)
*	Change speed of animation
*	Variable n: is integer change between 0 and 10
*	Max iteration: How many iterations are allowed before checking the distance between the difference
*	Nudge Value: How close the Neighbour Point is to the original.
*	Breakout Factor: How many iterations get tested before distance is evaluated. See note above.
*	Colour and Colour2: changes the colour of the Julia set. Final colour is mix of both based on how many iterations it took for point and neighbour to separate.
*	Background Colour: Is the Colour of the Background
*	Custom q Zero:q Zero is the initial Value of the function. Standard is c. But can be changed to any quaternion. Keep in mind that this only makes sense if the function adds or subtracts a constant value.

3D is the main part of this project. It renders the quaternion as Point Clouds. Since calculating the points in 3D is vastly more expensive, the points are compiled for a certain Volume, changing parameters live is therefore not an option. I also calculate the normal Vector of each Point which while quite expensive allows the shape of the Julia set to more easily read. Since for a good image of the Julia Set you need between hundreds of thousand and millions of Points, its very VRAM intensive. Running out of VRAM results in a crash, so be careful with how many points you’re trying to render. Calculating each voxel is also quite intensive and may take time. 
New Settings: <br>
* range: sets range of Voxel Volume, based on x coord of XYZ Resolution
*Normal Precision: How exact the normal Vectors are
*Normal Step Size: how big the volume that affects the normal Vector is
*Quad Size: Size of the Quad drawn to the screen
* Resolution: How many points are calculated (technically Workgroup Dimensions). To high may crash Program. The Volume is a cube with side length 1
*Custom  XYZ resolution. Sets workgroup Dimension to be a Cuboid. Side length Ratio defined by X:Y:Z Ratio.
*Invert: sets calculating mode to  invert point cloud (only starts next Compile)
*Post processing mode: Changes post processing shader for different visualisations


# Movement

Movement 2D:
*  W,A,S,D:  Up, down, left, right
* Q,E: Zoom in Zoom out
* 1: sets drawing mode to Black and white, draws only Julia Set
* 2: sets mode to RGB, draws based closest root of z³
* 3: sets mode to smooth Shading, works best with complex polynomials and Breakout factor at 0%

Movement 3D:
* Looking around: Hold left mouse button and move mouse around
* W,A,S,D:  forwards, backwards, left, right
*Q, E: Ups movement speed, downs movement speed
* Shift, Space: down, up




