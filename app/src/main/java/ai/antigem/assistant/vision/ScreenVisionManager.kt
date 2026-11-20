package ai.antigem.assistant.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.*
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// ============================================================================
// Screen Vision - Screenshot, OCR, UI element detection
// ============================================================================

data class ScreenCapture(
    val bitmap: Bitmap,
    val timestamp: Long,
    val width: Int,
    val height: Int
)

data class OCRResult(
    val text: String,
    val blocks: List<TextBlock>,
    val confidence: Float
)

data class TextBlock(
    val text: String,
    val left: Int, val top: Int, val right: Int, val bottom: Int
)

class ScreenVisionManager(private val context: Context) {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageReader: ImageReader? = null
    private var captureHandler: Handler? = null

    // Capture screenshot via MediaProjection (simplified - uses cameraX fallback)
    suspend fun captureScreen(): ScreenCapture? = withContext(Dispatchers.IO) {
        // Note: Real screen capture requires MediaProjection permission
        // This is a placeholder that returns null until MediaProjection is set up
        null
    }

    // OCR on a bitmap
    suspend fun performOCR(bitmap: Bitmap): OCRResult = withContext(Dispatchers.IO) {
        try {
            val recognizer = TextRecognition.getClient()
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(inputImage).await()

            val text = result.text
            val blocks = result.textBlocks.map { block ->
                TextBlock(
                    text = block.text,
                    left = block.boundingBox.left,
                    top = block.boundingBox.top,
                    right = block.boundingBox.right,
                    bottom = block.boundingBox.bottom
                )
            }
            OCRResult(text = text, blocks = blocks, confidence = 0.9f)
        } catch (e: Exception) {
            OCRResult(text = "", blocks = emptyList(), confidence = 0f)
        }
    }

    // Find text on screen and return bounding boxes
    suspend fun findTextOnScreen(bitmap: Bitmap, searchText: String): List<TextBlock> = withContext(Dispatchers.IO) {
        val ocr = performOCR(bitmap)
        ocr.blocks.filter { it.text.contains(searchText, ignoreCase = true) }
    }

    // Detect UI elements (button-like regions) - simple heuristic
    suspend fun detectButtons(bitmap: Bitmap): List<TextBlock> = withContext(Dispatchers.IO) {
        val ocr = performOCR(bitmap)
        ocr.blocks.filter { block ->
            block.text.length <= 30 && block.text.isNotEmpty()
        }
    }

    // Compare two screenshots for change detection
    fun detectScreenChange(before: Bitmap, after: Bitmap): Boolean {
        if (before.width != after.width || before.height != after.height) return true
        // Simple pixel comparison - in production use perceptual hash
        return before.sameAs(after) == false
    }

    // Save screenshot to file
    suspend fun saveScreenshot(bitmap: Bitmap, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val dir = context.getExternalFilesDir(null)
            val file = File(dir, fileName)
            java.io.FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun shutdown() {
        executor.shutdown()
    }

    // ----- Helper -----
    private fun Bitmap.sameAs(other: Bitmap): Boolean {
        if (width != other.width || height != other.height) return false
        // Sample every Nth pixel for performance
        val step = 10
        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                if (getPixel(x, y) != other.getPixel(x, y)) return false
            }
        }
        return true
    }
}
