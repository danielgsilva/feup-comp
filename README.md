# Compiler Project

Contains a reference implementation for the compiler project.

```
Tip: Use the analysis process to annotate the tree with extra information when you think it is relevant. [...] 
Since for each expression node you will need to know its type, 
it can be beneficial to add this information to the AST while you are
“analysing” those nodes (e.g. add a property “type”=“int” in the node).
```

Seguiu-se esta tip, e durante a análise semântica o tipo de cada nó foi adicionado no formato 
"type"="Type [name=int, isArray=false]". 
Essencialmente é o que `AddType.java` faz.