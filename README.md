###### this is an implementation of 2-pass linker, which demonstrates the knolwedge of memory recolation
![alt text](https://en.wikipedia.org/wiki/Linker_(computing)#/media/File:Linker.svg "linker")

##### Compile the program:
---------------------

	$ java linker input.txt 
	
##### Run the program:
---------------------

	$ java linker input.txt

**Sample input**:
> 
1   xy 2  
1   z 4  
5   R 1004  I 5678  E 2777  R 8002  E 7002  
0  
1   z 3  
6   R 8001  E 1777  E 1001  E 3002  R 1002  A 1010  
0  
1   z 1  
2   R 5001  E 4777  
1   z 2  
1   xy 2  
3   A 8000  E 1777  E 2001  


**Sample output**:
> Symbol Table  
> xy=2  
z=15  
Memory Map  
0:  1004  
1:  5678  
2:  2015  
3:  8002  
4:  7015  
5:  8006  
6:  1015  
7:  1015  
8:  3015  
9:  1007  
10: 1010  
11: 5012  
12: 4015  
13: 8000  
14: 1002  
15: 2002  
