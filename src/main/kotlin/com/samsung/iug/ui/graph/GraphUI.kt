package com.samsung.iug.ui.graph

import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JLayeredPane
import java.awt.Color
import java.awt.Dimension
import com.intellij.ui.util.preferredHeight
import com.intellij.ui.util.preferredWidth
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

/**
 * Main UI class managing the graph panel and other UI components
 */
object GraphUI: JPanel(BorderLayout()) {
    private val graphPanel = GraphPanel // Đây là phần UI của Graph
    // private val addNodePanel = AddNodeUI() // Original UI - commented out temporarily
    private val simpleAddNodePanel = SimpleAddNodeUI() // Temporary UI for adding nodes
    val layeredPane = JLayeredPane().apply {
        layout = null
        add(graphPanel)
        // add(addNodePanel) // Original UI - commented out temporarily
        add(simpleAddNodePanel) // Temporary UI for adding nodes
        setLayer(graphPanel, JLayeredPane.DEFAULT_LAYER)
        // setLayer(addNodePanel, JLayeredPane.POPUP_LAYER) // Original UI - commented out temporarily
        setLayer(simpleAddNodePanel, JLayeredPane.POPUP_LAYER) // Temporary UI for adding nodes
    }
    
    init {
        this.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        this.isOpaque = false

        // addNodePanel.isVisible = false // Original UI - commented out temporarily
        simpleAddNodePanel.isVisible = false // Temporary UI for adding nodes
        background = Color(0,0,0,0)
        add(layeredPane, BorderLayout.CENTER)
        graphPanel.setBounds(0, 0, 1920, 1080)
        graphPanel.repaint()
        graphPanel.revalidate()                             
        layeredPane.repaint()
        layeredPane.revalidate()

        repaint()
        revalidate()
    }
    
    /**
     * Show the add node UI
     * Called when the user clicks on the add button in the toolbar
     */
    fun showAddNodeUI() {
        // If source button is set, use legacy mode
        if (simpleAddNodePanel.hasSourceButtonCell()) {
            addNode()
        } else {
            // Just show the dialog
            addNode()
        }
    }
    
    /**
     * Show the add node UI (old method for compatibility)
     */
    fun addNode() {
        val centerX = layeredPane.width / 2 - 175 // Adjusted for SimpleAddNodeUI width
        val centerY = layeredPane.height / 2 - 150 // Centered vertically
        // addNodePanel.setBounds(centerX, centerY, addNodePanel.preferredWidth, addNodePanel.preferredHeight) // Original UI - commented out temporarily
        simpleAddNodePanel.setBounds(centerX, centerY, simpleAddNodePanel.preferredWidth, simpleAddNodePanel.preferredHeight) // Temporary UI for adding nodes
        // addNodePanel.isVisible = true // Original UI - commented out temporarily
        simpleAddNodePanel.isVisible = true // Temporary UI for adding nodes
        // addNodePanel.repaint() // Original UI - commented out temporarily
        // addNodePanel.revalidate() // Original UI - commented out temporarily
        simpleAddNodePanel.repaint() // Temporary UI for adding nodes
        simpleAddNodePanel.revalidate() // Temporary UI for adding nodes
        layeredPane.repaint()
        layeredPane.revalidate()
    }

    /**
     * Close the add node UI and return to the graph panel
     */
    fun closeNode() {
        // addNodePanel.isVisible = false // Original UI - commented out temporarily
        simpleAddNodePanel.isVisible = false // Temporary UI for adding nodes
    }

    /**
     * Update the UI layout (old method for compatibility)
     */
    fun repaintLayered() {
        val centerX = layeredPane.width / 2 - 175 // Adjusted for SimpleAddNodeUI width
        val centerY = layeredPane.height / 2 - 150 // Centered vertically
        // addNodePanel.setBounds(centerX, centerY, addNodePanel.preferredWidth, addNodePanel.preferredHeight) // Original UI - commented out temporarily
        simpleAddNodePanel.setBounds(centerX, centerY, simpleAddNodePanel.preferredWidth, simpleAddNodePanel.preferredHeight) // Temporary UI for adding nodes
        // addNodePanel.repaint() // Original UI - commented out temporarily
        // addNodePanel.revalidate() // Original UI - commented out temporarily
        simpleAddNodePanel.repaint() // Temporary UI for adding nodes
        simpleAddNodePanel.revalidate() // Temporary UI for adding nodes
        layeredPane.repaint()
        layeredPane.revalidate()
    }
    
    /**
     * Add a new node to the graph after the user completes adding from AddNodeUI
     * 
     * @param title Title of the node
     * @param x X coordinate
     * @param y Y coordinate
     */
    fun addNewNode(title: String, x: Double, y: Double) {
        // Add new node using GraphPanel
        GraphPanel.addNewNode(title, x, y)
        
        // Return to graph panel
        closeNode()
    }

    /**
     * Set the source button cell that was clicked
     * 
     * @param cell The add button cell that was clicked
     */
    fun setSourceButtonCell(cell: com.mxgraph.model.mxCell) {
        simpleAddNodePanel.setSourceButtonCell(cell)
    }

    fun setPanningMode(enabled: Boolean) {
        graphPanel.setPanningMode(enabled)
    }
}