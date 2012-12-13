Java BTree designed to store DNA sequences.

Binary File
[Metadata]
16xb Degree
16xb Root offset
 8xb Number of nodes
 8xb Sequence Length

[Node]
16xb Key (byte offset)
 8xb Number of keys
 2xb isLeaf
 ?xb Keys
 ?xb Children

[Key]
16xb Sequence
 8xb Frequency

[Child]
16xb Key (byte offest)
