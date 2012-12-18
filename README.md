Java BTree designed to store DNA sequences.

Optimal Degree:
4096 = 8 + 8 + 4 + (2T - 1)*13 + (2T)*8
T = 97

How data is stored on file:
 - #xb = number of bytes
 - node keys are their Byte offset from beginning of file

[Metadata]
4xb Degree
8xb Root (Byte offset)
4xb Number of nodes

[Node]
8xb Key
4xb Number of Keys
1xb # isLeaf
?xb keys[]
?xb children[] (Byte offsets)

[Key]
1xb Sequence Length
8xb Sequence
4xb Frequency
 
[Child Key]
8xb Key (Byte offset)


BTree Timings:

CREATE
-----------------------
  T  k   cache   time
-----------------------
 97  7    100    20.064s
 97  7    500    19.504s
  2  7    100    25.305s
  2  7    500    33.125s
  
-----------------------
  T  k   cache   time
-----------------------
 97  7    100     0.594s
 97  7    500     0.566s
  2  7    100     0.709s
  2  7    500     0.729s
