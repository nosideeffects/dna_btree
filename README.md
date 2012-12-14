Java BTree designed to store DNA sequences.

Optimal Degree:
4096 = 8 + 8 + 4 + (2T - 1)*13 + (2T)*8

[Metadata]
4xb Degree
8xb Root (Byte offset)
4xb Number of nodes

[Node]
8xb Key
4xb Number of Keys
1xb # isLeaf
?xb Keys
?xb Child Key (Byte offsets)

[Key]
1xb Sequence Length
8xb Sequence
4xb Frequency
 
[Child Key]
8xb Key (Byte offset)