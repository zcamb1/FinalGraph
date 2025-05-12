package com.samsung.iug.ui.preview

import com.samsung.iug.device.getAdb
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.concurrent.fixedRateTimer

class ImageLabel : JLabel() {
    var highlightRect: Rectangle? = null
    var scaleX = 1.0
    var scaleY = 1.0

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        highlightRect?.let {
            val g2 = g as Graphics2D
            g2.color = Color.RED
            g2.stroke = BasicStroke(2f)

            val x = (it.x / scaleX).toInt()
            val y = (it.y / scaleY).toInt()
            val w = (it.width / scaleX).toInt()
            val h = (it.height / scaleY).toInt()

            g2.drawRect(x, y, w, h)
        }
    }
}

object ScreenMirror {
    val panel = JPanel(BorderLayout())
    private val imageLabel = ImageLabel().apply {
        horizontalAlignment = SwingConstants.CENTER
    }

    val adbPath = getAdb().absolutePath
    private var mouseListener: MouseAdapter? = null
    private var isStreaming = false
    private var captureTimer: java.util.Timer? = null
    private var highlightRect: Rectangle? = null

    init {
        panel.add(imageLabel, BorderLayout.CENTER)
    }

    fun getDeviceScreenSizeDdmlib(device: String): Pair<Int, Int> {
        val process = Runtime.getRuntime().exec("$adbPath -s $device shell wm size")
        val reader = process.inputStream.bufferedReader()
        val output = reader.readText()
        val match = Regex("Physical size: (\\d+)x(\\d+)").find(output)
        if (match != null) {
            val (width, height) = match.destructured
            return Pair(width.toInt(), height.toInt())
        }
        throw RuntimeException("Cannot get screen size from device")
    }

    fun mapToDevice(panelX: Int, panelY: Int, device: String): Pair<Int, Int> {
        val (deviceWidth, deviceHeight) = getDeviceScreenSizeDdmlib(device)
        val imageIcon = imageLabel.icon as? ImageIcon ?: return Pair(0, 0)

        val imageWidth = imageIcon.iconWidth
        val imageHeight = imageIcon.iconHeight
        val labelWidth = imageLabel.width
        val labelHeight = imageLabel.height

        val offsetX = (labelWidth - imageWidth) / 2
        val offsetY = (labelHeight - imageHeight) / 2

        val relativeX = panelX - offsetX
        val relativeY = panelY - offsetY

        if (relativeX < 0 || relativeY < 0 || relativeX > imageWidth || relativeY > imageHeight) {
            return Pair(0, 0)
        }

        val scaleX = deviceWidth / imageWidth.toDouble()
        val scaleY = deviceHeight / imageHeight.toDouble()

        return Pair((relativeX * scaleX).toInt(), (relativeY * scaleY).toInt())
    }

    fun startStream(device: String) {
        isStreaming = true
        imageLabel.text = "Connecting..."

        Thread {
            while (isStreaming) {
                try {
                    val process = ProcessBuilder(adbPath, "-s", device, "exec-out", "screencap", "-p")
                        .redirectErrorStream(true)
                        .start()

                    val image = ImageIO.read(process.inputStream)
                    if (image != null) {
                        val targetWidth = 300
                        val aspectRatio = image.height.toDouble() / image.width
                        val targetHeight = (targetWidth * aspectRatio).toInt()

                        val scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
                        val icon = ImageIcon(scaledImage)

                        SwingUtilities.invokeLater {
                            imageLabel.icon = icon
                            imageLabel.text = null
                            imageLabel.setSize(targetWidth, targetHeight)
                            imageLabel.revalidate()
                            imageLabel.repaint()

                            imageLabel.scaleX = image.width.toDouble() / targetWidth
                            imageLabel.scaleY = image.height.toDouble() / targetHeight
                        }
                    }

                    Thread.sleep(500)
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }.start()
    }

    fun stopStream(device: String) {
        isStreaming = false
        captureTimer?.cancel()
        captureTimer = null

        SwingUtilities.invokeLater {
            imageLabel.icon = null
            imageLabel.text = "Not connected"
            imageLabel.revalidate()
            imageLabel.repaint()
        }
    }

    fun startControl(device: String) {
        mouseListener = object : MouseAdapter() {
            private var lastX = -1
            private var lastY = -1

            override fun mousePressed(e: MouseEvent) {
                val (x, y) = mapToDevice(e.x, e.y, device)
                lastX = x
                lastY = y
            }

            override fun mouseReleased(e: MouseEvent) {
                val (x, y) = mapToDevice(e.x, e.y, device)
                val endX = x
                val endY = y

                val isTap = (Math.abs(lastX - endX) < 10) && (Math.abs(lastY - endY) < 10)
                val duration = if (isTap) 1 else 300
                Runtime.getRuntime().exec("$adbPath -s $device shell input swipe $lastX $lastY $endX $endY $duration")
            }
        }
        imageLabel.addMouseListener(mouseListener!!)
    }

    fun stopControl() {
        mouseListener?.let {
            imageLabel.removeMouseListener(it)
            mouseListener = null
        }
    }

    fun highlight(bounds: Rectangle) {
        highlightRect = bounds
        imageLabel.highlightRect = bounds
        repaintMirror()
    }

    private fun repaintMirror() {
        imageLabel.repaint()
    }
}