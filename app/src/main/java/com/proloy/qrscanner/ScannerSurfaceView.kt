package com.proloy.qrscanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class ScannerSurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    private var scannerPaint: Paint = Paint()
    private var scannerPosition = 0f
    private var scannerSpeed = 5f // Adjust the speed of the scanner

    init {
        holder.addCallback(this)
        scannerPaint.color = Color.GREEN
        scannerPaint.strokeWidth = 5f
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Start a thread to update the scanner position and redraw
        ScannerThread().start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    inner class ScannerThread : Thread() {
        override fun run() {
            while (!isInterrupted) {
                updateScannerPosition()
                drawScanner()
                try {
                    Thread.sleep(16) // Adjust the sleep time for the desired frame rate
                } catch (e: InterruptedException) {
                    interrupt()
                }
            }
        }
    }

    private fun updateScannerPosition() {
        // Update the position of the scanner
        scannerPosition += scannerSpeed
        if (scannerPosition > height) {
            scannerPosition = 0f
        }
    }

    private fun drawScanner() {
        // Lock the canvas to draw on it
        val canvas = holder.lockCanvas()
        try {
            canvas.drawColor(Color.BLACK) // Clear the canvas
            drawScannerLine(canvas)
        } finally {
            // Unlock the canvas to show the changes
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawScannerLine(canvas: Canvas) {
        // Draw the scanning line
        canvas.drawLine(0f, scannerPosition, width.toFloat(), scannerPosition, scannerPaint)
    }
}