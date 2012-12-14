Java BTree designed to store DNA sequences.

binary file

[Metadata]
8xb Degree
8xb Root (Byte offset)
4xb Number of nodes

[Node]
8xb Key
4xb Number of Keys
1xb # isLeaf
?xb Nodes
?xb Child Keys (Byte offsets)

[Key]
1xb Sequence Length
8xb Sequence
4xb Frequency
 
[Child Key]
8xb Key (Byte offset)