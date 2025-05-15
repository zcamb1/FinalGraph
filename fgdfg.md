```
 private fun findCellByStepId(graph: mxGraph, stepId: String): mxCell? {
        // Special handling for User Query
        if (stepId.equals("User Query", ignoreCase = true)) {
            val parent = graph.defaultParent
            val childVertices = graph.getChildVertices(parent)

            // Look for the first node containing "User Query" text
            return childVertices
                .filterIsInstance<mxCell>()
                .find { cell ->
                    val cellValue = cell.value?.toString() ?: ""
                    cellValue.contains("User Query")
                }
        }

        // Standard search for other nodes
        val parent = graph.defaultParent
        val childVertices = graph.getChildVertices(parent)

        // First try to find an exact match
        val exactMatch = childVertices
            .filterIsInstance<mxCell>()
            .find { cell ->
                val cellValue = cell.value?.toString() ?: ""
                val extractedStepId = extractStepIdFromCell(cellValue)
                extractedStepId == stepId
            }
        
        if (exactMatch != null) {
            return exactMatch
        }
        
        // If no exact match, fall back to contains search for backward compatibility
        return childVertices
            .filterIsInstance<mxCell>()
            .find { cell ->
                val cellValue = cell.value?.toString() ?: ""
                cellValue.contains(stepId)
            }
    }
```
