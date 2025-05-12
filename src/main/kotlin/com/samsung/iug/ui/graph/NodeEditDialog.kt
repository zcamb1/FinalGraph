package com.samsung.iug.ui.graph

import com.mxgraph.model.mxCell
import com.mxgraph.view.mxGraph
import com.samsung.iug.graph.listNode
import java.awt.*
import javax.swing.*
import javax.swing.border.LineBorder

/**
 * Dialog for editing nodes in the graph
 */
object NodeEditDialog {
    
    /**
     * Show edit dialog for the given cell
     * 
     * @param parent The parent component (GraphPanel)
     * @param cell The cell to edit
     * @param graph The mxGraph instance
     * @param updateCallback Optional callback after update
     */
    fun showEditDialog(parent: JPanel, cell: mxCell, graph: mxGraph, updateCallback: (() -> Unit)? = null) {
        // Get current content
        val content = cell.value.toString()
        
        // Extract query from HTML content
        val queryText = extractTextFromCell(cell)
        
        // Create edit dialog similar to EditUtteranceDialog
        val window = SwingUtilities.getWindowAncestor(parent)
        val dialog = JDialog(window as Frame, "Edit User Query", true)
        dialog.layout = BorderLayout()
        dialog.isUndecorated = true
        dialog.background = Color(0, 0, 0, 0)
        
        // Create a rounded panel like in EditUtteranceDialog
        val panel = createRoundedPanel()
        panel.layout = GridBagLayout()
        
        val textArea = JTextArea(queryText)
        textArea.foreground = Color.WHITE
        textArea.background = Color(25, 25, 25)
        textArea.caretColor = Color.WHITE
        textArea.font = Font("Arial", Font.PLAIN, 13)
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.margin = Insets(8, 10, 8, 10)
        
        val scrollPane = JScrollPane(textArea)
        scrollPane.border = BorderFactory.createLineBorder(Color(100, 100, 100), 1, true)
        scrollPane.preferredSize = Dimension(240, 70)
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        buttonPanel.isOpaque = false
        
        val updateButton = createRoundedButton("Update")
        updateButton.addActionListener {
            // Update node content
            graph.model.beginUpdate()
            try {
                // Update cell value with HTML content instead of plain text
                graph.model.setValue(cell, createQueryNodeHtml(textArea.text))
                
                // Update node data in listNode if needed
                if (listNode.listNode.isNotEmpty()) {
                    listNode.listNode[0].guildContent = textArea.text
                }
                
                // Call the update callback if provided
                updateCallback?.invoke()
            } finally {
                graph.model.endUpdate()
            }
            dialog.dispose()
        }
        
        val cancelButton = createRoundedButton("Cancel")
        cancelButton.addActionListener { dialog.dispose() }
        
        buttonPanel.add(updateButton)
        buttonPanel.add(cancelButton)
        
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.insets = Insets(10, 10, 5, 10)
        panel.add(JLabel("EDIT USER QUERY").apply {
            foreground = Color.WHITE
            font = Font("Arial", Font.BOLD, 14)
        }, gbc)
        
        gbc.gridy = 1
        gbc.insets = Insets(5, 10, 5, 10)
        panel.add(scrollPane, gbc)
        
        gbc.gridy = 2
        gbc.insets = Insets(5, 10, 10, 10)
        panel.add(buttonPanel, gbc)
        
        dialog.contentPane = panel
        dialog.pack()
        
        // Force rounded shape on the dialog itself like in EditUtteranceDialog
        dialog.shape = java.awt.geom.RoundRectangle2D.Float(
            0f, 0f, dialog.width.toFloat(), dialog.height.toFloat(), 20f, 20f
        )
        
        // Position dialog so it doesn't cover the hover icons
        if (parent is GraphPanel) {
            val graphComponent = parent.getGraphComponent()
            val cellBounds = graph.getCellBounds(cell)
            
            if (cellBounds != null) {
                // Convert cell bounds to screen coordinates
                val point = Point(cellBounds.x.toInt(), cellBounds.y.toInt() + cellBounds.height.toInt())
                SwingUtilities.convertPointToScreen(point, graphComponent.graphControl)
                
                // Set dialog position with increased vertical offset (50px) to avoid covering hover icons
                dialog.setLocation(point.x + 10, point.y + 50)
            } else {
                dialog.setLocationRelativeTo(parent)
            }
        } else {
            dialog.setLocationRelativeTo(parent)
        }
        
        dialog.isVisible = true
    }
    
