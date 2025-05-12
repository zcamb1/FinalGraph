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
        graphPanel.setBounds(0, 200, 1920, 1080)
        graphPanel.repaint()
        graphPanel.revalidate()
        layeredPane.repaint()
        layeredPane.revalidate()

        repaint()
        revalidate()
    }
    
    /**
     * Hiển thị giao diện thêm node mới
     * Được gọi khi người dùng nhấp vào nút dấu cộng
     */
    fun showAddNodeUI() {
        addNode()
    }
    
    /**
     * Hiển thị UI thêm node (phương thức cũ để tương thích)
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
     * Đóng giao diện thêm node và trở lại graph panel
     */
    fun closeNode() {
        // addNodePanel.isVisible = false // Original UI - commented out temporarily
        simpleAddNodePanel.isVisible = false // Temporary UI for adding nodes
    }

    /**
     * Cập nhật lại UI (phương thức cũ để tương thích)
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
     * Thêm node mới vào graph sau khi người dùng hoàn thành việc thêm từ AddNodeUI
     * 
     * @param title Tiêu đề của node
     * @param x Tọa độ x
     * @param y Tọa độ y 
     */
    fun addNewNode(title: String, x: Double, y: Double) {
        // Thêm node mới sử dụng GraphPanel
        GraphPanel.addNewNode(title, x, y)
        
        // Trở lại graph panel
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
}