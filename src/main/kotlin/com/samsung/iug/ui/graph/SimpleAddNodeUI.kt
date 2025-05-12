package com.samsung.iug.ui.graph

import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGeometry
import com.samsung.iug.ui.graph.NodeEditDialog

/**
 * Temporary simplified UI for adding nodes
 * Used while the main AddNodeUI is being worked on by someone else
 */
class SimpleAddNodeUI : JPanel() {
    private val stepIdField = JTextField(15)
    private val guideContentField = JTextArea(4, 15)
    private val cancelButton = JButton("Cancel")
    private val addButton = JButton("Add Node")
    
    // Reference to the clicked add button cell
    private var sourceButtonCell: mxCell? = null
    
    init {
        layout = BorderLayout(10, 10)
        border = EmptyBorder(20, 20, 20, 20)
        background = Color(30, 30, 30) // Dark background
        
        // Create title
        val titleLabel = JLabel("Add New Node")
        titleLabel.foreground = Color.WHITE
        titleLabel.font = Font("Arial", Font.BOLD, 16)
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        
        // Create form panel
        val formPanel = JPanel(GridBagLayout())
        formPanel.background = Color(30, 30, 30) // Dark background
        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            fill = GridBagConstraints.HORIZONTAL
        }
        
        // Step ID field
        val stepIdLabel = JLabel("Step ID (Node Name):")
        stepIdLabel.foreground = Color.WHITE
        gbc.gridx = 0
        gbc.gridy = 0
        formPanel.add(stepIdLabel, gbc)
        
        stepIdField.apply {
            font = Font("Arial", Font.PLAIN, 14)
            text = "User Query" // Default value
        }
        gbc.gridx = 0
        gbc.gridy = 1
        formPanel.add(stepIdField, gbc)
        
        // Guide content field
        val guideContentLabel = JLabel("Guide Content:")
        guideContentLabel.foreground = Color.WHITE
        gbc.gridx = 0
        gbc.gridy = 2
        formPanel.add(guideContentLabel, gbc)
        
        guideContentField.apply {
            font = Font("Arial", Font.PLAIN, 14)
            text = "What does the user ask?" // Default value
            lineWrap = true
            wrapStyleWord = true
        }
        val scrollPane = JScrollPane(guideContentField)
        gbc.gridx = 0
        gbc.gridy = 3
        formPanel.add(scrollPane, gbc)
        
        // Buttons panel
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 0))
        buttonsPanel.background = Color(30, 30, 30) // Dark background
        
        cancelButton.apply {
            font = Font("Arial", Font.PLAIN, 14)
            preferredSize = Dimension(120, 30)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addActionListener { closeUI() }
        }
        
        addButton.apply {
            font = Font("Arial", Font.BOLD, 14)
            preferredSize = Dimension(120, 30)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addActionListener { addNewNode() }
        }
        
        buttonsPanel.add(cancelButton)
        buttonsPanel.add(addButton)
        
        // Add components to main panel
        add(titleLabel, BorderLayout.NORTH)
        add(formPanel, BorderLayout.CENTER)
        add(buttonsPanel, BorderLayout.SOUTH)
        
        // Set preferred size
        preferredSize = Dimension(350, 300)
    }
    
    /**
     * Set the add button cell that was clicked
     * 
     * @param cell The add button cell that was clicked
     */
    fun setSourceButtonCell(cell: mxCell) {
        sourceButtonCell = cell
    }
    
    /**
     * Close the UI
     */
    private fun closeUI() {
        GraphUI.closeNode()
    }
    
    /**
     * Add a new node with the entered data
     */
    private fun addNewNode() {
        // Get values from fields
        val stepId = stepIdField.text.trim()
        val guideContent = guideContentField.text.trim()
        
        // Validate inputs
        if (stepId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Step ID", "Validation Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        
        if (guideContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Guide Content", "Validation Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        
        // Check if we have the source button reference
        if (sourceButtonCell != null) {
            val graph = GraphPanel.getGraph()
            val sourceNode = AddButtonConnector.findParentNodeForAddButton(graph, sourceButtonCell!!)
            
            if (sourceNode != null) {
                // Get the geometry of the button that was clicked
                val buttonGeometry = graph.getCellGeometry(sourceButtonCell)
                val sourceNodeGeometry = graph.getCellGeometry(sourceNode)
                
                if (buttonGeometry != null && sourceNodeGeometry != null) {
                    // Begin update to batch all graph changes
                    graph.model.beginUpdate()
                    try {
                        // First, remove all edges connected to the add button
                        val edges = graph.getEdges(sourceButtonCell)
                        if (edges != null && edges.isNotEmpty()) {
                            graph.removeCells(edges)
                        }
                        
                        // Then remove the add button itself
                        graph.removeCells(arrayOf(sourceButtonCell))
                        
                        // Get node dimensions
                        val nodeWidth = GraphPanel.NODE_WIDTH
                        val nodeHeight = GraphPanel.NODE_HEIGHT
                        
                        // Calculate better position for the new node to avoid overlap
                        // Place it to the right of the source node with proper spacing
                        val newNodeX = sourceNodeGeometry.x + nodeWidth + AddButtonConnector.HORIZONTAL_SPACING / 2
                        
                        // Keep it at the same Y level as the source node
                        val newNodeY = sourceNodeGeometry.y
                        
                        // Create a new node at the calculated position
                        val newNode = graph.insertVertex(
                            graph.defaultParent, null, NodeEditDialog.createStepNodeHtml(guideContent, stepId),
                            newNodeX, newNodeY, nodeWidth, nodeHeight, "stepNode"
                        ) as mxCell
                        
                        // Create a connection (edge) between the source node and the new node
                        graph.insertEdge(
                            graph.defaultParent, null, "", 
                            sourceNode, newNode, 
                            "stepEdge"
                        )
                        
                        // Add new add button after the new node
                        val addButton = AddButtonConnector.createAddButtonForNode(graph, graph.defaultParent, newNode)
                        
                        // Update the add button style to green
                        graph.model.setStyle(addButton, "stepAddButton")
                        
                        // Refresh the graph
                        graph.refresh()
                    } finally {
                        graph.model.endUpdate()
                    }
                } else {
                    // Fallback if geometry can't be determined
                    GraphPanel.addNewNode(stepId, 100.0, 100.0)
                }
            } else {
                // Fallback if source node not found
                GraphPanel.addNewNode(stepId, 100.0, 100.0)
            }
        } else {
            // Fallback if no source button set
            GraphPanel.addNewNode(stepId, 100.0, 100.0)
        }
        
        // Close the UI
        closeUI()
    }
} 