    /**
     * Create a rounded panel like in EditUtteranceDialog
     */
    private fun createRoundedPanel(): JPanel {
        return object : JPanel() {
            init {
                isOpaque = false
                border = BorderFactory.createLineBorder(Color(60, 60, 60), 1, true)
            }
            
            override fun paintComponent(g: Graphics) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.color = Color(36, 36, 36)
                g2.fillRoundRect(0, 0, width, height, 20, 20)
                super.paintComponent(g)
            }
        }
    }
    
    /**
     * Create a rounded button like in EditUtteranceDialog
     */
    private fun createRoundedButton(text: String): JButton {
        return object : JButton(text) {
            init {
                isFocusPainted = false
                isContentAreaFilled = false
                foreground = Color.WHITE
                background = Color(60, 60, 60)
                font = Font("Arial", Font.PLAIN, 13)
                border = BorderFactory.createLineBorder(Color(150, 150, 150), 1, true)
                preferredSize = Dimension(90, 30)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }
            
            override fun paintComponent(g: Graphics) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.color = background
                g2.fillRoundRect(0, 0, width, height, 16, 16)
                g2.color = foreground
                val fm = g.fontMetrics
                val textWidth = fm.stringWidth(text)
                val textY = (height + fm.ascent - fm.descent) / 2
                g2.drawString(text, (width - textWidth) / 2, textY)
            }
        }
    }
    
    /**
     * Extract text from mxCell (from either HTML content or direct string)
     */
    private fun extractTextFromCell(cell: mxCell): String {
        val value = cell.value?.toString() ?: ""
        
        // Check if it's HTML content
        return if (value.contains("<")) {
            val textPattern = "<tr><td>(.*?)</td></tr>".toRegex(RegexOption.DOT_MATCHES_ALL)
            val match = textPattern.find(value)
            match?.groupValues?.get(1)?.unescapeHtml() ?: value
        } else {
            // It's already plain text
            value
        }
    }
    
    /**
     * Create HTML content for a user query node
     */
    fun createQueryNodeHtml(text: String): String {
        return """
          <table style="width:150px; color:#fff; font-family:Arial; font-size:11px;margin-top:25px;padding-left:8px">
                <tr><td>${text.escapeHtml()}</td></tr> 
                <tr><td style="font-weight:bold; opacity:0.7;">User Query</td></tr>
            </table>
            """.trimIndent()
    }
    
    /**
     * Create HTML content for a step node
     */
    fun createStepNodeHtml(guideText: String, stepId: String): String {
        return """
          <table style="width:150px; color:#fff; font-family:Arial; font-size:11px;margin-top:25px;padding-left:8px">
                <tr><td>${guideText.escapeHtml()}</td></tr> 
                <tr><td style="font-weight:bold; opacity:0.7;">${stepId.escapeHtml()}</td></tr>
            </table>
            """.trimIndent()
    }
    
    /**
     * Create HTML content for a node with custom subtitle
     */
    fun createNodeHtml(text: String, subtitle: String): String {
        return """
          <table style="width:150px; color:#fff; font-family:Arial; font-size:9px;margin-top:30px;">
                  <tr><td>${text.escapeHtml()}</td></tr>
                  <tr><td style="font-weight:bold; opacity:0.7;">${subtitle.escapeHtml()}</td></tr>
                </table>
            """.trimIndent()
    }
    
    /**
     * Extension function to escape HTML special characters
     */
    private fun String.escapeHtml(): String {
        return this.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
    
    /**
     * Extension function to unescape HTML special characters
     */
    private fun String.unescapeHtml(): String {
        return this.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }
} 